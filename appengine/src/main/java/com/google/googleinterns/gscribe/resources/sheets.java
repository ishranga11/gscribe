package com.google.googleinterns.gscribe.resources;

import com.google.googleinterns.gscribe.model.SheetsQuickstart;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Path("/sheet")
public class sheets {

    @GET
    public String callSheets() throws IOException, GeneralSecurityException {
        SheetsQuickstart caller = new SheetsQuickstart();
        caller.callSheets();
        return "DONE";
    }

}
