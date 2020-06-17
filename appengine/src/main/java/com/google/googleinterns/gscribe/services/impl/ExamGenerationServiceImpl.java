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

import com.google.googleinterns.gscribe.models.*;
import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;
import com.google.googleinterns.gscribe.services.ExamGenerationService;
import com.google.googleinterns.gscribe.services.data.ExamSource;

import java.util.ArrayList;
import java.util.List;

public class ExamGenerationServiceImpl implements ExamGenerationService {

    /**
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
     * reads the points of the question
     * reads the statement of question
     * return subjective question object
     *
     * @param questionObject ( question instance from sheet )
     * @param questionNumber ( number of this question in order of questions )
     * @return SubjectiveQuestion object for this question
     */
    private Question createSubjectiveQuestion(List<Object> questionObject, int questionNumber) {
        int points = Integer.parseInt(questionObject.get(6).toString());
        String questionStatement = questionObject.get(1).toString();
        return new SubjectiveQuestion(questionStatement, points, questionNumber);
    }

    /**
     * reads the points of the question
     * reads the statement of the question
     * reads the options of the question
     * returns the multiple choice question object
     *
     * @param questionObject ( question instance from sheet )
     * @param questionNumber ( number of this question in order of questions )
     * @return MultipleChoiceQuestion object for this question
     */
    private Question CreateMultipleChoiceQuestion(List<Object> questionObject, int questionNumber) {
        int points = Integer.parseInt(questionObject.get(6).toString());
        String questionStatement = questionObject.get(1).toString();
        List<String> options = new ArrayList<>();
        for (int i = 2; i < 6; i++) options.add(questionObject.get(i).toString());
        return new MultipleChoiceQuestion(questionStatement, points, questionNumber, options);
    }

    /**
     * makes examMetadata object
     * makes an arraylist of questions
     * returns exam object
     *
     * @param examSource ( contains sheet instance containing exam )
     * @param request    ( contains spreadsheetId and sheetName )
     * @param userID     ( unique user ID of user )
     * @return Exam object
     */
    @Override
    public Exam generate(ExamSource examSource, ExamRequest request, String userID) {
        ExamMetadata metadata = generateMetadata(examSource, request, userID);
        List<Question> questions = new ArrayList<>();
        List<List<Object>> exam = examSource.getExam();
        int questionNumber;
        for (int i = 2; i < exam.size(); i++) {
            questionNumber = i - 1;
            List<Object> currentQuestion = exam.get(i);
            if (currentQuestion.get(0).equals("MCQ"))
                questions.add(CreateMultipleChoiceQuestion(currentQuestion, questionNumber));
            else if (currentQuestion.get(0).equals("SUBJECTIVE"))
                questions.add(createSubjectiveQuestion(currentQuestion, questionNumber));
        }
        return new Exam(metadata, questions);
    }
}
