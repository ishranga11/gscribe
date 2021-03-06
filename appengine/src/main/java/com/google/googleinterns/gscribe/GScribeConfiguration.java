/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.googleinterns.gscribe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.googleinterns.gscribe.config.MySQLConfig;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class GScribeConfiguration extends Configuration {

    /**
     * Reads the database configuration from the yaml file
     * Needs - username, password, url, driver class
     */
    @Valid
    @NotNull
    private MySQLConfig mySQLConfig;

    @Valid
    @NotNull
    private Environment environment;

    @JsonProperty("database")
    public MySQLConfig getMySQLConfig() {
        return mySQLConfig;
    }

    @JsonProperty("database")
    public void setMySQLConfig(MySQLConfig mySQLConfig) {
        this.mySQLConfig = mySQLConfig;
    }


    public Environment getEnvironment() {
        return environment;
    }

    @JsonProperty("environment")
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
