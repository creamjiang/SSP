/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.ssp;

import org.junit.Test;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ServerIT extends AbstractIntegrationTest {

    @Test
    public void testServerVersion() {
        final Long expectedBuildDate = Long.parseLong(System.getProperty("buildDate").trim());
        final String expectedArtifactVersion = System.getProperty("artifactVersion");
        final String expectedSCMRevision = System.getProperty("scmRevision");

        expect()
        .statusCode(200)
        .body("artifact", equalTo("org.jasig.ssp:ssp"),
                "artifactVersion", equalTo(expectedArtifactVersion),
                "name", equalTo("SSP"),
                "scmRevision", equalTo(expectedSCMRevision),
                "buildDate", lessThan(expectedBuildDate))
        .when().get("server/version");
    }

    @Test
    public void testServerDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        expect()
        .statusCode(200)
        .body("timestamp", greaterThan(new Date().getTime()),
                "date", equalTo(df.format(new Date())))
        .when().get("server/datetime");
    }
}