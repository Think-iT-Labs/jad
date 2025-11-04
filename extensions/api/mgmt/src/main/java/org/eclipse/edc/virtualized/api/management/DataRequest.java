package org.eclipse.edc.virtualized.api.management;

public record DataRequest(
        String providerId,
        String policyId,
        String assetId
) {
}
