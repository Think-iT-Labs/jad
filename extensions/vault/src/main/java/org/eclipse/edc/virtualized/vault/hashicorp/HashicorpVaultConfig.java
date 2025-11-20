package org.eclipse.edc.virtualized.vault.hashicorp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.participantcontext.spi.config.ParticipantContextConfig;
import org.eclipse.edc.spi.EdcException;
import org.jetbrains.annotations.Nullable;

import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_CONFIG;

public record HashicorpVaultConfig(HashicorpVaultCredentials credentials, String vaultUrl, String secretsPath, @Nullable String folderPath) {

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
