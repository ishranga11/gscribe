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

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.googleinterns.gscribe.models.UserToken;
import com.google.googleinterns.gscribe.resources.ExamResource;
import com.google.googleinterns.gscribe.services.TokenRefreshService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TokenRefreshImpl implements TokenRefreshService {

    /**
     * When the access Token expires then this method uses refreshToken to generate a new accessToken
     *
     * @param userToken ( contains refreshToken )
     * @throws IOException
     */
    @Override
    public void refresh(UserToken userToken) throws IOException {

        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        final String CREDENTIALS_FILE_PATH = "/credentials.json";
        InputStream in = ExamResource.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        String clientID = clientSecrets.getWeb().getClientId();
        String clientSecret = clientSecrets.getWeb().getClientSecret();
        TokenResponse response = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(), userToken.getRefreshToken(), clientID, clientSecret).execute();
        String accessToken = response.getAccessToken();
        userToken.setAccessToken(accessToken);

    }
}
