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

    @Override
    public String verifyIDToken(String IDToken) throws GeneralSecurityException, IOException {
        return tokenVerificationService.verify(IDToken);
    }

    /**
     * Using TokenGenerationService generates tokens for given authCode
     * Validates user and authCode by comparing unique user Id received from IDToken and authCode
     *
     * @param IDToken  ( user IDToken for authentication )
     * @param authCode ( authentication code for generation of tokens )
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Override
    public UserToken generateToken(String IDToken, String authCode) throws GeneralSecurityException, IOException {
        String userID = tokenVerificationService.verify(IDToken);
        UserToken token = tokenGenerationService.generate(authCode);
        if (!userID.equals(token.getId())) throw new RuntimeException();
        return token;
    }

    @Override
    public void refreshToken(UserToken token) throws IOException {
        tokenRefreshService.refresh(token);
    }
}
