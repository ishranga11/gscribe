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

package com.google.googleinterns.gscribe.resources;

import com.google.googleinterns.gscribe.resources.io.request.AuthenticationRequest;
import com.google.googleinterns.gscribe.resources.io.response.AuthenticationResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/authenticate")
public class AuthenticationResource {


    /**
     * Get corresponding userID from the IDToken using tokenVerifier
     * With the help of tokenGenerator generate the tokens from authCode
     * Validate that userID from authCode is same as userID from IDToken in header
     * Save the tokens in the database
     *
     * @param IDToken ( from header )
     * @param request ( must contain authCode )
     * @return a response message if tokens are saved in database for the user
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AuthenticationResponse saveToken(@HeaderParam("authorization-code") String IDToken, AuthenticationRequest request) {
        return new AuthenticationResponse();
    }

    /**
     * Get corresponding userID from the IDToken using tokenVerifier
     * With corresponding userID check in database for tokens
     *
     * @param IDToken ( from header )
     * @return a response based on if tokens are present in database for current user
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AuthenticationResponse isTokenAvailable(@HeaderParam("authorization-code") String IDToken) {
        if (IDToken == null) throw new WebApplicationException("Bad call");
        else return new AuthenticationResponse();
    }


}
