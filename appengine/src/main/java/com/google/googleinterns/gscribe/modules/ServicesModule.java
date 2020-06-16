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

package com.google.googleinterns.gscribe.modules;

import com.google.googleinterns.gscribe.services.*;
import com.google.googleinterns.gscribe.services.impl.*;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ServicesModule extends AbstractModule {

    @Provides
    @Singleton
    ExamGenerationService examGenerationServiceProvider() {
        return new ExamGenerationServiceImpl();
    }

    @Provides
    @Singleton
    ExamValidationService examValidationServiceProvider() {
        return new ExamValidationServiceImpl();
    }

    @Provides
    @Singleton
    ExamSourceService examSourceServiceProvider() {
        return new ExamSourceServiceImpl();
    }

    @Inject
    @Provides
    @Singleton
    public ExamParserService examParserServiceProvider(ExamGenerationService examGenerationService, ExamValidationService examValidationService, ExamSourceService examSourceService) {
        return new ExamParserServiceImpl(examValidationService, examGenerationService, examSourceService);
    }

    @Inject
    @Provides
    @Singleton
    TokenGenerationService tokenGenerationServiceProvider(TokenVerificationService tokenVerificationService) {
        return new TokenGenerationServiceImpl(tokenVerificationService);
    }

    @Provides
    @Singleton
    TokenVerificationService tokenVerificationServiceProvider() {
        return new TokenVerificationServiceImpl();
    }

    @Provides
    @Singleton
    TokenRefreshService tokenRefreshServiceProvider() {
        return new TokenRefreshImpl();
    }

    @Inject
    @Provides
    @Singleton
    public TokenService tokenServiceProvider(TokenGenerationService tokenGenerationService, TokenVerificationService tokenVerificationService, TokenRefreshService tokenRefreshService) {
        return new TokenServiceImpl(tokenGenerationService, tokenVerificationService, tokenRefreshService);
    }

}
