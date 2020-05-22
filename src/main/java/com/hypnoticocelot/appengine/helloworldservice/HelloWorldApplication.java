//Copyright 2020 Google LLC
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//https://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.hypnoticocelot.appengine.helloworldservice;

import com.codahale.metrics.servlets.HealthCheckServlet;
import com.hypnoticocelot.appengine.helloworldservice.resources.HomeResource;
import com.hypnoticocelot.appengine.helloworldservice.resources.Login;
import io.dropwizard.Application;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

  public static void main(String[] args) throws Exception {
    new HelloWorldApplication().run(args);
  }

  @Override
  public String getName() {
    return "HelloWorld";
  }

  @Override
  public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
  }

  public void run(HelloWorldConfiguration configuration, Environment environment) throws Exception {
    environment.jersey().register(new HomeResource());
    environment.jersey().register(new Login());
    environment.servlets()
        .addServlet("healthcheck", new HealthCheckServlet(environment.healthChecks()))
        .addMapping("/_ah/health");
  }
}
