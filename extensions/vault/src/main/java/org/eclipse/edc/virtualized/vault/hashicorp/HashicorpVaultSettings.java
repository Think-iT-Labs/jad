package org.eclipse.edc.virtualized.vault.hashicorp;

import org.eclipse.edc.participantcontext.spi.config.ParticipantContextConfig;

import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_FOLDER;
import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_PASSWORD;
import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_PATH;
import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_TOKEN;
import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_URL;
import static org.eclipse.edc.virtualized.vault.hashicorp.VaultConstants.VAULT_USER;

public final class HashicorpVaultSettings {
    private String vaultToken;
    private String secretPath;
    private String vaultUrl;
    private String folderPath;
    private String username;
    private String password;


    public static HashicorpVaultSettings forParticipant(String participantContextId, ParticipantContextConfig config) {
        var url = config.getString(participantContextId, VAULT_URL, null);
        var secrets = config.getString(participantContextId, VAULT_PATH, null);
        var token = config.getString(participantContextId, VAULT_TOKEN, null);
        var user = config.getString(participantContextId, VAULT_USER, null);
        var pass = config.getString(participantContextId, VAULT_PASSWORD, null);
        var folder = config.getString(participantContextId, VAULT_FOLDER, null);

        return HashicorpVaultSettings.Builder.newInstance()
                .vaultUrl(url)
                .secretPath(secrets)
                .vaultToken(token)
                .userName(user)
                .password(pass)
                .folderPath(folder)
                .build();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getVaultToken() {
        return vaultToken;
    }

    public String getSecretPath() {
        return secretPath;
    }

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public static final class Builder {

        private final HashicorpVaultSettings settings;

        private Builder() {
            settings = new HashicorpVaultSettings();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder vaultToken(String vaultToken) {
            this.settings.vaultToken = vaultToken;
            return this;
        }

        public Builder secretPath(String secretPath) {
            this.settings.secretPath = secretPath;
            return this;
        }

        public Builder vaultUrl(String vaultUrl) {
            this.settings.vaultUrl = vaultUrl;
            return this;
        }

        public Builder folderPath(String folderPath) {
            this.settings.folderPath = folderPath;
            return this;
        }

        public Builder userName(String userName) {
            this.settings.username = userName;
            return this;
        }

        public Builder password(String password) {
            this.settings.password = password;
            return this;
        }

        public HashicorpVaultSettings build() {
            return settings;
        }
    }
}
