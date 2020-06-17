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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.googleinterns.gscribe.models.UserToken;
import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;
import com.google.googleinterns.gscribe.services.ExamSourceService;
import com.google.googleinterns.gscribe.services.data.ExamSource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class ExamSourceServiceImpl implements ExamSourceService {

    /**
     * Reads column 1 of the sheet and return the number of rows filled
     * With help of this we identify the range of spreadsheet to be read
     *
     * @param service       ( sheet service instance )
     * @param spreadsheetId ( spreadsheet ID to be read )
     * @param sheetName     ( sheetName from spreadsheet to be read )
     * @return number of rows filled in column 1
     * @throws IOException ( if the spreadsheetId or sheetName is invalid )
     */
    private int getNumberOfRowsFilled(Sheets service, String spreadsheetId, String sheetName) throws IOException {
        final String noOfQuestionsChecker = sheetName + "!A:A";
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, noOfQuestionsChecker).execute();
        return response.getValues().size();
    }

    /**
     * Reads the sheet identified by request
     *
     * @param request ( contains spreadsheetId, sheetName to be read )
     * @param token   ( contains access token )
     * @return an ExamSource object containing an image of the sheet identified with request
     * @throws IOException              ( thrown by NetHttpTransport or when unable to read sheet )
     * @throws GeneralSecurityException ( thrown by NetHttpTransport )
     */
    public ExamSource getExam(ExamRequest request, UserToken token) throws IOException, GeneralSecurityException {

        // Set access token to get the spreadsheet Instance
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(token.getAccessToken());
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gscribe").build();

        // Parse the Sheet
        int numberOfQuestions = getNumberOfRowsFilled(service, request.getSpreadsheetID(), request.getSheetName());
        String range = request.getSheetName() + "!A1:G" + numberOfQuestions;
        ValueRange response = service.spreadsheets().values().get(request.getSpreadsheetID(), range).execute();
        List<List<Object>> exam = response.getValues();
        return new ExamSource(exam);

    }

}
