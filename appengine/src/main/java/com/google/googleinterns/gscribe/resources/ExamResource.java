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
import com.google.googleinterns.gscribe.dao.*;
import com.google.googleinterns.gscribe.models.*;
import com.google.googleinterns.gscribe.resources.io.exception.ExamFormatException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidDatabaseDataException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;
import com.google.googleinterns.gscribe.resources.io.request.ExamInstanceRequest;
import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;
import com.google.googleinterns.gscribe.resources.io.request.ExamSubmitRequest;
import com.google.googleinterns.gscribe.resources.io.response.ExamInstanceResponse;
import com.google.googleinterns.gscribe.resources.io.response.ExamResponse;
import com.google.googleinterns.gscribe.resources.io.response.ExamSubmitResponse;
import com.google.googleinterns.gscribe.resources.io.response.ExamsListResponse;
import com.google.googleinterns.gscribe.services.ExamService;
import com.google.googleinterns.gscribe.services.SpreadsheetService;
import com.google.googleinterns.gscribe.services.TokenService;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Path("/exam")
@Produces("application/json")
public class ExamResource {

    private final ExamService examService;
    private final TokenService tokenService;
    private final UserTokenDao userTokenDao;
    private final ExamMetadataDao examMetadataDao;
    private final QuestionsDao questionsDao;
    private final ExamInstanceDao examInstanceDao;
    private final AnswerDao answerDao;
    private final SpreadsheetService spreadsheetService;
    private final ObjectMapper objectMapper;

    @Inject
    public ExamResource(ExamService examService, TokenService tokenService, UserTokenDao userTokenDao, ExamMetadataDao examMetadataDao, QuestionsDao questionsDao, ExamInstanceDao examInstanceDao, AnswerDao answerDao, SpreadsheetService spreadsheetService, ObjectMapper objectMapper) {
        this.examService = examService;
        this.tokenService = tokenService;
        this.userTokenDao = userTokenDao;
        this.examMetadataDao = examMetadataDao;
        this.questionsDao = questionsDao;
        this.examInstanceDao = examInstanceDao;
        this.answerDao = answerDao;
        this.spreadsheetService = spreadsheetService;
        this.objectMapper = objectMapper;
    }

    /**
     * TODO:
     *  - Code cleanup by creating new service for duplicate code
     *  - Handle throwing server errors from service only
     *  - Update Test cases accordingly
     */

    /**
     * Called when paper setter submits question paper
     * Gets corresponding userID from the IDToken using tokenVerifier
     * Get tokens for the user from the database and use the tokens to read exam from the spreadsheet
     * Validate exam and convert exam from List<List<Object>> to Exam object
     * post examMetadata in database to get examID
     * make response template in google sheets for this exam
     * post exam into the database
     *
     * @param IDToken ( used to authenticate user, here paper setter )
     * @param request ( contains spreadsheetID, sheetName )
     * @return Exam object
     * @throws BadRequestException          ( if IDToken is invalid,
     *                                      if user cannot access spreadsheet given in the request,
     *                                      if the question paper is not following correct format as defined in template )
     * @throws InternalServerErrorException ( by GeneralSecurityException and IOException for credentials file,
     *                                      if the database contains malformed used token data,
     *                                      unable to create response sheet in spreadsheet for this exam )
     * @throws NotAuthorizedException       ( if the user is not authorized i.e. no tokens for user available in database )
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
            exam = examService.getExam(request, token);
        } catch (InvalidRequestException | ExamFormatException e) {
            throw new BadRequestException(e.getMessage());
        } catch (GeneralSecurityException | IOException | InvalidDatabaseDataException e) {
            throw new InternalServerErrorException();
        }

        try {
            int examID = examMetadataDao.insertExamMetadata(exam.getExamMetadata());
            exam.getExamMetadata().setId(examID);
            spreadsheetService.makeResponseSheet(exam, token);
            String questionJSON = objectMapper.writeValueAsString(exam.getQuestions());
            questionsDao.insertExamQuestions(examID, questionJSON);
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
        return new ExamResponse(exam);
    }

    /**
     * Called to fill exam metadata table on paper setter dashboard
     * Get corresponding userID from the IDToken using tokenVerifier
     * using userID get all exams metadata from database for current user
     *
     * @param IDToken ( used to authenticate user, here paper setter )
     * @return List of exam metadata
     * @throws BadRequestException          ( if IDToken is invalid )
     * @throws InternalServerErrorException ( by GeneralSecurityException and IOException for credentials file )
     */
    @GET
    @Path("/all")
    public ExamsListResponse getAllExamsID(@NotNull @HeaderParam("Authentication") String IDToken) {
        String userID;
        try {
            userID = tokenService.verifyIDToken(IDToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new InternalServerErrorException();
        } catch (InvalidRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        return new ExamsListResponse(examMetadataDao.getExamMetadataListByUser(userID));
    }

    /**
     * Called when paper setter requests to view exam object for an exam identified by exam id {id}
     * Get corresponding userID from the IDToken using tokenVerifier
     * Check if exam with given examID was given by current user
     * Fetch exam metadata for given examID
     * Fetch exam questions for given examID
     *
     * @param IDToken ( used to authenticate user, here paper setter )
     * @param id      ( examID for some exam )
     * @return exam object
     * @throws BadRequestException          ( if IDToken is invalid )
     * @throws InternalServerErrorException ( by GeneralSecurityException and IOException for credentials file )
     */
    @GET
    @Path("/{id}")
    public ExamResponse getExam(@NotNull @HeaderParam("Authentication") String IDToken, @NotNull @PathParam("id") int id) {
        String userID;
        try {
            userID = tokenService.verifyIDToken(IDToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new InternalServerErrorException();
        } catch (InvalidRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        ExamMetadata metadata = examMetadataDao.getExamMetadataByUser(id, userID);
        Questions questions = questionsDao.getExamQuestions(id);
        Exam exam = new Exam(metadata, questions);
        return new ExamResponse(exam);
    }

    /**
     * Called when examinee wants to start the exam via google assistant
     * Get corresponding userID from the IDToken using firebaseTokenVerifier
     * firebaseTokenVerifier is used as this IDToken was generated for firebase functions
     * Check if the exam is already taken by the user or not
     * Fetch exam metadata and questions for the exam
     * Check if the examID is correct using that metadata and questions returned are not null
     * Create exam instance for the user and insert in database
     * Return exam instance response with exam and exam instance id
     *
     * @param IDToken             ( used to authenticate user, here examinee )
     * @param examInstanceRequest ( exam start request containing student roll number and exam id of exam to be taken )
     * @return exam instance response object having ( exam, exam instance id )
     * @throws InternalServerErrorException ( by GeneralSecurityException and IOException for credentials file )
     * @throws BadRequestException          ( if the IDToken is invalid,
     *                                      if the examinee already attempted this exam,
     *                                      if the exam id is invalid )
     */
    @POST
    @Path("/start")
    public ExamInstanceResponse startExam(@NotNull @HeaderParam("Authentication") String IDToken, @NotNull ExamInstanceRequest examInstanceRequest) {
        String userID;
        try {
            userID = tokenService.firebaseVerifyIDToken(IDToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new InternalServerErrorException();
        } catch (InvalidRequestException e) {
            throw new BadRequestException(e.getMessage());
        }

        ExamInstance examInstance = new ExamInstance(examInstanceRequest.getExamID(), userID, examInstanceRequest.getStudentRollNum());
        if (examInstanceDao.getExamInstanceByUserDetails(examInstanceRequest.getExamID(), examInstanceRequest.getStudentRollNum()) != null) {
            throw new BadRequestException("Exam already taken!");
        }

        ExamMetadata metadata = examMetadataDao.getExamMetadataByExamId(examInstanceRequest.getExamID());
        Questions questions = questionsDao.getExamQuestions(examInstanceRequest.getExamID());
        Exam exam = new Exam(metadata, questions);

        if (metadata == null || questions == null) {
            throw new BadRequestException("Incorrect exam ID requested");
        }
        int examInstanceID = examInstanceDao.insertExamInstance(examInstance);
        return new ExamInstanceResponse(exam, examInstanceID);

    }

    /**
     * Called when examinee wants to submit the exam
     * Get corresponding userID from the IDToken using firebaseTokenVerifier
     * firebaseTokenVerifier is used as this IDToken was generated for firebase functions
     * Check if the exam instance is same as when it was created checking same ( user id, exam id, student roll number )
     * Update the end time in exam instance
     * Insert answers for the exam instance
     * Write back responses to the responses sheet for this exam
     *
     * @param IDToken           ( used to authenticate user, here examinee )
     * @param examSubmitRequest ( contains exam instance object )
     * @return ExamSubmitResponse object containing success message
     */
    @POST
    @Path("/submit")
    public ExamSubmitResponse submitExam(@NotNull @HeaderParam("Authentication") String IDToken, @NotNull ExamSubmitRequest examSubmitRequest) {
        String userID;
        try {
            userID = tokenService.firebaseVerifyIDToken(IDToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new InternalServerErrorException();
        } catch (InvalidRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        examSubmitRequest.getExamInstance().setUserID(userID);
        ExamInstance examInstance = examInstanceDao.getExamInstanceByExamInstanceID(examSubmitRequest.getExamInstance().getId());
        if (examInstance == null || examInstance.getExamID() != examSubmitRequest.getExamInstance().getExamID() ||
                !userID.equals(examInstance.getUserID()) || examInstance.getStudentRollNum() != examSubmitRequest.getExamInstance().getStudentRollNum() || examInstance.getEndTime() != null) {
            throw new BadRequestException("Malformed request");
        }

        examSubmitRequest.getExamInstance().setStartTime(examInstance.getStartTime());

        try {

            String answersJSON = objectMapper.writeValueAsString(examSubmitRequest.getExamInstance().getAnswers());
            examInstanceDao.updateExamInstanceEndTime(examSubmitRequest.getExamInstance().getId());
            answerDao.insertAnswers(examSubmitRequest.getExamInstance().getId(), answersJSON);

            User user = userTokenDao.getUserTokenByExamID(examInstance.getExamID());
            ExamMetadata examMetadata = examMetadataDao.getExamMetadataByExamId(examInstance.getExamID());
            spreadsheetService.addResponseRequest(examSubmitRequest.getExamInstance(), user, examMetadata);

        } catch (Exception e) {
            throw new BadRequestException("Failed to submit Exam");
        }
        return new ExamSubmitResponse("Exam Submitted Successfully");

    }

}
