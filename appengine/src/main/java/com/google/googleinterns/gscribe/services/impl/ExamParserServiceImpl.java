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
import com.google.googleinterns.gscribe.resources.io.exception.ExamFormatException;
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

    /**
     * Reads the sheet identified by request
     *
     * @param request ( contains spreadsheetId, sheetName to be read )
     * @param token   ( contains access token )
     * @return an ExamSource object containing an image of the sheet identified with request
     * @throws GeneralSecurityException,IOException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     */
    @Override
    public ExamSource getExam(ExamRequest request, UserToken token) throws IOException, GeneralSecurityException {
        return examSourceService.getExam(request, token);
    }

    /**
     * makes examMetadata object
     * makes an arraylist of questions
     * returns exam object
     *
     * @param examSource ( contains sheet instance containing exam )
     * @param request    ( contains spreadsheetId and sheetName )
     * @param userID     ( unique user ID of user )
     * @return Exam object
     */
    @Override
    public Exam generateExam(ExamSource examSource, ExamRequest request, String userID) {
        return examGenerationService.generate(examSource, request, userID);
    }

    /**
     * check that the size of sheet is at least 3
     * validate duration
     * validate each question
     *
     * @param examSource ( contains instance of exam sheet )
     */
    @Override
    public void validateExam(ExamSource examSource) throws ExamFormatException {
        examValidationService.validate(examSource);
    }

}
