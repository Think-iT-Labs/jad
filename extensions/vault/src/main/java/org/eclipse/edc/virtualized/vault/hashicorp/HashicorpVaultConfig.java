package org.eclipse.edc.virtualized.vault.hashicorp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.participantcontext.spi.config.ParticipantContextConfig;
import org.eclipse.edc.spi.EdcException;
import org.jetbrains.annotations.Nullable;

import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_CONFIG;

/**
 * POJO that contains Hashicorp Vault configuration.
 *
 * @param credentials credentials object, see {@link HashicorpVaultCredentials}
 * @param vaultUrl The base URL of the Hashicorp Vault instance.
 * @param secretsPath the path of the secret engine. Usually prefixed with "v1", so if you created a secret engine called "my-engine", the path would be "v1/my-engine".
 * @param folderPath an optional path within the secrets engine where the secrets are stored.
 */
public record HashicorpVaultConfig(HashicorpVaultCredentials credentials, String vaultUrl, String secretsPath,
                                   @Nullable String folderPath) {

    private static final ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static HashicorpVaultConfig forParticipant(String participantContextId, ParticipantContextConfig config) {
        var vaultConfigJson = config.getString(participantContextId, VAULT_CONFIG);
        try {
            return mapper.readValue(vaultConfigJson, HashicorpVaultConfig.class);
        } catch (JsonProcessingException e) {
            throw new EdcException(e);
        }
    }
}
