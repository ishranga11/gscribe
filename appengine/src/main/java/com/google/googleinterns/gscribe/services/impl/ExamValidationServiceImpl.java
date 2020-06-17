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

import com.google.googleinterns.gscribe.resources.io.exception.ExamFormatException;
import com.google.googleinterns.gscribe.services.ExamValidationService;
import com.google.googleinterns.gscribe.services.data.ExamSource;

import java.util.List;

public class ExamValidationServiceImpl implements ExamValidationService {

    /**
     * in the exam template duration is to be mentioned in B1
     * check that in B1 duration is mentioned in proper format and in range 1-300
     *
     * @param row1 ( row 1 of spreadsheet )
     */
    private void validateDuration(List<Object> row1) {
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
     * check that the question has statement in column 2
     * check that the question has options in column 3-6
     *
     * @param question    ( a list containing question )
     * @param questionRow ( row in which this question lies )
     */
    private void checkMultipleChoiceQuestion(List<Object> question, int questionRow) {
        if (question.get(1).equals("")) throw new ExamFormatException("missing question statement B" + questionRow);
        for (int i = 2; i < 6; i++) {
            if (question.get(i).equals(""))
                throw new ExamFormatException("missing multiple choice question option in row " + questionRow);
        }
    }

    /**
     * check that the question has statement in column 2
     * check that the question has no options in column 3-6
     *
     * @param question    ( a list containing question )
     * @param questionRow ( row in which this question lies )
     */
    private void checkSubjectiveQuestion(List<Object> question, int questionRow) {
        if (question.get(1).equals("")) throw new ExamFormatException("missing question statement B" + questionRow);
        for (int i = 2; i < 6; i++) {
            if (!question.get(i).equals(""))
                throw new ExamFormatException("subjective question does not expect option in row " + questionRow);
        }
    }

    /**
     * check that the points are in proper format
     * check that the points lie in range of 1-100
     *
     * @param pointsString ( points mentioned for question in sheet )
     * @param questionRow  ( row in which this question lies )
     */
    private void validatePoints(String pointsString, int questionRow) {
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
     * check that the size of sheet is at least 3
     * validate duration
     * validate each question
     *
     * @param examSource ( contains instance of exam sheet )
     */
    @Override
    public void validate(ExamSource examSource) {
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

}
