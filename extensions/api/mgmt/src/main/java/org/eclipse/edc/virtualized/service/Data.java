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

import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;

public class Data {
    public static final Policy MEMBERSHIP_POLICY = Policy.Builder.newInstance()
            .permission(Permission.Builder.newInstance()
                    .action(Action.Builder.newInstance()
                            .type("http://www.w3.org/ns/odrl/2/use")
                            .build())
                    .constraint(AtomicConstraint.Builder.newInstance()
                            .leftExpression(new LiteralExpression("MembershipCredential"))
                            .operator(Operator.EQ)
                            .rightExpression(new LiteralExpression("active"))
                            .build())
                    .build())
            .build();
}
