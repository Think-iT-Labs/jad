/*
 *  Copyright (c) 2025 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.virtualized.vault.hashicorp;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.MediaType;

import java.util.Map;

public interface VaultConstants {
    String VAULT_TOKEN_HEADER = "X-Vault-Token";
    String VAULT_SECRET_METADATA_PATH = "metadata";
    MediaType MEDIA_TYPE_APPLICATION_JSON = MediaType.get("application/json");
    String VAULT_URL = "edc.vault.hashicorp.url";
    String VAULT_TOKEN = "edc.vault.hashicorp.token";
    String VAULT_PATH = "edc.vault.hashicorp.api.secret.path";
    String VAULT_FOLDER = "edc.vault.hashicorp.folder";
}
