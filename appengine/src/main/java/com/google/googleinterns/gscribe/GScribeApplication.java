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

import com.codahale.metrics.servlets.HealthCheckServlet;
import com.google.googleinterns.gscribe.modules.*;
import com.google.googleinterns.gscribe.resources.AuthenticationResource;
import com.google.googleinterns.gscribe.resources.ExamResource;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class GScribeApplication extends Application<GScribeConfiguration> {

    public static void main(String[] args) throws Exception {
        new GScribeApplication().run(args);
    }

    @Override
    public String getName() {
        return "GScribeApplication";
    }

    @Override
    public void initialize(Bootstrap<GScribeConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
        bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html"));
    }

    public void run(GScribeConfiguration configuration, Environment environment) {
        Injector injector = Guice.createInjector(new DBConnectorModule(configuration.getMySQLConfig()), new DaoModule(), new ServicesModule(), new ObjectMapperModule(), new GoogleClientSecretsModule());
        environment.jersey().register(injector.getInstance(AuthenticationResource.class));
        environment.jersey().register(injector.getInstance(ExamResource.class));
        environment.jersey().setUrlPattern("/api/*");
        environment.servlets()
                .addServlet("healthcheck", new HealthCheckServlet(environment.healthChecks()))
                .addMapping("/_ah/health");
    }
}
