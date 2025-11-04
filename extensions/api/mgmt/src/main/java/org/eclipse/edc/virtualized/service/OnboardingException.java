package org.eclipse.edc.virtualized.service;

import org.eclipse.edc.spi.result.Failure;
import org.eclipse.edc.spi.result.ServiceFailure;

public class OnboardingException extends RuntimeException {
    private final ServiceFailure failure;

    public OnboardingException(ServiceFailure f) {
        super(f.getFailureDetail());
        this.failure = f;
    }

    public ServiceFailure getFailure() {
        return failure;
    }
}
