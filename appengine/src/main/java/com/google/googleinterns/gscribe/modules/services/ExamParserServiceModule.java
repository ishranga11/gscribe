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

import com.google.googleinterns.gscribe.services.ExamGenerationService;
import com.google.googleinterns.gscribe.services.ExamParserService;
import com.google.googleinterns.gscribe.services.ExamSourceService;
import com.google.googleinterns.gscribe.services.ExamValidationService;
import com.google.googleinterns.gscribe.services.impl.ExamParserServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ExamParserServiceModule extends AbstractModule {

    @Inject
    @Provides
    @Singleton
    public ExamParserService examParserServiceProvider(ExamGenerationService examGenerationService, ExamValidationService examValidationService, ExamSourceService examSourceService) {
        return new ExamParserServiceImpl(examValidationService, examGenerationService, examSourceService);
    }

}
