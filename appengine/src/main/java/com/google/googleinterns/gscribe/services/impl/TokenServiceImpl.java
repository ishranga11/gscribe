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

package com.google.googleinterns.gscribe.services.impl;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.googleinterns.gscribe.models.User;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidDatabaseDataException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;
import com.google.googleinterns.gscribe.services.TokenService;
import com.google.googleinterns.gscribe.services.data.TokenResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class TokenServiceImpl implements TokenService {

    private final GoogleClientSecrets clientSecrets;
    private final NetHttpTransport HTTP_TRANSPORT;

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public TokenServiceImpl(GoogleClientSecrets clientSecrets, NetHttpTransport http_transport) {
        this.clientSecrets = clientSecrets;
        HTTP_TRANSPORT = http_transport;
    }

    /**
     * Called to verify IDToken received from paper setter
     * This function takes as input the IDToken passed in header for authentication
     * If the JWT clears all authentication checks then unique userID is extracted from the JWT and returned
     *
     * @param IDTokenString ( a JWT, web token signed by google )
     * @return userID ( unique user ID for the user included in JWT )
     * @throws GeneralSecurityException,IOException ( thrown by GoogleTokenVerifier verify function  )
     * @throws InvalidRequestException              ( if the verification fails then returned token is null, then throw this exception )
     */
    @Override
    public String verifyIDToken(String IDTokenString) throws GeneralSecurityException, IOException, InvalidRequestException {

        String clientID = clientSecrets.getWeb().getClientId();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY).setAudience(Collections.singletonList(clientID)).build();
        GoogleIdToken idToken = verifier.verify(IDTokenString);
        if (idToken == null) throw new InvalidRequestException("Authentication failed");
        return idToken.getPayload().getSubject();
    }

    /**
     * Called to verify IDToken received from firebase functions
     * As this function was originally intended for firebase function client so client id is added for that service
     * This function takes as input the IDToken passed in header for authentication
     * If the JWT clears all authentication checks then unique userID is extracted from the JWT and returned
     *
     * @param IDTokenString ( a JWT, web token signed by google )
     * @return userID ( unique user ID for the user included in JWT )
     * @throws GeneralSecurityException,IOException ( thrown by GoogleTokenVerifier verify function  )
     * @throws InvalidRequestException              ( if the verification fails then returned token is null, If we receive null token then return this exception )
     */
    @Override
    public String firebaseVerifyIDToken(String IDTokenString) throws GeneralSecurityException, IOException, InvalidRequestException {

        String clientID = "201502787341-rqsisrvv0givo5agv86p44e2hjui05or.apps.googleusercontent.com";
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY).setAudience(Collections.singletonList(clientID)).build();
        GoogleIdToken idToken = verifier.verify(IDTokenString);
        if (idToken == null) throw new InvalidRequestException("Authentication failed");
        return idToken.getPayload().getSubject();
    }

    /**
     * Called when user is being authorized so new tokens are generated from authorization code
     * Generates new accessToken and refreshToken from authorization code
     * Needs credentials file to generate the tokens
     *
     * @param authCode ( authentication code )
     * @return userToken object containing access token, refresh token and unique user Id
     * @throws IOException             ( thrown by GoogleTokenResponse execute function )
     * @throws InvalidRequestException ( if the authorization code is invalid )
     */
    @Override
    public TokenResponse generateToken(String authCode) throws IOException, InvalidRequestException {

        final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setAccessType("offline").build();
        GoogleAuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(authCode);
        tokenRequest.setRedirectUri(clientSecrets.getWeb().getRedirectUris().get(0));
        try {
            GoogleTokenResponse GoogleTokenResponse = tokenRequest.execute();
            return new TokenResponse(GoogleTokenResponse.getAccessToken(), GoogleTokenResponse.getRefreshToken(), GoogleTokenResponse.getIdToken());
        } catch (TokenResponseException e) {
            throw new InvalidRequestException("Authorization failed");
        }
    }

    /**
     * Called when access token expires so new access token is required
     * The user tokens are retrieved from database and refreshed if the access token expires
     * After getting a GoogleTokenResponse for validation we check that this refreshToken is valid for the main user
     * For validation verify the IDToken from the token received from refreshToken to get a userID
     * Compare both userIDs of main request and refreshToken
     *
     * @param user ( contains refreshToken )
     * @throws IOException,GeneralSecurityException ( thrown by GoogleTokenVerifier verify function  )
     * @throws InvalidDatabaseDataException         ( if the userID retrieved from refreshing the access token is different than the actual userID,
     *                                              if the IDToken retrieved from refreshing the token is not verified by googleVerifier
     *                                              if the refresh token is not correct and TokenRequestException is received )
     */
    @Override
    public void refreshToken(User user) throws IOException, InvalidDatabaseDataException, GeneralSecurityException {

        String clientID = clientSecrets.getWeb().getClientId();
        String clientSecret = clientSecrets.getWeb().getClientSecret();
        try {
            GoogleTokenResponse response = new GoogleRefreshTokenRequest(HTTP_TRANSPORT, JSON_FACTORY, user.getRefreshToken(), clientID, clientSecret).execute();
            String accessToken = response.getAccessToken();
            String responseUserID = verifyIDToken(response.getIdToken());
            if (!user.getId().equals(responseUserID)) {
                throw new InvalidDatabaseDataException("User credentials incorrect");
            }
            user.setAccessToken(accessToken);
        } catch (TokenResponseException | InvalidRequestException e) {
            throw new InvalidDatabaseDataException("Invalid user refresh token");
        }
    }
}
