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

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.googleinterns.gscribe.resources.ExamResource;
import com.google.googleinterns.gscribe.services.TokenGenerationService;
import com.google.googleinterns.gscribe.services.data.TokenResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class TokenGenerationServiceImpl implements TokenGenerationService {

    /**
     * Take authentication code as an input
     * Uses credentials file allotted for the application to generate tokens for the given auth code
     *
     * @param authCode ( authentication code )
     * @return userToken object containing access token, refresh token ans unique user Id
     * @throws GeneralSecurityException,IOException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     */
    @Override
    public TokenResponse generate(String authCode) throws GeneralSecurityException, IOException {
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
        GoogleTokenResponse tokenResponse = tokenRequest.execute();
        return new TokenResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), tokenResponse.getIdToken());
    }

}
