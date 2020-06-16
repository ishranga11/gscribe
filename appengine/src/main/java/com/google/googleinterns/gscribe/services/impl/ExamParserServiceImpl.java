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

import com.google.googleinterns.gscribe.models.Exam;
import com.google.googleinterns.gscribe.models.UserToken;
import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;
import com.google.googleinterns.gscribe.services.ExamGenerationService;
import com.google.googleinterns.gscribe.services.ExamParserService;
import com.google.googleinterns.gscribe.services.ExamSourceService;
import com.google.googleinterns.gscribe.services.ExamValidationService;
import com.google.googleinterns.gscribe.services.data.ExamSource;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ExamParserServiceImpl implements ExamParserService {

    private final ExamValidationService examValidationService;
    private final ExamGenerationService examGenerationService;
    private final ExamSourceService examSourceService;

    public ExamParserServiceImpl(ExamValidationService examValidationService, ExamGenerationService examGenerationService, ExamSourceService examSourceService) {
        this.examGenerationService = examGenerationService;
        this.examValidationService = examValidationService;
        this.examSourceService = examSourceService;
    }

    @Override
    public ExamSource getExam(ExamRequest examRequest, UserToken userToken) throws IOException, GeneralSecurityException {
        return examSourceService.getExam(examRequest, userToken);
    }

    @Override
    public Exam generateExam(ExamSource examSource, ExamRequest request, String userID) {
        return examGenerationService.generate(examSource, request, userID);
    }

    @Override
    public void validateExam(ExamSource examSource) throws RuntimeException {
        examValidationService.validate(examSource);
    }

}
