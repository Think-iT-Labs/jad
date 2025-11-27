/*
 *  Copyright (c) 2022 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Initial API and Implementation
 *       Cofinity-X - implement extensible authentication
 *
 */

plugins {
    `java-library`
}

dependencies {
//    api(project(":spi:common:core-spi"))
    api(libs.edc.spi.core)
//    api(project(":spi:common:http-spi"))
    api(libs.edc.spi.http)
//    api(project(":spi:common:vault-hashicorp-spi"))
    api(libs.edc.spi.hashicorp)
//    api(project(":spi:common:participant-context-config-spi"))
    api(libs.edc.spi.participantcontext.config)

//    implementation(project(":core:common:lib:util-lib"))
    implementation(libs.edc.lib.util)

//    testImplementation(project(":core:common:runtime-core"))
//    testImplementation(testFixtures(project(":core:common:lib:http-lib")))
//    testImplementation(project(":core:common:junit"))
    testImplementation(libs.edc.junit)
    testImplementation(testFixtures(libs.edc.lib.http))
    testImplementation(libs.jakarta.json.api)
    testImplementation(libs.wiremock)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.vault)
    implementation(libs.bouncyCastle.bcpkixJdk18on)
}
