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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.googleinterns.gscribe.services.TokenVerificationService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class TokenVerificationServiceImpl implements TokenVerificationService {

    /**
     * This function takes as input the IDToken passed in header for authentication
     * If the JWT clears all authentication checks then unique userID is extracted from the JWT and returned
     *
     * @param IDTokenString ( a JWT, web token signed by google )
     * @return userID ( unique user ID for the user included in JWT )
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Override
    public String verify(String IDTokenString) throws GeneralSecurityException, IOException {
        if (IDTokenString == null) {
            throw new RuntimeException();
        }
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
                .setAudience(Collections.singletonList("361993398276-n4dboc83jnellr02pkg0v8rh2rvlnqn6.apps.googleusercontent.com"))
                .build();
        GoogleIdToken idToken = verifier.verify(IDTokenString);
        if (idToken == null) {
            throw new RuntimeException();
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        return payload.getSubject();
    }

}
