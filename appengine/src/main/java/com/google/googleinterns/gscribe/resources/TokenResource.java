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

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/token")
public class TokenResource {

    @POST
     /*
    Input - accessCode, IDToken
    Function - get ( accessToken, refreshToken, userID ) from accessCode
    Output - return Status if accessToken and refreshToken was received
     */
    public String saveToken(String accessCode) {
        return "Done";
    }

    @POST
    @Path("/present")
     /*
    Input - IDToken
    Function - get userID from IDToken and check if accessToken, refreshToken for user are present in database
    Output - return if accessToken and refreshToken present in database for user
     */
    public String tokenPresentForUser(String IDToken) {
        return "Needed";
    }

}
