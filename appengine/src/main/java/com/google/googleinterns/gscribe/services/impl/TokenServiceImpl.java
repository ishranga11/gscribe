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
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.googleinterns.gscribe.models.User;
import com.google.googleinterns.gscribe.resources.ExamResource;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidDatabaseDataException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;
import com.google.googleinterns.gscribe.services.TokenService;
import com.google.googleinterns.gscribe.services.data.TokenResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class TokenServiceImpl implements TokenService {

    /**
     * Called to verify IDToken received from paper setter
     * This function takes as input the IDToken passed in header for authentication
     * If the JWT clears all authentication checks then unique userID is extracted from the JWT and returned
     *
     * @param IDTokenString ( a JWT, web token signed by google )
     * @return userID ( unique user ID for the user included in JWT )
     * @throws GeneralSecurityException,IOException ( by google verifier, or reading credentials file errors )
     * @throws InvalidRequestException              ( if the verification fails then returned token is null, If we receive null token then return this exception )
     */
    @Override
    public String verifyIDToken(String IDTokenString) throws GeneralSecurityException, IOException, InvalidRequestException {
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        final String CREDENTIALS_FILE_PATH = "/credentials.json";
        InputStream in = ExamResource.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        String clientID = clientSecrets.getWeb().getClientId();

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY).setAudience(Collections.singletonList(clientID)).build();
        GoogleIdToken idToken = verifier.verify(IDTokenString);
        if (idToken == null) throw new InvalidRequestException("Authentication failed");
        return idToken.getPayload().getSubject();
    }

    /**
     * Called to verify IDToken received from examinee
     * As this function was originally intended for firebase function client so client id is added for that service
     * This function takes as input the IDToken passed in header for authentication
     * If the JWT clears all authentication checks then unique userID is extracted from the JWT and returned
     *
     * @param IDTokenString ( a JWT, web token signed by google )
     * @return userID ( unique user ID for the user included in JWT )
     * @throws GeneralSecurityException,IOException ( by google verifier, or reading credentials file errors )
     * @throws InvalidRequestException              ( if the verification fails then returned token is null, If we receive null token then return this exception )
     */
    @Override
    public String firebaseVerifyIDToken(String IDTokenString) throws GeneralSecurityException, IOException, InvalidRequestException {
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        String clientID = "201502787341-rqsisrvv0givo5agv86p44e2hjui05or.apps.googleusercontent.com";

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY).setAudience(Collections.singletonList(clientID)).build();
        GoogleIdToken idToken = verifier.verify(IDTokenString);
        if (idToken == null) throw new InvalidRequestException("Authentication failed");
        return idToken.getPayload().getSubject();
    }

    /**
     * Called when user is being authorized so tokens are generated from authorization code
     * Generates new accessToken and refreshToken from authorization code
     * Needs credentials file to generate the tokens
     *
     * @param authCode ( authentication code )
     * @return userToken object containing access token, refresh token ans unique user Id
     * @throws GeneralSecurityException,IOException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     * @throws InvalidRequestException              ( if the authorization code is invalid )
     */
    @Override
    public TokenResponse generateToken(String authCode) throws GeneralSecurityException, IOException, InvalidRequestException {
        TokenResponse tokenResponse;
        try {
            final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
            final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
            final String CREDENTIALS_FILE_PATH = "/credentials.json";
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            InputStream in = ExamResource.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setAccessType("offline").build();
            GoogleAuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(authCode);
            tokenRequest.setRedirectUri(clientSecrets.getWeb().getRedirectUris().get(0));
            GoogleTokenResponse GoogleTokenResponse = tokenRequest.execute();
            tokenResponse = new TokenResponse(GoogleTokenResponse.getAccessToken(), GoogleTokenResponse.getRefreshToken(), GoogleTokenResponse.getIdToken());
        } catch (TokenResponseException e) {
            throw new InvalidRequestException("Authorization failed");
        }
        return tokenResponse;
    }

    /**
     * Called when access token expires so new access token is required
     * The user tokens are retrieved from database and refreshed if the access token expires
     * After getting a GoogleTokenResponse for validation we check that this refreshToken is valid for the main user
     * For validation verify the IDToken from the token received from refreshToken to get a userID
     * Compare both userIDs of main request and refreshToken
     *
     * @param user ( contains refreshToken )
     * @throws IOException,GeneralSecurityException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     * @throws InvalidDatabaseDataException         ( if the userID retrieved from refreshing the access token is different than the actual userID,
     *                                              if the IDToken retrieved from refreshing the token is not verified by googleVerifier
     *                                              if the refresh token is not correct and TokenRequestException is received )
     */
    @Override
    public void refreshToken(User user) throws IOException, InvalidDatabaseDataException, GeneralSecurityException {
        try {
            final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
            final String CREDENTIALS_FILE_PATH = "/credentials.json";
            InputStream in = TokenServiceImpl.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            String clientID = clientSecrets.getWeb().getClientId();
            String clientSecret = clientSecrets.getWeb().getClientSecret();
            GoogleTokenResponse response = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(), user.getRefreshToken(), clientID, clientSecret).execute();
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
