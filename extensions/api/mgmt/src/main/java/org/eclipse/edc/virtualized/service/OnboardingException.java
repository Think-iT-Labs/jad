/*
 *  Copyright (c) 2025 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

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
