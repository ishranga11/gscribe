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

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.googleinterns.gscribe.resources.ExamResource;
import com.google.googleinterns.gscribe.services.TokenVerificationService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class TokenVerificationServiceImpl implements TokenVerificationService {

    /**
     * This function takes as input the IDToken passed in header for authentication
     * If the JWT clears all authentication checks then unique userID is extracted from the JWT and returned
     *
     * @param IDTokenString ( a JWT, web token signed by google )
     * @return userID ( unique user ID for the user included in JWT )
     * @throws GeneralSecurityException,IOException ( by google verifier, or reading credentials file errors )
     */
    @Override
    public String verify(String IDTokenString) throws GeneralSecurityException, IOException {
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
        return idToken.getPayload().getSubject();
    }

}
