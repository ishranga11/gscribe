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

package com.google.googleinterns.gscribe.services;

import com.google.googleinterns.gscribe.models.Exam;
import com.google.googleinterns.gscribe.models.User;
import com.google.googleinterns.gscribe.resources.io.exception.ExamFormatException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidDatabaseDataException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;
import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface ExamService {

    /**
     * Called to completely process and validate the question paper from spreadsheet and return an Exam object
     * This method first parses the spreadsheet with getExamSheet method
     * To reuse the accessTokens at first older access token is used to access the spreadsheet
     * If the accessToken has expired identified by GoogleJsonResponseException then refresh the accessToken
     * Validate the exam
     * Generate the exam object
     *
     * @param examRequest ( contains spreadsheetID, sheetName )
     * @param user        ( contains accessToken, refreshToken for user )
     * @return Exam object
     * @throws IOException,GeneralSecurityException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     * @throws ExamFormatException                  ( when exam validation fails )
     * @throws InvalidDatabaseDataException         ( when the token received from database is invalid or inconsistent with user )
     * @throws InvalidRequestException              ( when unable to parse the spreadsheet )
     */
    Exam getExam(ExamRequest examRequest, User user) throws IOException, GeneralSecurityException, ExamFormatException, InvalidRequestException, InvalidDatabaseDataException;

}
