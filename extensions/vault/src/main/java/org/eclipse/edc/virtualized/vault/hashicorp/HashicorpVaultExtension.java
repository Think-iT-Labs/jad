package org.eclipse.edc.virtualized.vault.hashicorp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.participantcontext.spi.config.ParticipantContextConfig;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.security.ParticipantVault;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.Optional.ofNullable;
import static org.eclipse.edc.virtualized.vault.hashicorp.HashicorpVaultExtension.NAME;
import static org.eclipse.edc.virtualized.vault.hashicorp.HashicorpVaultConfig.forParticipant;

@Extension(value = NAME)
public class HashicorpVaultExtension implements ServiceExtension {
    public static final String NAME = "Multi-tenant Hashicorp Vault Extension";

    @Inject
    private ParticipantContextConfig config;
    @Inject
    private Monitor monitor;
    @Inject
    private EdcHttpClient httpClient;
    @Inject
    private Vault singleTenantVault;

    @Override
    public String name() {
        return NAME;
    }

    @Provider
    public ParticipantVault createVault() {
        return new HashicorpParticipantVault(config, monitor, httpClient, new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false));
    }

    private final class HashicorpParticipantVault implements ParticipantVault {
        private final ParticipantContextConfig config;
        private final Monitor monitor;
        private final EdcHttpClient httpClient;
        private final ObjectMapper objectMapper;

        private HashicorpParticipantVault(ParticipantContextConfig config,
                                          Monitor monitor,
                                          EdcHttpClient httpClient,
                                          ObjectMapper objectMapper) {
            this.config = config;
            this.monitor = monitor;
            this.httpClient = httpClient;
            this.objectMapper = objectMapper;
        }

        @Override
        public String resolveSecret(String participantContextId, String key) {

            return ofNullable(participantContextId).map(pcId -> create(pcId).resolveSecret(key))
                    .orElseGet(() -> singleTenantVault.resolveSecret(key));
        }

        @Override
        public Result<Void> storeSecret(String participantContextId, String key, String value) {
            return ofNullable(participantContextId).map(pcId -> create(pcId).storeSecret(key, value))
                    .orElseGet(() -> singleTenantVault.storeSecret(key, value));
        }

        @Override
        public Result<Void> deleteSecret(String participantContextId, String key) {
            return ofNullable(participantContextId).map(pcId -> create(pcId).deleteSecret(key))
                    .orElseGet(() -> singleTenantVault.deleteSecret(key));
        }

        private HashicorpVault create(String participantContextId) {
            var settings = forParticipant(participantContextId, config);
            //todo: replace with upstream vault?
            return new HashicorpVault(monitor, settings, httpClient, objectMapper);
        }

    }
}
