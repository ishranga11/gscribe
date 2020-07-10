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

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.googleinterns.gscribe.dao.UserTokenDao;
import com.google.googleinterns.gscribe.models.*;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidDatabaseDataException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;
import com.google.googleinterns.gscribe.services.SheetService;
import com.google.googleinterns.gscribe.services.TokenService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class SheetServiceImpl implements SheetService {

    private final TokenService tokenService;
    private final UserTokenDao userTokenDao;

    public SheetServiceImpl(TokenService tokenService, UserTokenDao userTokenDao) {
        this.tokenService = tokenService;
        this.userTokenDao = userTokenDao;
    }

    /**
     * Called when responses sheet needs to be setup for the exam
     * Fetch all the sheets metadata in the spreadsheet and check if sheet with name sheetName already exists
     * If sheet already exists then clear the sheet
     * If sheet does not exist then create new sheet with name sheetName in the spreadsheet
     *
     * @param service       ( sheet service object )
     * @param spreadsheetId ( spreadsheet id of spreadsheet where question paper lies )
     * @param sheetName     ( sheet name of sheet which is to be created or cleared )
     * @throws IOException ( if any error occurs regarding access of spreadsheet )
     */
    private void sheetSetup(Sheets service, String spreadsheetId, String sheetName) throws IOException {

        Sheets.Spreadsheets.Get spreadsheetService = service.spreadsheets().get(spreadsheetId);
        Spreadsheet spreadsheet = spreadsheetService.execute();
        List<Sheet> sheets = spreadsheet.getSheets();
        boolean sheetAlreadyMade = false;
        for (Sheet sheet : sheets) {
            if (sheet.getProperties().getTitle().equals(sheetName)) {
                ClearValuesRequest requestBody = new ClearValuesRequest();
                Sheets.Spreadsheets.Values.Clear request = service.spreadsheets().values().clear(spreadsheetId, sheetName, requestBody);
                request.execute();
                sheetAlreadyMade = true;
            }
        }
        if (!sheetAlreadyMade) {
            AddSheetRequest addSheetRequest = new AddSheetRequest();
            SheetProperties sheetProperties = new SheetProperties();
            sheetProperties.setTitle(sheetName);
            addSheetRequest.setProperties(sheetProperties);
            List<Request> requestsList = new ArrayList<>();
            requestsList.add(new Request().setAddSheet(addSheetRequest));
            BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest();
            batchUpdateSpreadsheetRequest.setRequests(requestsList);
            service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateSpreadsheetRequest).execute();
        }

    }

    /**
     * Called to create responses sheet for the exam
     * Sets up the responses sheet using sheetSetup function
     * Forms header for the response sheet as ( Start time, roll number, questions ..., final points )
     * Add header to the sheet
     *
     * @param exam ( exam object to be filled into the spreadsheet )
     * @param user ( user object for tokens to access spreadsheet )
     * @throws GeneralSecurityException,IOException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     */
    private void fillResponseTemplate(Exam exam, User user) throws GeneralSecurityException, IOException {
        /* Set access token to get the spreadsheet Instance */
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(user.getAccessToken());
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gscribe").build();

        String sheetName = "Responses_" + exam.getExamMetadata().getId();
        String spreadsheetID = exam.getExamMetadata().getSpreadsheetID();
        sheetSetup(service, spreadsheetID, sheetName);

        List<Question> questions = exam.getQuestionsList().getQuestions();
        List<List<Object>> writeBack = new ArrayList<>();
        List<Object> header = new ArrayList<>();
        header.add("Start time");
        header.add("RollNumber");
        for (Question question : questions) {
            header.add(question.getStatement());
        }
        header.add("Final Points");
        writeBack.add(header);
        ValueRange body = new ValueRange().setValues(writeBack);
        service.spreadsheets().values().update(spreadsheetID, sheetName, body).setValueInputOption("USER_ENTERED").execute();

    }

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
    @Override
    public void makeResponseSheet(Exam exam, User user) throws GeneralSecurityException, IOException, InvalidRequestException, InvalidDatabaseDataException {

        try {
            fillResponseTemplate(exam, user);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 401) {
                tokenService.refreshToken(user);
                userTokenDao.insertUserToken(user);
                try {
                    fillResponseTemplate(exam, user);
                } catch (GoogleJsonResponseException ex) {
                    throw new InvalidRequestException("Unable to parse Spreadsheet");
                }
            } else {
                throw new InvalidRequestException("Unable to parse Spreadsheet");
            }
        }
    }

    /**
     * Called to submit response to the responses spreadsheet
     * Forms the response row to be added to spreadsheet having ( start time, student roll number, answers ..., '-' for final points )
     * Append the responses to the spreadsheet
     *
     * @param examInstance ( exam instance object for the exam taken )
     * @param user         ( paper setter user object to access spreadsheet )
     * @param examMetadata ( exam metadata for exam to get spreadsheet id )
     * @throws GeneralSecurityException,IOException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     */
    private void addResponseToSheet(ExamInstance examInstance, User user, ExamMetadata examMetadata) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(user.getAccessToken());
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gscribe").build();

        List<Object> response = new ArrayList<>();
        response.add(examInstance.getStartTime().toString());
        response.add(Integer.toString(examInstance.getStudentRollNum()));
        for (int i = 0; i < examInstance.getAnswers().size(); i++) {
            response.add(examInstance.getAnswers().get(i).getAnswer());
        }
        response.add("-");
        List<List<Object>> toWrite = new ArrayList<>();
        toWrite.add(response);
        ValueRange body = new ValueRange().setValues(toWrite);
        String spreadSheetID = examMetadata.getSpreadsheetID();
        String sheetName = "Responses_" + examInstance.getExamID();
        service.spreadsheets().values().append(spreadSheetID, sheetName, body).setValueInputOption("USER_ENTERED").execute();
    }

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
    @Override
    public void addResponse(ExamInstance examInstance, User user, ExamMetadata examMetadata) throws GeneralSecurityException, InvalidDatabaseDataException, InvalidRequestException, IOException {
        try {
            addResponseToSheet(examInstance, user, examMetadata);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 401) {
                tokenService.refreshToken(user);
                userTokenDao.insertUserToken(user);
                try {
                    addResponseToSheet(examInstance, user, examMetadata);
                } catch (GoogleJsonResponseException ex) {
                    throw new InvalidRequestException("Unable to parse Spreadsheet");
                }
            } else {
                throw new InvalidRequestException("Unable to parse Spreadsheet");
            }
        }
    }

}
