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

import com.google.googleinterns.gscribe.models.UserToken;
import com.google.googleinterns.gscribe.services.TokenGenerationService;
import com.google.googleinterns.gscribe.services.TokenRefreshService;
import com.google.googleinterns.gscribe.services.TokenService;
import com.google.googleinterns.gscribe.services.TokenVerificationService;
import com.google.googleinterns.gscribe.services.data.TokenResponse;
import com.google.inject.Inject;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenServiceImpl implements TokenService {

    private final TokenGenerationService tokenGenerationService;
    private final TokenVerificationService tokenVerificationService;
    private final TokenRefreshService tokenRefreshService;

    @Inject
    public TokenServiceImpl(TokenGenerationService tokenGenerationService, TokenVerificationService tokenVerificationService, TokenRefreshService tokenRefreshService) {
        this.tokenGenerationService = tokenGenerationService;
        this.tokenVerificationService = tokenVerificationService;
        this.tokenRefreshService = tokenRefreshService;
    }

    /**
     * This function takes as input the IDToken passed in header for authentication
     * If the JWT clears all authentication checks then unique userID is extracted from the JWT and returned
     *
     * @param IDTokenString ( a JWT, web token signed by google )
     * @return userID ( unique user ID for the user included in JWT )
     * @throws GeneralSecurityException,IOException ( by google verifier, or reading credentials file errors )
     */
    @Override
    public String verifyIDToken(String IDTokenString) throws GeneralSecurityException, IOException {
        return tokenVerificationService.verify(IDTokenString);
    }

    /**
     * Take authentication code as an input
     * Uses credentials file allotted for the application to generate tokens for the given auth code
     *
     * @param authCode ( authentication code )
     * @return userToken object containing access token, refresh token ans unique user Id
     * @throws GeneralSecurityException,IOException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     */
    @Override
    public TokenResponse generateToken(String authCode) throws GeneralSecurityException, IOException {
        return tokenGenerationService.generate(authCode);
    }

    /**
     * When the access Token expires then this method uses refreshToken to generate a new accessToken
     *
     * @param userToken ( contains refreshToken )
     * @throws IOException ( if credentials file is not found or invalid )
     */
    @Override
    public void refreshToken(UserToken userToken) throws IOException {
        tokenRefreshService.refresh(userToken);
    }
}
