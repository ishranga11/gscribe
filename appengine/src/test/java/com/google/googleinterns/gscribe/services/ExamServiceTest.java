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
import com.google.googleinterns.gscribe.models.MultipleChoiceQuestion;
import com.google.googleinterns.gscribe.models.QuestionType;
import com.google.googleinterns.gscribe.models.User;
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


    @Test
    public void createNewExamTest() throws GeneralSecurityException, InvalidDatabaseDataException, InvalidRequestException, IOException, ExamFormatException {

        List<List<Object>> valueRangeDataWhole = new ArrayList<>();
        valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
        valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
        valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
        valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
        ValueRange valueRangeWhole = new ValueRange();
        valueRangeWhole.setValues(valueRangeDataWhole);

        when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
        when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

        Exam exam = examService.getExam(examRequest, user);

        assertEquals(exam.getExamMetadata().getSpreadsheetID(), spreadsheetId);
        assertEquals(exam.getExamMetadata().getDuration(), 100);
        assertEquals(exam.getExamMetadata().getUserID(), user.getId());
        assertEquals(exam.getQuestions().getQuestionsList().size(), 2);
        assertEquals(exam.getQuestions().getQuestionsList().get(0).getType(), QuestionType.SUBJECTIVE);
        assertEquals(exam.getQuestions().getQuestionsList().get(0).getQuestionNumber(), 1);
        assertEquals(exam.getQuestions().getQuestionsList().get(0).getPoints(), 2);
        assertEquals(exam.getQuestions().getQuestionsList().get(0).getStatement(), valueRangeDataWhole.get(2).get(1));
        assertEquals(exam.getQuestions().getQuestionsList().get(1).getType(), QuestionType.MCQ);
        assertEquals(exam.getQuestions().getQuestionsList().get(1).getQuestionNumber(), 2);
        assertEquals(exam.getQuestions().getQuestionsList().get(1).getPoints(), 3);
        assertEquals(exam.getQuestions().getQuestionsList().get(1).getStatement(), valueRangeDataWhole.get(3).get(1));
        assertEquals(((MultipleChoiceQuestion) exam.getQuestions().getQuestionsList().get(1)).getOptions().size(), 4);
        assertEquals(((MultipleChoiceQuestion) exam.getQuestions().getQuestionsList().get(1)).getOptions().get(0), valueRangeDataWhole.get(3).get(2));
        assertEquals(((MultipleChoiceQuestion) exam.getQuestions().getQuestionsList().get(1)).getOptions().get(1), valueRangeDataWhole.get(3).get(3));
        assertEquals(((MultipleChoiceQuestion) exam.getQuestions().getQuestionsList().get(1)).getOptions().get(2), valueRangeDataWhole.get(3).get(4));
        assertEquals(((MultipleChoiceQuestion) exam.getQuestions().getQuestionsList().get(1)).getOptions().get(3), valueRangeDataWhole.get(3).get(5));
    }

    @Test
    public void missingDurationTest() {

        ExamFormatException exception = assertThrows(ExamFormatException.class, () -> {
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataColumnA = new ArrayList<>();
            valueRangeDataColumnA.add(Arrays.asList(new String[]{"Duration of exam:"}));
            valueRangeDataColumnA.add(Arrays.asList(new String[]{"Question Type"}));
            ValueRange valueRangeColumnA = new ValueRange();
            valueRangeColumnA.setValues(valueRangeDataColumnA);

            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "asd"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "-10"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "1000"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"subjective", "SubjectiveStatement1", "", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "optionA", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "optionB", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "optionC", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "optionD", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "sd2"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "-1"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
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
            List<List<Object>> valueRangeDataWhole = new ArrayList<>();
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Duration of exam:", "100"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"Question Type", "Question statement", "Option A", "Option B", "Option C", "Option D", "Points"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"SUBJECTIVE", "SubjectiveStatement1", "", "", "", "", "1000"}));
            valueRangeDataWhole.add(Arrays.asList(new String[]{"MCQ", "MCQStatement1", "OptionA", "OptionB", "OptionC", "OptionD", "3"}));
            ValueRange valueRangeWhole = new ValueRange();
            valueRangeWhole.setValues(valueRangeDataWhole);

            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A:A")).thenReturn(valueRangeForNumberOfQuestions);
            when(spreadsheetService.parseSpreadsheetRequest(user, spreadsheetId, sheetName + "!A1:G4")).thenReturn(valueRangeWhole);

            examService.getExam(examRequest, user);
        });
        assertEquals("Exam format Exception: points not in a valid range of 1-100 in G3", exception.getMessage());

    }


}
