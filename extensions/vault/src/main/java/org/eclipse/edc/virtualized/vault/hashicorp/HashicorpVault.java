/*
 *  Copyright (c) 2022 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Initial API and Implementation
 *       Materna Information & Communications SE - Refactoring
 *       Cofinity-X - implement extensible authentication
 *
 */

package org.eclipse.edc.virtualized.vault.hashicorp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.virtualized.vault.hashicorp.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_SECRET_METADATA_PATH;
import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_TOKEN_HEADER;

/**
 * Implements a vault backed by Hashicorp Vault.
 */
class HashicorpVault implements Vault {
    private static final String VAULT_SECRET_DATA_PATH = "data";
    private static final String VAULT_DATA_ENTRY_NAME = "content";

    private final Monitor monitor;
    private final HashicorpVaultConfig settings;
    private final EdcHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HashicorpVault(@NotNull Monitor monitor,
                          HashicorpVaultConfig settings,
                          EdcHttpClient httpClient,
                          ObjectMapper objectMapper) {
        this.monitor = monitor;
        this.settings = settings;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public @Nullable String resolveSecret(String key) {

        var requestUri = getSecretUrl(key, VAULT_SECRET_DATA_PATH);
        var request = new Request.Builder()
                .url(requestUri)
                .header(VAULT_TOKEN_HEADER, getVaultToken(settings))
                .get()
                .build();

        try (var response = httpClient.execute(request)) {

            if (response.isSuccessful()) {

                var responseBody = response.body();
                if (responseBody != null) {
                    // using JsonNode here because it makes traversing down the tree null-safe
                    var payload = objectMapper.readValue(responseBody.string(), JsonNode.class);
                    return payload.path("data").path("data").get(VAULT_DATA_ENTRY_NAME).asText();
                }
                monitor.debug("Secret response body is empty");

            } else {
                if (response.code() == 404) {
                    monitor.debug("Secret not found");
                } else {
                    monitor.debug("Failed to get secret with status %d".formatted(response.code()));
                }
            }
        } catch (IOException e) {
            monitor.warning("Failed to get secret with reason: %s".formatted(e.getMessage()));
        }
        return null;
    }

    @Override
    public Result<Void> storeSecret(String key, String value) {

        var requestUri = getSecretUrl(key, VAULT_SECRET_DATA_PATH);

        var requestPayload = Map.of("data", Map.of(VAULT_DATA_ENTRY_NAME, value));
        var request = new Request.Builder()
                .url(requestUri)
                .header(VAULT_TOKEN_HEADER, getVaultToken(settings))
                .post(jsonBody(requestPayload))
                .build();

        try (var response = httpClient.execute(request)) {
            if (response.isSuccessful()) {
                return response.body() == null ? Result.failure("Setting secret returned empty body") : Result.success();
            } else {
                return Result.failure("Failed to set secret with status %d".formatted(response.code()));
            }
        } catch (IOException e) {
            return Result.failure("Failed to set secret with reason: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Result<Void> deleteSecret(String key) {
        var requestUri = getSecretUrl(key, VAULT_SECRET_METADATA_PATH);
        var request = new Request.Builder()
                .url(requestUri)
                .header(VAULT_TOKEN_HEADER, getVaultToken(settings))
                .delete()
                .build();

        try (var response = httpClient.execute(request)) {
            return response.isSuccessful() || response.code() == 404 ? Result.success() : Result.failure("Failed to destroy secret with status %d".formatted(response.code()));
        } catch (IOException e) {
            return Result.failure("Failed to destroy secret with reason: %s".formatted(e.getMessage()));
        }
    }

    private String getVaultToken(HashicorpVaultConfig config) {
        // token is provided
        if(config.credentials().getToken() != null) {
            return config.credentials().getToken();
        }

        // get JWT from IdP
        var accessToken = getAccessToken(config.credentials());

        // use JWT to get vault token

        var requestUri = HttpUrl.parse(config.vaultUrl()).newBuilder("v1/auth/jwt/login").build();
        var request = new Request.Builder()
                .url(requestUri)
                .post(RequestBody.create("""
                        {
                            "role": "participant",
                            "jwt": "%s"
                        }
                        """.formatted(accessToken).getBytes()))
                .build();

        try (var response = httpClient.execute(request)) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new EdcException("Failed to obtain vault token");
            }
            var json = objectMapper.readValue(response.body().string(), JsonNode.class);
            return json.path("auth").path("client_token").asText();
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    private String getAccessToken(HashicorpVaultCredentials credentials) {

        // OAuth Credentials are provided
        var requestUri = HttpUrl.parse(credentials.getTokenUrl());
        var request = new Request.Builder()
                .url(requestUri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(RequestBody.create(
                        "grant_type=client_credentials" +
                                "&client_id=" + credentials.getClientId() +
                                "&client_secret=" + credentials.getClientSecret(),
                        MediaType.parse("application/x-www-form-urlencoded")))
                .build();

        try (var response = httpClient.execute(request)) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new EdcException("Failed to obtain vault token");
            }
            var json = objectMapper.readValue(response.body().string(), JsonNode.class);
            return json.path("access_token").asText();
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    private HttpUrl getSecretUrl(String key, String entryType) {
        key = URLEncoder.encode(key, StandardCharsets.UTF_8);

        // restore '/' characters to allow subdirectories
        var sanitizedKey = key.replace("%2F", "/");

        var vaultApiPath = settings.secretsPath();
        var folderPath = settings.folderPath();

        var builder = HttpUrl.parse(settings.vaultUrl())
                .newBuilder()
                .addPathSegments(PathUtil.trimLeadingOrEndingSlash(vaultApiPath))
                .addPathSegment(entryType);

        if (folderPath != null) {
            builder.addPathSegments(PathUtil.trimLeadingOrEndingSlash(folderPath));
        }

        return builder
                .addPathSegments(sanitizedKey)
                .build();
    }

    private RequestBody jsonBody(Object body) {
        String jsonRepresentation;
        try {
            jsonRepresentation = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new EdcException(e);
        }
        return RequestBody.create(jsonRepresentation, VaultConstants.MEDIA_TYPE_APPLICATION_JSON);
    }
}
