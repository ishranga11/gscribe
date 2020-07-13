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

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.googleinterns.gscribe.models.*;
import com.google.googleinterns.gscribe.resources.io.exception.ExamFormatException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidDatabaseDataException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;
import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;
import com.google.googleinterns.gscribe.services.ExamService;
import com.google.googleinterns.gscribe.services.SpreadsheetService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class ExamServiceImpl implements ExamService {

    private final SpreadsheetService spreadsheetService;

    public ExamServiceImpl(SpreadsheetService spreadsheetService) {
        this.spreadsheetService = spreadsheetService;
    }

    /**
     * Called when parsing question paper from spreadsheet to know how many rows should be read
     * Reads column A of the sheet and returns the number of rows filled in column A
     * With help of this we identify the range of spreadsheet to be read
     *
     * @param user          ( user tokens to be passed to spreadsheetService )
     * @param spreadsheetId ( spreadsheet ID to be read )
     * @param sheetName     ( sheetName from spreadsheet to be read )
     * @return number of rows filled in column 1
     * @throws IOException,GeneralSecurityException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file   )
     * @throws InvalidDatabaseDataException         ( thrown by TokenService when refreshing the token, user ID mismatches )
     * @throws InvalidRequestException              ( when unable to access spreadsheet instance with user tokens indicating that user does not have access to that spreadsheet )
     */
    private int getNumberOfRowsFilled(User user, String spreadsheetId, String sheetName) throws IOException, InvalidRequestException, GeneralSecurityException, InvalidDatabaseDataException {
        String range = sheetName + "!A:A";
        ValueRange response = spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, range);
        return response.getValues().size();
    }

    /**
     * Called to extract question paper from spreadsheet and feed into ExamSource object
     * Reads the sheet identified by request ( spreadsheetID, sheetName )
     *
     * @param request ( request object containing spreadsheetId, sheetName to be read )
     * @param user    ( user object containing tokens )
     * @return an ExamSource object
     * @throws GeneralSecurityException,IOException ( thrown by NetHttpTransport, GoogleClientSecrets, GoogleTokenResponse or by invalid credentials file  )
     * @throws InvalidDatabaseDataException         ( thrown by TokenService when refreshing the token, user ID mismatches )
     * @throws InvalidRequestException              ( when unable to access spreadsheet instance with user tokens indicating that user does not have access to that spreadsheet )
     */
    private ExamSource getExamSheet(ExamRequest request, User user) throws IOException, GeneralSecurityException, InvalidRequestException, InvalidDatabaseDataException {
        ExamSource examSource;
        int numberOfRowsFilled = getNumberOfRowsFilled(user, request.getSpreadsheetID(), request.getSheetName());
        String range = request.getSheetName() + "!A1:G" + numberOfRowsFilled;
        ValueRange response = spreadsheetService.parseSpreadsheetRequest(user, request.getSpreadsheetID(), range);
        List<List<Object>> exam = response.getValues();
        examSource = new ExamSource(exam);
        return examSource;
    }

    /**
     * Called to generate an exam metadata object combining various metadata fields
     * Takes information needed for examMetadata and returns ExamMetadata object
     *
     * @param examSource ( contains sheet instance containing exam )
     * @param request    ( contains spreadsheetId and sheetName )
     * @param userID     ( unique user ID of user )
     * @return examMetadata
     */
    private ExamMetadata generateMetadata(ExamSource examSource, ExamRequest request, String userID) {
        int duration = Integer.parseInt(examSource.getExam().get(0).get(1).toString());
        return new ExamMetadata(request.getSpreadsheetID(), request.getSheetName(), userID, duration);
    }

    /**
     * Called when a List<Object> of subjective question needs to be converted to Question object
     * Reads the points of the question
     * Reads the statement of question
     * Return subjective question object
     *
     * @param questionObject ( question instance from sheet )
     * @param questionNumber ( number of this question in order of questions )
     * @return Question object
     */
    private Question createSubjectiveQuestion(List<Object> questionObject, int questionNumber) {
        int points = Integer.parseInt(questionObject.get(6).toString());
        String questionStatement = questionObject.get(1).toString();
        return new SubjectiveQuestion(questionStatement, points, questionNumber);
    }

    /**
     * Called when a List<Object> of MCQ question needs to be converted to Question object
     * Reads the points of the question
     * Reads the statement of the question
     * Reads the options of the question
     * Returns the multiple choice question object
     *
     * @param questionObject ( question instance from sheet )
     * @param questionNumber ( number of this question in order of questions )
     * @return Question Object
     */
    private Question CreateMultipleChoiceQuestion(List<Object> questionObject, int questionNumber) {
        int points = Integer.parseInt(questionObject.get(6).toString());
        String questionStatement = questionObject.get(1).toString();
        List<String> options = new ArrayList<>();
        for (int i = 2; i < 6; i++) options.add(questionObject.get(i).toString());
        return new MultipleChoiceQuestion(questionStatement, points, questionNumber, options);
    }

    /**
     * Called to combine all exam fields to create a single exam object
     * Makes examMetadata object
     * Makes a list of questions
     * Question object is added based on question type
     * Returns exam object
     *
     * @param examSource ( contains sheet instance containing exam )
     * @param request    ( contains spreadsheetId and sheetName )
     * @param userID     ( unique user ID of user )
     * @return Exam object
     */
    private Exam generateExam(ExamSource examSource, ExamRequest request, String userID) {
        ExamMetadata metadata = generateMetadata(examSource, request, userID);
        List<Question> questionsList = new ArrayList<>();
        List<List<Object>> exam = examSource.getExam();
        int questionNumber;
        for (int i = 2; i < exam.size(); i++) {
            questionNumber = i - 1;
            List<Object> currentQuestion = exam.get(i);
            if (currentQuestion.get(0).equals("MCQ"))
                questionsList.add(CreateMultipleChoiceQuestion(currentQuestion, questionNumber));
            else if (currentQuestion.get(0).equals("SUBJECTIVE"))
                questionsList.add(createSubjectiveQuestion(currentQuestion, questionNumber));
        }
        Questions questions = new Questions();
        questions.setQuestionsList(questionsList);
        return new Exam(metadata, questions);
    }

    /**
     * Called to validate that duration of exam is in proper range
     * In the exam template duration is to be mentioned in B1
     * check that in B1 duration is mentioned in proper format and in range 1-300
     *
     * @param row1 ( row 1 of spreadsheet )
     * @throws ExamFormatException ( if duration is not present in assigned B1 cell,
     *                             if duration is not in a proper integer format,
     *                             if duration is not in the range of 1-300 )
     */
    private void validateDuration(List<Object> row1) throws ExamFormatException {
        if (row1.size() < 2) throw new ExamFormatException("duration not present in B1");
        String durationString = row1.get(1).toString();
        int duration;
        try {
            duration = Integer.parseInt(durationString);
        } catch (NumberFormatException e) {
            throw new ExamFormatException("duration not in a proper format in B1");
        }
        if (duration <= 0 || duration > 300) throw new ExamFormatException("duration not in a range of 1-300 in B1");
    }

    /**
     * Called to validate MCQ question
     * check that the question has statement in column 2
     * check that the question has options in column 3-6
     *
     * @param question    ( instance of row of spreadsheet containing question )
     * @param questionRow ( row number of row in which this question lies )
     * @throws ExamFormatException ( if question statement is missing in column B of the row
     *                             if question has empty options field )
     */
    private void checkMultipleChoiceQuestion(List<Object> question, int questionRow) throws ExamFormatException {
        if (question.get(1).equals("")) throw new ExamFormatException("missing question statement B" + questionRow);
        for (int i = 2; i < 6; i++) {
            if (question.get(i).equals(""))
                throw new ExamFormatException("missing multiple choice question option in row " + questionRow);
        }
    }

    /**
     * Called to validate subjective question
     * check that the question has statement in column 2
     * check that the question has no options in column 3-6
     *
     * @param question    ( instance of row of spreadsheet containing question )
     * @param questionRow ( row number of row in which this question lies )
     * @throws ExamFormatException ( if question statement is missing in column B of the row
     *                             if question has non empty options field )
     */
    private void checkSubjectiveQuestion(List<Object> question, int questionRow) throws ExamFormatException {
        if (question.get(1).equals("")) throw new ExamFormatException("missing question statement B" + questionRow);
        for (int i = 2; i < 6; i++) {
            if (!question.get(i).equals(""))
                throw new ExamFormatException("subjective question does not expect option in row " + questionRow);
        }
    }

    /**
     * Called to validate that points for a question are in proper range
     * Check that the points are in proper format
     * Check that the points lie in range of 1-100
     *
     * @param pointsString ( points mentioned for question in sheet )
     * @param questionRow  ( row in which this question lies )
     * @throws ExamFormatException ( if points are not in a proper integer format,
     *                             if points are not in the range of 1-100 )
     */
    private void validatePoints(String pointsString, int questionRow) throws ExamFormatException {
        int points;
        try {
            points = Integer.parseInt(pointsString);
        } catch (NumberFormatException e) {
            throw new ExamFormatException("points not in a proper format in G" + questionRow);
        }
        if (points <= 0 || points > 100)
            throw new ExamFormatException("points not in a valid range of 1-100 in G" + questionRow);
    }

    /**
     * Called to validate whole exam
     * check that the size of sheet is at least 3 as first two rows are taken by template
     * validate duration
     * validate each question based on question type
     *
     * @param examSource ( contains instance of exam sheet )
     * @throws ExamFormatException ( if duration verification fails,
     *                             if multipleChoiceQuestion verification fails,
     *                             if SubjectiveQuestion verification fails,
     *                             if question had invalid question type,
     *                             if points verification for any question fails )
     */
    private void validateExam(ExamSource examSource) throws ExamFormatException {
        List<List<Object>> exam = examSource.getExam();
        if (exam.size() < 3) throw new ExamFormatException("Improper exam template used");
        validateDuration(exam.get(0));
        for (int i = 2; i < exam.size(); i++) {
            List<Object> currentQuestion = exam.get(i);
            int questionRow = i + 1;
            if (currentQuestion.get(0).equals("MCQ")) checkMultipleChoiceQuestion(currentQuestion, questionRow);
            else if (currentQuestion.get(0).equals("SUBJECTIVE")) checkSubjectiveQuestion(currentQuestion, questionRow);
            else throw new ExamFormatException("Question type not identified at A" + questionRow);
            validatePoints(currentQuestion.get(6).toString(), questionRow);
        }
    }

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
    @Override
    public Exam getExam(ExamRequest examRequest, User user) throws IOException, GeneralSecurityException, ExamFormatException, InvalidDatabaseDataException, InvalidRequestException {
        ExamSource examSource = getExamSheet(examRequest, user);
        validateExam(examSource);
        return generateExam(examSource, examRequest, user.getId());
    }

    /**
     * Private class to wrap List<List<Object>>
     * It represents the sheet instance
     */
    private static class ExamSource {
        private final List<List<Object>> exam;

        public ExamSource(List<List<Object>> exam) {
            this.exam = exam;
        }

        public List<List<Object>> getExam() {
            return exam;
        }
    }
}
