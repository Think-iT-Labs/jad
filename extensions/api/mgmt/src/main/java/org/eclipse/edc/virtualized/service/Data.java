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
                            .type("use")
                            .build())
                    .constraint(AtomicConstraint.Builder.newInstance()
                            .leftExpression(new LiteralExpression("MembershipCredential"))
                            .operator(Operator.EQ)
                            .rightExpression(new LiteralExpression("active"))
                            .build())
                    .build())
            .build();
}
