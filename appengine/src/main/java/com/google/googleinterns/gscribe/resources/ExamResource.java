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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.googleinterns.gscribe.dao.ExamDao;
import com.google.googleinterns.gscribe.dao.ExamMetadataDao;
import com.google.googleinterns.gscribe.dao.UserTokenDao;
import com.google.googleinterns.gscribe.models.Exam;
import com.google.googleinterns.gscribe.models.ExamMetadata;
import com.google.googleinterns.gscribe.models.UserToken;
import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;
import com.google.googleinterns.gscribe.resources.io.response.ExamResponse;
import com.google.googleinterns.gscribe.resources.io.response.ExamsListResponse;
import com.google.googleinterns.gscribe.services.ExamParserService;
import com.google.googleinterns.gscribe.services.TokenService;
import com.google.googleinterns.gscribe.services.data.ExamSource;
import com.google.inject.Inject;

import javax.ws.rs.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Path("/exam")
@Produces("application/json")
public class ExamResource {

    private final ExamParserService examParserService;
    private final TokenService tokenService;
    private final UserTokenDao userTokenDao;
    private final ExamMetadataDao examMetadataDao;
    private final ExamDao examDao;

    @Inject
    public ExamResource(ExamParserService examParserService, TokenService tokenService, UserTokenDao userTokenDao, ExamMetadataDao examMetadataDao, ExamDao examDao) {
        this.examParserService = examParserService;
        this.tokenService = tokenService;
        this.userTokenDao = userTokenDao;
        this.examMetadataDao = examMetadataDao;
        this.examDao = examDao;
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
     */
    @POST
    public ExamResponse postExam(@HeaderParam("Authentication") String IDToken, ExamRequest request) throws GeneralSecurityException, IOException, RuntimeException {
        String userID = tokenService.verifyIDToken(IDToken);
        UserToken token = userTokenDao.getUserToken(userID);
        ExamSource examSource = null;
        try {
            examSource = examParserService.getExam(request, token);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 401) {
                tokenService.refreshToken(token);
                userTokenDao.updateTokens(token);
                examSource = examParserService.getExam(request, token);
            }
        }
        examParserService.validateExam(examSource);
        Exam exam = examParserService.generateExam(examSource, request, userID);
        List<String> questionJSON = new ArrayList<>();
        List<Integer> questionNum = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < exam.getQuestions().size(); i++) {
            questionJSON.add(mapper.writeValueAsString(exam.getQuestions().get(i)));
            questionNum.add(exam.getQuestions().get(i).getQuestionNumber());
        }
        int examID = examMetadataDao.insertExamMetadata(exam.getExamMetadata());
        examDao.insertExamQuestions(questionJSON, examID, questionNum);
        return new ExamResponse(exam);
    }

    /**
     * Get corresponding userID from the IDToken using tokenVerifier
     * using userID get all exams metadata from database for current user
     *
     * @param IDToken ( from header )
     * @return List of exam metadata for current user
     */
    @GET
    @Path("/all")
    public ExamsListResponse getAllExamsId(@HeaderParam("authorization-code") String IDToken) throws GeneralSecurityException, IOException {
        String userID = tokenService.verifyIDToken(IDToken);
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
     */
    @GET
    @Path("/{id}")
    public ExamResponse getExam(@HeaderParam("authorization-code") String IDToken, @PathParam("id") String id) throws GeneralSecurityException, IOException {
        String userID = tokenService.verifyIDToken(IDToken);
        ExamMetadata metadata = examMetadataDao.getExamMetadataById(id, userID);
        return new ExamResponse();
    }

}
