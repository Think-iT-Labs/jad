package org.eclipse.edc.virtualized.vault.hashicorp;

import org.eclipse.edc.participantcontext.spi.config.ParticipantContextConfig;

import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_FOLDER;
import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_PATH;
import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_TOKEN;
import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_URL;

public record HashicorpVaultSettings(String vaultToken, String secretPath, String vaultUrl, String folderPath) {

    public static HashicorpVaultSettings forParticipant(String participantContextId, ParticipantContextConfig config) {
        var url = config.getString(participantContextId, VAULT_URL, null);
        var secrets = config.getString(participantContextId, VAULT_PATH, null);
        var token = config.getString(participantContextId, VAULT_TOKEN, null);
        var folder = config.getString(participantContextId, VAULT_FOLDER, null);

        return new HashicorpVaultSettings(token, secrets, url, folder);
    }

}
