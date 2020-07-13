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

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.googleinterns.gscribe.models.Exam;
import com.google.googleinterns.gscribe.models.ExamInstance;
import com.google.googleinterns.gscribe.models.ExamMetadata;
import com.google.googleinterns.gscribe.models.User;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidDatabaseDataException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface SpreadsheetService {

    /**
     * Called to read spreadsheet identified by spreadsheet id and range given in arguments
     * Calls parseSpreadsheet function to parse the spreadsheet
     * If first time gets 401 error then refresh the token as access token might have expired
     *
     * @param user          ( user object containing tokens )
     * @param spreadsheetID ( spreadsheet ID of spreadsheet to be read )
     * @param range         ( range to be read from spreadsheet )
     * @return ValueRange object
     * @throws GeneralSecurityException,IOException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     * @throws InvalidDatabaseDataException         ( when the token received from database is invalid or inconsistent with user )
     * @throws InvalidRequestException              ( when unable to parse the spreadsheet )
     */
    ValueRange parseSpreadsheetRequest(@NotNull User user, @NotNull String spreadsheetID, @NotNull String range) throws GeneralSecurityException, InvalidDatabaseDataException, InvalidRequestException, IOException;

    /**
     * Called when exam is submitted by the paper setter and now responses sheet should be made
     * Creates sheet for responses or clear sheet if already present
     * Adds header to the sheet for this exam
     * If first time gets 401 error then refresh the token as access token might have expired
     *
     * @param exam ( exam object to be filled into the spreadsheet )
     * @param user ( user object for tokens to access spreadsheet )
     * @throws GeneralSecurityException,IOException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     * @throws InvalidDatabaseDataException         ( If the user tokens stored in database are malformed )
     * @throws InvalidRequestException              ( If unable to access spreadsheet to submit response )
     */
    void makeResponseSheet(Exam exam, User user) throws GeneralSecurityException, IOException, InvalidRequestException, InvalidDatabaseDataException;

    /**
     * Called to store the response from examinee to the responses spreadsheet
     * Forms the row to be added to spreadsheet and adds it to spreadsheet via addResponseToSheet function
     * If first time gets 401 error then refresh the token as access token might have expired
     *
     * @param examInstance ( exam instance object for the exam taken )
     * @param user         ( paper setter user object to access spreadsheet )
     * @param examMetadata ( exam metadata for exam to get spreadsheet id )
     * @throws GeneralSecurityException,IOException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     * @throws InvalidDatabaseDataException         ( If the user tokens stored in database are malformed )
     * @throws InvalidRequestException              ( If unable to access spreadsheet to submit response )
     */
    void addResponseRequest(ExamInstance examInstance, User user, ExamMetadata examMetadata) throws GeneralSecurityException, InvalidDatabaseDataException, InvalidRequestException, IOException;

}
