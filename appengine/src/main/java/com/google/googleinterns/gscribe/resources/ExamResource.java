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

package com.google.googleinterns.gscribe.resources;

import com.google.googleinterns.gscribe.dao.ExamMetadataDao;
import com.google.googleinterns.gscribe.dao.QuestionsDao;
import com.google.googleinterns.gscribe.dao.UserTokenDao;
import com.google.googleinterns.gscribe.models.Exam;
import com.google.googleinterns.gscribe.models.ExamMetadata;
import com.google.googleinterns.gscribe.models.Question;
import com.google.googleinterns.gscribe.models.User;
import com.google.googleinterns.gscribe.resources.io.exception.ExamFormatException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidDatabaseDataException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;
import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;
import com.google.googleinterns.gscribe.resources.io.response.ExamResponse;
import com.google.googleinterns.gscribe.resources.io.response.ExamsListResponse;
import com.google.googleinterns.gscribe.services.ExamSheetsService;
import com.google.googleinterns.gscribe.services.TokenService;
import com.google.gson.Gson;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Path("/exam")
@Produces("application/json")
public class ExamResource {

    private final ExamSheetsService examSheetsService;
    private final TokenService tokenService;
    private final UserTokenDao userTokenDao;
    private final ExamMetadataDao examMetadataDao;
    private final QuestionsDao questionsDao;

    @Inject
    public ExamResource(ExamSheetsService examSheetsService, TokenService tokenService, UserTokenDao userTokenDao, ExamMetadataDao examMetadataDao, QuestionsDao questionsDao) {
        this.examSheetsService = examSheetsService;
        this.tokenService = tokenService;
        this.userTokenDao = userTokenDao;
        this.examMetadataDao = examMetadataDao;
        this.questionsDao = questionsDao;
    }

    /**
     * Get corresponding userID from the IDToken using tokenVerifier
     * Get tokens for the user from the database
     * Use the tokens to read exam from the spreadsheet
     * Validate exam
     * Convert exam from List<List<Object>> to Exam object
     * post examMetadata in database to get examID
     * post exam into the database
     *
     * @param IDToken ( from header )
     * @param request ( must contain spreadsheetID, sheetName )
     * @return Exam object
     * @throws BadRequestException          ( if IDToken is invalid )
     * @throws InternalServerErrorException ( by GeneralSecurityException and IOException for credentials file )
     */
    @POST
    public ExamResponse postExam(@NotNull @HeaderParam("Authentication") String IDToken, @NotNull ExamRequest request) {
        String userID;
        User token;
        Exam exam;
        try {
            userID = tokenService.verifyIDToken(IDToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new InternalServerErrorException();
        } catch (InvalidRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        token = userTokenDao.getUserToken(userID);
        if (token == null) {
            throw new NotAuthorizedException("User not authorized");
        }

        try {
            exam = examSheetsService.getExam(request, token);
        } catch (InvalidRequestException | ExamFormatException e) {
            throw new BadRequestException(e.getMessage());
        } catch (GeneralSecurityException | IOException | InvalidDatabaseDataException e) {
            throw new InternalServerErrorException();
        }

        int examID = examMetadataDao.insertExamMetadata(exam.getExamMetadata());
        exam.getExamMetadata().setId(examID);

        List<String> questionJSON = new ArrayList<>();
        List<Integer> questionNum = new ArrayList<>();
        for (int i = 0; i < exam.getQuestions().size(); i++) {
            questionJSON.add(new Gson().toJson(exam.getQuestions().get(i)));
            questionNum.add(exam.getQuestions().get(i).getQuestionNumber());
        }
        questionsDao.insertExamQuestions(questionJSON, examID, questionNum);
        return new ExamResponse(exam);
    }

    /**
     * Get corresponding userID from the IDToken using tokenVerifier
     * using userID get all exams metadata from database for current user
     *
     * @param IDToken ( from header )
     * @return List of exam metadata for current user
     * @throws BadRequestException          ( if IDToken is invalid )
     * @throws InternalServerErrorException ( by GeneralSecurityException and IOException for credentials file )
     */
    @GET
    @Path("/all")
    public ExamsListResponse getAllExamsId(@NotNull @HeaderParam("authorization-code") String IDToken) {
        String userID;
        try {
            userID = tokenService.verifyIDToken(IDToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new InternalServerErrorException();
        } catch (InvalidRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        return new ExamsListResponse(examMetadataDao.getExamMetadataByUser(userID));
    }

    /**
     * Get corresponding userID from the IDToken using tokenVerifier
     * Check if exam with given examID was given by current user
     * Fetch exam metadata for given examID
     * Fetch exam questions for given examID
     *
     * @param IDToken ( from header )
     * @param id      ( examID for some exam )
     * @return exam object for given examID
     * @throws BadRequestException          ( if IDToken is invalid )
     * @throws InternalServerErrorException ( by GeneralSecurityException and IOException for credentials file )
     */
    @GET
    @Path("/{id}")
    public ExamResponse getExam(@NotNull @HeaderParam("authorization-code") String IDToken, @NotNull @PathParam("id") String id) {
        String userID;
        try {
            userID = tokenService.verifyIDToken(IDToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new InternalServerErrorException();
        } catch (InvalidRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        ExamMetadata metadata = examMetadataDao.getExamMetadataById(id, userID);
        List<Question> questions = questionsDao.getExamQuestions(id);
        Exam exam = new Exam(metadata, questions);
        return new ExamResponse(exam);
    }

}
