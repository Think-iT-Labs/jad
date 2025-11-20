package org.eclipse.edc.virtualized.vault.hashicorp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.participantcontext.spi.config.ParticipantContextConfig;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.ParticipantVault;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.eclipse.edc.virtualized.vault.hashicorp.HashicorpVaultExtension.NAME;

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
        return new HashicorpParticipantVault(config, monitor, httpClient, new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false), singleTenantVault);
    }

}
