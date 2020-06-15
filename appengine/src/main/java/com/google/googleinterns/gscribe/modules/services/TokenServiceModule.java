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

package com.google.googleinterns.gscribe.modules.services;

import com.google.googleinterns.gscribe.services.TokenGenerationService;
import com.google.googleinterns.gscribe.services.TokenService;
import com.google.googleinterns.gscribe.services.TokenVerificationService;
import com.google.googleinterns.gscribe.services.impl.TokenServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class TokenServiceModule extends AbstractModule {

    @Inject
    @Provides
    @Singleton
    public TokenService tokenServiceProvider(TokenGenerationService tokenGenerationService, TokenVerificationService tokenVerificationService) {
        return new TokenServiceImpl(tokenGenerationService, tokenVerificationService);
    }

}
