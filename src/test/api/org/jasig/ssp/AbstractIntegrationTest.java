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

import com.jayway.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;

public abstract class AbstractIntegrationTest {

    private static String baseURI = "http://localhost";
    private static int port = 8080;
    private static String basePath = "/ssp/api/1";
    private static Boolean propertiesLoaded = false;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractIntegrationTest.class);

    private static void loadProperties() {

        try {
            String inputBaseURL = System.getProperty("apiURL");
            String inputPort = System.getProperty("apiPORT");

            if ( org.apache.commons.lang.StringUtils.isNotBlank(inputBaseURL) ) {
                baseURI = inputBaseURL;
            }
            if ( org.apache.commons.lang.StringUtils.isNotBlank(inputPort) ) {
                port = Integer.parseInt(inputPort);
            }
        }
        catch (NumberFormatException n) {
            LOGGER.error("Error loading port, not a number! Using default: " + port +".");
        }
        finally {
            propertiesLoaded = true;
        }

        LOGGER.info("Base URI = " + baseURI);
        LOGGER.info("Port = " + port);
        LOGGER.info("Base Path = " + basePath +"\n");
    }

    @Before
    public final void setup() {

        if ( propertiesLoaded == false ) {
            loadProperties();
        }

        RestAssured.baseURI = baseURI;
        RestAssured.port = port;
        RestAssured.basePath = basePath;
    }
}
