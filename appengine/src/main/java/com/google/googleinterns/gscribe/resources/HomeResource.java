package com.google.googleinterns.gscribe.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class HomeResource {

  @GET
  public String get() {
    return "Hello, World!";
  }
}
