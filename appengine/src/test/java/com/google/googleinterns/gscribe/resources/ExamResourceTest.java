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
import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;
import com.google.googleinterns.gscribe.resources.io.response.ExamInstanceResponse;
import com.google.googleinterns.gscribe.resources.io.response.ExamResponse;
import com.google.googleinterns.gscribe.resources.io.response.ExamSubmitResponse;
import com.google.googleinterns.gscribe.resources.io.response.ExamsListResponse;
import com.google.googleinterns.gscribe.services.ExamService;
import com.google.googleinterns.gscribe.services.SpreadsheetService;
import com.google.googleinterns.gscribe.services.TokenService;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ExamResourceTest {

    private static final ExamService examService = mock(ExamService.class);
    private static final TokenService tokenService = mock(TokenService.class);
    private static final UserTokenDao userTokenDao = mock(UserTokenDao.class);
    private static final ExamMetadataDao examMetadataDao = mock(ExamMetadataDao.class);
    private static final QuestionsDao questionsDao = mock(QuestionsDao.class);
    private static final ExamInstanceDao examInstanceDao = mock(ExamInstanceDao.class);
    private static final AnswerDao answerDao = mock(AnswerDao.class);
    private static final SpreadsheetService spreadsheetService = mock(SpreadsheetService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ExamResource examResource = new ExamResource(examService, tokenService, userTokenDao, examMetadataDao, questionsDao, examInstanceDao, answerDao, spreadsheetService, objectMapper);

    private static final ResourceExtension ext = ResourceExtension.builder().addResource(examResource).build();

    private static final String IDTokenValid = "IDTokenValid";
    private static final String IDTokenInvalid = "IDTokenInvalid";
    private static final String randomIDTokenThrowGeneralSecurityException = "randomIDTokenThrowGeneralSecurityException";
    private static final String randomIDTokenThrowIOException = "randomIDTokenThrowIOException";
    private static final User user = new User("userID", "AccessToken", "RefreshToken", null);
    private static final String postRequest = "{\"spreadsheetID\":\"spreadsheetID\",\"sheetName\":\"sheetName\"}";
    private static final String examNotTakenRequest = "{ \"studentRollNum\": 10, \"examID\":10 }";
    private static final String submitExamRequest = "{ \"examInstance\":{ \"id\":10, \"examID\":10, \"studentRollNum\":10," +
            " \"answers\":{ \"answersList\":[ {\"answer\":\"Answer1\",\"questionNum\":1},{\"answer\":\"Answer2\",\"questionNum\":2} ] } } }";
    private static final ExamInstance examNotTakenInstance = new ExamInstance(10, "userID", 10);
    private static final ExamInstance submitExamInstance = new ExamInstance(10, "userID", 10);
    private static ExamRequest examRequest;
    private static Exam exam;
    private static ExamMetadata examMetadata;

    @BeforeAll
    public static void setup() throws GeneralSecurityException, IOException, InvalidRequestException, InvalidDatabaseDataException, ExamFormatException {
        String userID = "userID";
        String validSpreadsheetID = "spreadsheetID";
        String validSheetName = "sheetName";
        List<String> options = new ArrayList<>(Arrays.asList("OptionA", "OptionB", "OptionC", "OptionD"));
        Question subjectiveQuestion = new SubjectiveQuestion("SubjectiveStatement", 5, 1);
        Question multipleChoiceQuestion = new MultipleChoiceQuestion("MCQStatement", 5, 2, options);
        List<Question> questionsList = new ArrayList<>(Arrays.asList(subjectiveQuestion, multipleChoiceQuestion));
        Questions questions = new Questions();
        questions.setQuestionsList(questionsList);
        examMetadata = new ExamMetadata(validSpreadsheetID, validSheetName, userID, 10, 10, null);
        exam = new Exam(examMetadata, questions);
        ExamInstance examTakenInstance = new ExamInstance(10, userID, 11);
        examRequest = new ExamRequest(validSpreadsheetID, validSheetName);
        List<Answer> answersList = new ArrayList<>();
        answersList.add(new Answer("Answer1", 1));
        answersList.add(new Answer("Answer2", 2));
        Answers submitAnswers = new Answers();
        submitAnswers.setAnswersList(answersList);
        submitExamInstance.setId(10);
        submitExamInstance.setAnswers(submitAnswers);

        questions.setQuestionsList(questionsList);
        when(tokenService.verifyIDToken(IDTokenValid)).thenReturn(userID);
        when(tokenService.verifyIDToken(IDTokenInvalid)).thenThrow(InvalidRequestException.class);
        when(tokenService.verifyIDToken(randomIDTokenThrowGeneralSecurityException)).thenThrow(GeneralSecurityException.class);
        when(tokenService.verifyIDToken(randomIDTokenThrowIOException)).thenThrow(IOException.class);
        when(tokenService.firebaseVerifyIDToken(IDTokenValid)).thenReturn(userID);
        when(tokenService.firebaseVerifyIDToken(IDTokenInvalid)).thenThrow(InvalidRequestException.class);
        when(tokenService.firebaseVerifyIDToken(randomIDTokenThrowGeneralSecurityException)).thenThrow(GeneralSecurityException.class);
        when(tokenService.firebaseVerifyIDToken(randomIDTokenThrowIOException)).thenThrow(IOException.class);
        when(userTokenDao.getUserToken(userID)).thenReturn(user);
        when(examService.getExam(examRequest, user)).thenReturn(exam);
        when(examMetadataDao.insertExamMetadata(examMetadata)).thenReturn(10);
        when(examMetadataDao.getExamMetadataListByUser(userID)).thenReturn(new ArrayList<>(Collections.singletonList(examMetadata)));
        when(examMetadataDao.getExamMetadataByUser(10, userID)).thenReturn(examMetadata);
        when(examMetadataDao.getExamMetadataByExamId(10)).thenReturn(examMetadata);
        when(questionsDao.getExamQuestions(10)).thenReturn(questions);
        when(examInstanceDao.getExamInstanceByUserDetails(10, 11)).thenReturn(examTakenInstance);
        when(examInstanceDao.insertExamInstance(examNotTakenInstance)).thenReturn(10);
        when(examInstanceDao.getExamInstanceByExamInstanceID(10)).thenReturn(examNotTakenInstance);
        when(userTokenDao.getUserTokenByExamID(10)).thenReturn(user);
    }

    @Test
    public void postExamTest() throws GeneralSecurityException, InvalidDatabaseDataException, InvalidRequestException, IOException {
        String questionsJSON = objectMapper.writeValueAsString(exam.getQuestions());

        ExamResponse examResponse = ext.target("/exam").request().header("Authentication", IDTokenValid).post(Entity.entity(postRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        verify(spreadsheetService).makeResponseSheet(exam, user);
        verify(questionsDao).insertExamQuestions(exam.getExamMetadata().getId(), questionsJSON);
        assertEquals(exam, examResponse.getExam());

    }

    @Test
    public void WithoutHeaderAllFunctionsTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam").request().post(Entity.entity(postRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
    }

    @Test
    public void postExamInvalidIDTokenTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam").request().header("Authentication", IDTokenInvalid).post(Entity.entity(postRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
    }

    @Test
    public void postExamIDTokenInternalServerExceptionTest() {
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam").request().header("Authentication", randomIDTokenThrowGeneralSecurityException).post(Entity.entity(postRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam").request().header("Authentication", randomIDTokenThrowIOException).post(Entity.entity(postRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
    }

    @Test
    public void postExamUserNotAuthorizedTest() {
        assertThrows(NotAuthorizedException.class, () -> {
            String IDTokenForNewUser = "NewIDToken";
            String newUserID = "newUser";

            when(tokenService.verifyIDToken(IDTokenForNewUser)).thenReturn(newUserID);
            when(userTokenDao.getUserToken(newUserID)).thenReturn(null);

            ext.target("/exam").request().header("Authentication", IDTokenForNewUser).post(Entity.entity(postRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
    }

    @Test
    public void postExamInvalidRequestTest() throws GeneralSecurityException, InvalidDatabaseDataException, ExamFormatException, InvalidRequestException, IOException {
        ExamRequest examRequest = new ExamRequest("FailedSpreadsheet", "FailedSheet");
        String invalidPostRequest = "{\"spreadsheetID\":\"FailedSpreadsheet\",\"sheetName\":\"FailedSheet\"}";
        when(examService.getExam(examRequest, user)).thenThrow(InvalidRequestException.class).thenThrow(ExamFormatException.class).thenThrow(GeneralSecurityException.class).thenThrow(IOException.class).thenThrow(InvalidDatabaseDataException.class);
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam").request().header("Authentication", IDTokenValid).post(Entity.entity(invalidPostRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam").request().header("Authentication", IDTokenValid).post(Entity.entity(invalidPostRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam").request().header("Authentication", IDTokenValid).post(Entity.entity(invalidPostRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam").request().header("Authentication", IDTokenValid).post(Entity.entity(invalidPostRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam").request().header("Authentication", IDTokenValid).post(Entity.entity(invalidPostRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
    }

    @Test
    public void postExamFailMakingResponseSheetTest() throws GeneralSecurityException, InvalidDatabaseDataException, InvalidRequestException, IOException, ExamFormatException {
        String failWriteIDToken = "failWriteIDToken";
        String failWriteUserID = "failWriteUserID";
        User failWriteUser = new User(failWriteUserID, "AccessToken", "RefreshToken", null);
        when(tokenService.verifyIDToken(failWriteIDToken)).thenReturn(failWriteUserID);
        when(userTokenDao.getUserToken(failWriteUserID)).thenReturn(failWriteUser);
        when(examService.getExam(examRequest, failWriteUser)).thenReturn(exam);
        doThrow(GeneralSecurityException.class).doThrow(IOException.class).when(spreadsheetService).makeResponseSheet(exam, failWriteUser);
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam").request().header("Authentication", failWriteIDToken).post(Entity.entity(postRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam").request().header("Authentication", failWriteIDToken).post(Entity.entity(postRequest, MediaType.APPLICATION_JSON_TYPE), ExamResponse.class);
        });
    }

    @Test
    public void getAllExamsIDTest() {
        ExamsListResponse examsListResponse = ext.target("/exam/all").request().header("Authentication", IDTokenValid).get(ExamsListResponse.class);
        assertEquals(1, examsListResponse.getExamsList().size());
        assertEquals(examMetadata, examsListResponse.getExamsList().get(0));
    }

    @Test
    public void getAllExamsIDWithoutHeaderTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/all").request().get(ExamsListResponse.class);
        });
    }

    @Test
    public void getAllExamsIDInvalidIDTokenTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/all").request().header("Authentication", IDTokenInvalid).get(ExamsListResponse.class);
        });
    }

    @Test
    public void getAllExamsIDIDTokenInternalServerExceptionTest() {
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam/all").request().header("Authentication", randomIDTokenThrowGeneralSecurityException).get(ExamsListResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam/all").request().header("Authentication", randomIDTokenThrowIOException).get(ExamsListResponse.class);
        });
    }

    @Test
    public void getExamTest() {
        ExamResponse examResponse = ext.target("/exam/10").request().header("Authentication", IDTokenValid).get(ExamResponse.class);
        assertEquals(exam, examResponse.getExam());
    }

    @Test
    public void getExamIDWithoutHeaderTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/10").request().get(ExamsListResponse.class);
        });
    }

    @Test
    public void getExamInvalidIDTokenTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/10").request().header("Authentication", IDTokenInvalid).get(ExamsListResponse.class);
        });
    }

    @Test
    public void getExamIDTokenInternalServerExceptionTest() {
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam/10").request().header("Authentication", randomIDTokenThrowGeneralSecurityException).get(ExamsListResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam/10").request().header("Authentication", randomIDTokenThrowIOException).get(ExamsListResponse.class);
        });
    }

    @Test
    public void startExamTest() {
        ExamInstanceResponse examInstanceResponse = ext.target("/exam/start").request().header("Authentication", IDTokenValid).post(Entity.entity(examNotTakenRequest, MediaType.APPLICATION_JSON_TYPE), ExamInstanceResponse.class);
        assertEquals(10, examInstanceResponse.getExamInstanceID());
        assertEquals(exam, examInstanceResponse.getExam());
    }

    @Test
    public void startExamWithoutHeaderTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/start").request().post(Entity.entity(examNotTakenRequest, MediaType.APPLICATION_JSON_TYPE), ExamInstanceResponse.class);
        });
    }

    @Test
    public void startExamInvalidIDTokenTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/start").request().header("Authentication", IDTokenInvalid).post(Entity.entity(examNotTakenRequest, MediaType.APPLICATION_JSON_TYPE), ExamInstanceResponse.class);
        });
    }

    @Test
    public void startExamIDTokenInternalServerExceptionTest() {
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam/start").request().header("Authentication", randomIDTokenThrowGeneralSecurityException).post(Entity.entity(examNotTakenRequest, MediaType.APPLICATION_JSON_TYPE), ExamInstanceResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam/start").request().header("Authentication", randomIDTokenThrowIOException).post(Entity.entity(examNotTakenRequest, MediaType.APPLICATION_JSON_TYPE), ExamInstanceResponse.class);
        });
    }

    @Test
    public void startExamAlreadyTakenTest() {
        String examTakenRequest = "{ \"studentRollNum\": 11, \"examID\":10 }";
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/start").request().header("Authentication", IDTokenValid).post(Entity.entity(examTakenRequest, MediaType.APPLICATION_JSON_TYPE), ExamInstanceResponse.class);
        });
    }

    @Test
    public void startExamInvalidExamIDTest() {
        assertThrows(BadRequestException.class, () -> {
            String invalidExamIDRequest = "{ \"studentRollNum\": 10, \"examID\":20 }";
            ext.target("/exam/start").request().header("Authentication", IDTokenValid).post(Entity.entity(invalidExamIDRequest, MediaType.APPLICATION_JSON_TYPE), ExamInstanceResponse.class);
        });
    }


    @Test
    public void submitExamTest() throws GeneralSecurityException, InvalidDatabaseDataException, InvalidRequestException, IOException {
        ExamSubmitResponse examSubmitResponse = ext.target("/exam/submit").request().header("Authentication", IDTokenValid).post(Entity.entity(submitExamRequest, MediaType.APPLICATION_JSON_TYPE), ExamSubmitResponse.class);

        verify(examInstanceDao).updateExamInstanceEndTime(10);
        verify(answerDao).insertAnswers(10, "{\"answersList\":[{\"answer\":\"Answer1\",\"questionNum\":1},{\"answer\":\"Answer2\",\"questionNum\":2}]}");
        verify(spreadsheetService).addResponseRequest(submitExamInstance, user, examMetadata);
        assertEquals("Exam Submitted Successfully", examSubmitResponse.getMessage());
    }

    @Test
    public void submitExamWithoutHeaderTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/submit").request().post(Entity.entity(submitExamRequest, MediaType.APPLICATION_JSON_TYPE), ExamSubmitResponse.class);
        });
    }

    @Test
    public void submitExamInvalidIDTokenTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/submit").request().header("Authentication", IDTokenInvalid).post(Entity.entity(submitExamRequest, MediaType.APPLICATION_JSON_TYPE), ExamSubmitResponse.class);
        });
    }

    @Test
    public void submitExamIDTokenInternalServerExceptionTest() {
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam/submit").request().header("Authentication", randomIDTokenThrowGeneralSecurityException).post(Entity.entity(submitExamRequest, MediaType.APPLICATION_JSON_TYPE), ExamSubmitResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/exam/submit").request().header("Authentication", randomIDTokenThrowIOException).post(Entity.entity(submitExamRequest, MediaType.APPLICATION_JSON_TYPE), ExamSubmitResponse.class);
        });
    }

    @Test
    public void submitExamDifferentExamIDTest() {
        String submitRequest = "{ \"examInstance\":{ \"id\":10, \"examID\":12, \"studentRollNum\":10," +
                " \"answers\":{ \"answersList\":[ {\"answer\":\"Answer1\",\"questionNum\":1},{\"answer\":\"Answer2\",\"questionNum\":2} ] } } }";
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/submit").request().header("Authentication", IDTokenValid).post(Entity.entity(submitRequest, MediaType.APPLICATION_JSON_TYPE), ExamSubmitResponse.class);
        });
    }

    @Test
    public void submitExamDifferentUserIDTest() throws GeneralSecurityException, IOException, InvalidRequestException {
        String submitRequest = "{ \"examInstance\":{ \"id\":10, \"examID\":10, \"studentRollNum\":10," +
                " \"answers\":{ \"answersList\":[ {\"answer\":\"Answer1\",\"questionNum\":1},{\"answer\":\"Answer2\",\"questionNum\":2} ] } } }";
        String differentIDToken = "differentIDToken";
        String differentUserID = "differentUserID";
        when(tokenService.firebaseVerifyIDToken(differentIDToken)).thenReturn(differentUserID);
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/submit").request().header("Authentication", differentIDToken).post(Entity.entity(submitRequest, MediaType.APPLICATION_JSON_TYPE), ExamSubmitResponse.class);
        });
    }

    @Test
    public void submitExamDifferentStudentRollNumberTest() {
        String submitRequest = "{ \"examInstance\":{ \"id\":10, \"examID\":10, \"studentRollNum\":12," +
                " \"answers\":{ \"answersList\":[ {\"answer\":\"Answer1\",\"questionNum\":1},{\"answer\":\"Answer2\",\"questionNum\":2} ] } } }";
        assertThrows(BadRequestException.class, () -> {
            ext.target("/exam/submit").request().header("Authentication", IDTokenValid).post(Entity.entity(submitRequest, MediaType.APPLICATION_JSON_TYPE), ExamSubmitResponse.class);
        });
    }

    @Test
    public void submitExamFailAddResponseRequest() throws GeneralSecurityException, InvalidDatabaseDataException, InvalidRequestException, IOException {
        String differentSubmitExamRequest = "{ \"examInstance\":{ \"id\":11, \"examID\":10, \"studentRollNum\":20," +
                " \"answers\":{ \"answersList\":[ {\"answer\":\"Answer1\",\"questionNum\":1},{\"answer\":\"Answer2\",\"questionNum\":2} ] } } }";
        ExamInstance differentExamInstance = new ExamInstance(11, 10, "userID", 20, null, null);
        differentExamInstance.setAnswers(submitExamInstance.getAnswers());

        when(examInstanceDao.getExamInstanceByExamInstanceID(11)).thenReturn(new ExamInstance(10, "userID", 20));
        doThrow(GeneralSecurityException.class).doThrow(InvalidDatabaseDataException.class).doThrow(InvalidRequestException.class).doThrow(IOException.class).when(spreadsheetService).addResponseRequest(differentExamInstance, user, examMetadata);

        for (int i = 0; i < 4; i++) {
            assertThrows(BadRequestException.class, () -> {
                ext.target("/exam/submit").request().header("Authentication", IDTokenValid).post(Entity.entity(differentSubmitExamRequest, MediaType.APPLICATION_JSON_TYPE), ExamSubmitResponse.class);
                verify(examInstanceDao).updateExamInstanceEndTime(11);
                verify(answerDao).insertAnswers(11, "{\"answersList\":[{\"answer\":\"Answer1\",\"questionNum\":1},{\"answer\":\"Answer2\",\"questionNum\":2}]}");
                verify(spreadsheetService).addResponseRequest(differentExamInstance, user, examMetadata);
            });
        }

    }

}
