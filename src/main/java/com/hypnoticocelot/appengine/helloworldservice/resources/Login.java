package com.hypnoticocelot.appengine.helloworldservice.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/login")
public class Login {

    @GET
    public String getter ( ){
        return "Hello from login";
    }

}
