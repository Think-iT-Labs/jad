package org.eclipse.edc.virtualized.service;

import org.eclipse.edc.connector.controlplane.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.controlplane.contract.spi.types.negotiation.ContractNegotiationStates;
import org.eclipse.edc.connector.controlplane.contract.spi.types.negotiation.ContractRequest;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.connector.controlplane.services.spi.contractnegotiation.ContractNegotiationService;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcess;
import org.eclipse.edc.iam.did.spi.document.Service;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.participantcontext.spi.types.ParticipantContext;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.virtualized.api.management.DataRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataRequestService {

    private final ContractNegotiationService contractNegotiationService;
    private final DidResolverRegistry didResolverRegistry;

    public DataRequestService(ContractNegotiationService contractNegotiationService, DidResolverRegistry didResolverRegistry) {
        this.contractNegotiationService = contractNegotiationService;
        this.didResolverRegistry = didResolverRegistry;
    }

    public CompletableFuture<ServiceResult<Object>> getData(ParticipantContext participantContext, DataRequest dataRequest) {
        return initiateContractNegotiation(participantContext, dataRequest)
                .thenCompose(this::waitForContractNegotiation)
                .thenCompose(contractNegotiation -> startTransferProcess(contractNegotiation.getId()))
                .thenCompose(this::waitForTransferProcess)
                .thenCompose(transferProcess -> getEdrs())
                .thenCompose(edrs -> getEdr("REPLACEME"))
                .thenCompose(this::downloadData)
                .thenApply(transferProcess -> ServiceResult.success("Success"));
    }

    private CompletableFuture<String> initiateContractNegotiation(ParticipantContext participantContext, DataRequest dataRequest) {
        var addressForDid = getAddressForDid(dataRequest.providerId());
        if (addressForDid.failed()) {
            return CompletableFuture.failedFuture(new RuntimeException("Could not resolve address for did: %s".formatted(addressForDid.getFailureDetail())));
        }
        var rq = ContractRequest.Builder.newInstance()
                .protocol("dataspace-protocol-http:2025-1")
                .counterPartyAddress(addressForDid.getContent())
                .contractOffer(ContractOffer.Builder.newInstance()
                        .id(dataRequest.policyId())
                        .assetId(dataRequest.assetId())
                        .policy(Data.MEMBERSHIP_POLICY.toBuilder()
                                .target(dataRequest.assetId())
                                .assigner(dataRequest.providerId())
                                .build())
                        .build())
                .build();
        var negotiation = contractNegotiationService.initiateNegotiation(participantContext, rq);

        return CompletableFuture.completedFuture(negotiation.getId());
    }

    private Result<String> getAddressForDid(String counterPartyDid) {
        var did = didResolverRegistry.resolve(counterPartyDid);
        if (did.failed()) {
            return did.mapFailure();
        }

        var doc = did.getContent();

        var protocolEndpoint = doc.getService().stream().filter(s -> s.getType().equals("ProtocolEndpoint")).findFirst();
        return Result.from(protocolEndpoint).map(Service::getServiceEndpoint);
    }

    private CompletableFuture<ContractAgreement> waitForContractNegotiation(String contractNegotiationId) {

        try {
            ContractNegotiationStates state;
            do {
                state = ContractNegotiationStates.valueOf(contractNegotiationService.getState(contractNegotiationId));
                Thread.sleep(1000);
            } while (state != ContractNegotiationStates.FINALIZED && state != ContractNegotiationStates.TERMINATED);

            if (state == ContractNegotiationStates.TERMINATED) {
                return CompletableFuture.failedFuture(new RuntimeException("Contract negotiation terminated"));
            }

            return CompletableFuture.completedFuture(contractNegotiationService.getForNegotiation(contractNegotiationId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<String> startTransferProcess(String contractNegotiationId) {
        return CompletableFuture.completedFuture("transfer-process-id");
    }

    private CompletableFuture<TransferProcess> waitForTransferProcess(String transferProcessId) {
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<List<DataAddress>> getEdrs() {
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<DataAddress> getEdr(String transferProcessId) {
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Object> downloadData(DataAddress edr) {
        return CompletableFuture.completedFuture(null);
    }
}
