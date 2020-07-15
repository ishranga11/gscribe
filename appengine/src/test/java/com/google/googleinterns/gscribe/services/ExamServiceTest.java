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
import com.google.googleinterns.gscribe.models.*;
import com.google.googleinterns.gscribe.resources.io.exception.ExamFormatException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidDatabaseDataException;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;
import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;
import com.google.googleinterns.gscribe.services.impl.ExamServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExamServiceTest {

    private final static User user = new User("user", "access_token", "refresh_token", null);
    private final static String spreadsheetId = "spreadsheet_id";
    private final static String sheetName = "sheet_name";
    private final static ExamRequest examRequest = new ExamRequest();
    private final static ValueRange valueRangeForNumberOfQuestions = new ValueRange();
    private final static ValueRange valueRangeWhole = new ValueRange();

    @Mock
    SpreadsheetService spreadsheetService;
    @InjectMocks
    ExamServiceImpl examService;

    @BeforeAll
    private static void init() {
        examRequest.setSheetName(sheetName);
        examRequest.setSpreadsheetID(spreadsheetId);
        List<List<Object>> valueRangeDataFoNumberOfQuestions = new ArrayList<>();
        valueRangeDataFoNumberOfQuestions.add(Arrays.asList(new String[]{"Duration of exam:"}));
        valueRangeDataFoNumberOfQuestions.add(Arrays.asList(new String[]{"Question Type"}));
        valueRangeDataFoNumberOfQuestions.add(Arrays.asList(new String[]{"SUBJECTIVE"}));
        valueRangeDataFoNumberOfQuestions.add(Arrays.asList(new String[]{"MCQ"}));
        valueRangeForNumberOfQuestions.setValues(valueRangeDataFoNumberOfQuestions);
    }

    private List<List<Object>> getBaseQuestionPaper() {
        List<List<Object>> baseQuestionPaper = new ArrayList<>();
        baseQuestionPaper.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
        baseQuestionPaper.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
        baseQuestionPaper.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
        baseQuestionPaper.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
        return baseQuestionPaper;
    }

    @Test
    public void createNewExamTest() throws GeneralSecurityException, InvalidDatabaseDataException, InvalidRequestException, IOException, ExamFormatException {

        List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
        valueRangeWhole.setValues(valueRangeDataWhole);
        ExamMetadata actualExamMetadata = new ExamMetadata(spreadsheetId, sheetName, user.getId(), 100);
        Questions actualQuestions = new Questions();
        SubjectiveQuestion actualSubjectiveQuestion = new SubjectiveQuestion("SubjectiveStatement1", 2, 1);
        List<String> actualOptions = new ArrayList<>();
        actualOptions.add("OptionA");
        actualOptions.add("OptionB");
        actualOptions.add("OptionC");
        actualOptions.add("OptionD");
        MultipleChoiceQuestion actualMultipleChoiceQuestion = new MultipleChoiceQuestion("MCQStatement1", 3, 2, actualOptions);
        List<Question> actualQuestionList = new ArrayList<>();
        actualQuestionList.add(actualSubjectiveQuestion);
        actualQuestionList.add(actualMultipleChoiceQuestion);
        actualQuestions.setQuestionsList(actualQuestionList);
        Exam actualExam = new Exam(actualExamMetadata, actualQuestions);

        when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
        when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

        Exam exam = examService.getExam(examRequest, user);

        assertEquals(actualExam, exam);
    }

    @Test
    public void missingDurationTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();

            valueRangeDataWhole.set(0, Arrays.asList(new String[]{"Duration of exam:"}));
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: duration not present in B1", exception.getMessage());

    }

    @Test
    public void EmptyExamTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();

            List<List<Object>> valueRangeDataColumnA = new ArrayList<>();
            valueRangeDataColumnA.add(Arrays.asList(new String[]{"Duration of exam:"}));
            valueRangeDataColumnA.add(Arrays.asList(new String[]{"Question Type"}));
            ValueRange valueRangeColumnA = new ValueRange();
            valueRangeColumnA.setValues(valueRangeDataColumnA);

            valueRangeDataWhole.remove(3);
            valueRangeDataWhole.remove(2);
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeColumnA);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G2")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: Improper exam template used", exception.getMessage());

    }

    @Test
    public void wrongDurationFormatTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(0).set(1, "asd");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: duration not in a proper format in B1", exception.getMessage());

    }

    @Test
    public void durationInNegativeTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(0).set(1, "-10");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: duration not in a range of 1-300 in B1", exception.getMessage());

    }

    @Test
    public void durationOutOfRangeTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(0).set(1, "1000");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: duration not in a range of 1-300 in B1", exception.getMessage());

    }

    @Test
    public void wrongQuestionTypeTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(2).set(0, "fill_in_the_blanks");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: Question type not identified at A3", exception.getMessage());

    }

    @Test
    public void questionWithoutStatementTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(3).set(1, "");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: missing question statement B4", exception.getMessage());

    }

    @Test
    public void subjectiveQuestionWithOptionsATest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(2).set(2, "optionA");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: subjective question does not expect option in row 3", exception.getMessage());

    }

    @Test
    public void subjectiveQuestionWithOptionsBTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(2).set(3, "optionB");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: subjective question does not expect option in row 3", exception.getMessage());

    }

    @Test
    public void subjectiveQuestionWithOptionsCTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(2).set(4, "optionC");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: subjective question does not expect option in row 3", exception.getMessage());

    }

    @Test
    public void subjectiveQuestionWithOptionsDTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(2).set(5, "optionD");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: subjective question does not expect option in row 3", exception.getMessage());

    }

    @Test
    public void multipleChoiceQuestionWithoutOptionATest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(3).set(2, "");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: missing multiple choice question option in row 4", exception.getMessage());

    }

    @Test
    public void multipleChoiceQuestionWithoutOptionBTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(3).set(3, "");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: missing multiple choice question option in row 4", exception.getMessage());

    }

    @Test
    public void multipleChoiceQuestionWithoutOptionCTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(3).set(4, "");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: missing multiple choice question option in row 4", exception.getMessage());

    }

    @Test
    public void multipleChoiceQuestionWithoutOptionDTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(3).set(5, "");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: missing multiple choice question option in row 4", exception.getMessage());

    }

    @Test
    public void pointsIncorrectFormatTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(2).set(6, "asd");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: points not in a proper format in G3", exception.getMessage());

    }

    @Test
    public void negativePointsTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(2).set(6, "-1");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: points not in a valid range of 1-100 in G3", exception.getMessage());

    }

    @Test
    public void pointsOutOfRangeTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = getBaseQuestionPaper();
            valueRangeDataWhole.get(2).set(6, "1000");
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: points not in a valid range of 1-100 in G3", exception.getMessage());

    }


}
