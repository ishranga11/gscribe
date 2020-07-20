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

import com.google.googleinterns.gscribe.dao.UserTokenDao;
import com.google.googleinterns.gscribe.models.User;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;
import com.google.googleinterns.gscribe.resources.io.request.AuthenticationRequest;
import com.google.googleinterns.gscribe.resources.io.response.AuthenticationResponse;
import com.google.googleinterns.gscribe.services.TokenService;
import com.google.googleinterns.gscribe.services.data.TokenResponse;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Path("/authenticate")
public class AuthenticationResource {

    private final TokenService tokenService;
    private final UserTokenDao userTokenDao;

    @Inject
    public AuthenticationResource(TokenService tokenService, UserTokenDao userTokenDao) {
        this.tokenService = tokenService;
        this.userTokenDao = userTokenDao;
    }

    /**
     * Called to generate and save tokens for the user ( paper setter ) in the database
     * Get corresponding userID from the IDToken using tokenVerifier
     * With the help of tokenGenerator generate the tokens from authCode
     * Validate that userID from authCode is same as userID from IDToken in header
     * Save the tokens in the database
     *
     * @param IDToken ( from header )
     * @param request ( contains authCode )
     * @return a response message if tokens are saved in database for the user
     * @throws BadRequestException          ( if authCode is invalid or not present in the request,
     *                                      if IDToken is not invalid in header,
     *                                      if IDToken is invalid received from authCode token request,
     *                                      if userID from both IDToken from header and authCode token request are different )
     * @throws InternalServerErrorException ( by GeneralSecurityException and IOException for credentials file )
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AuthenticationResponse saveToken(@NotNull @HeaderParam("Authentication") String IDToken, @NotNull AuthenticationRequest request) {
        User token;
        TokenResponse tokenResponse;
        String userID, tokenResponseUserID;

        if (request.getAuthCode() == null)
            throw new BadRequestException("Missing request parameters");

        try {
            userID = tokenService.verifyIDToken(IDToken);
            tokenResponse = tokenService.generateToken(request.getAuthCode());
            tokenResponseUserID = tokenService.verifyIDToken(tokenResponse.getIDToken());
        } catch (GeneralSecurityException | IOException e) {
            throw new InternalServerErrorException();
        } catch (InvalidRequestException e) {
            throw new BadRequestException(e.getMessage());
        }

        if (!userID.equals(tokenResponseUserID))
            throw new BadRequestException("Malformed request.");

        token = new User(userID, tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), null);
        userTokenDao.insertUserToken(token);
        return new AuthenticationResponse("User Authorized");
    }

    /**
     * Called to check if tokens are present for user in the database
     * Get corresponding userID from the IDToken using tokenVerifier
     * With corresponding userID check in database for tokens
     *
     * @param IDToken ( from header )
     * @return a success response containing message that user is authorized
     * @throws BadRequestException          ( if IDToken is invalid )
     * @throws InternalServerErrorException ( IOException or GeneralSecurityException due to credentials file )
     * @throws NotAuthorizedException       ( if user is not found in the database )
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AuthenticationResponse isTokenAvailable(@NotNull @HeaderParam("Authentication") String IDToken) {
        String userID;
        User token;

        try {
            userID = tokenService.verifyIDToken(IDToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new InternalServerErrorException();
        } catch (InvalidRequestException e) {
            throw new BadRequestException(e.getMessage());
        }

        token = userTokenDao.getUserToken(userID);
        if (token == null) throw new NotAuthorizedException("Authentication Failed");
        return new AuthenticationResponse("User is Authorized");
    }


}
