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

import com.google.googleinterns.gscribe.services.ExamValidationService;
import com.google.googleinterns.gscribe.services.data.ExamSource;

import java.util.List;

public class ExamValidationServiceImpl implements ExamValidationService {

    private void validateDuration(List<Object> row1) throws RuntimeException {
        if (row1.size() < 2) throw new RuntimeException();
        String durationString = row1.get(1).toString();
        int duration = Integer.parseInt(durationString);
        if (duration <= 0 || duration > 300) throw new RuntimeException();
    }

    private void checkMultipleChoiceQuestion(List<Object> question) throws RuntimeException {
        if (question.get(1).equals("")) throw new RuntimeException();
        for (int i = 2; i < 6; i++) {
            if (question.get(i).equals("")) throw new RuntimeException();
        }
    }

    private void checkSubjectiveQuestion(List<Object> question) throws RuntimeException {
        if (question.get(1).equals("")) throw new RuntimeException();
        for (int i = 2; i < 6; i++) {
            if (!question.get(i).equals("")) throw new RuntimeException();
        }
    }

    private void validatePoints(String pointsString) throws RuntimeException {
        int points = Integer.parseInt(pointsString);
        if (points <= 0 || points > 100) throw new RuntimeException();
    }

    @Override
    public void validate(ExamSource examSource) throws RuntimeException {
        List<List<Object>> exam = examSource.getExam();
        if (exam.size() < 3) throw new RuntimeException();
        validateDuration(exam.get(0));
        for (int i = 2; i < exam.size(); i++) {
            List<Object> currentQuestion = exam.get(i);
            if (currentQuestion == null || currentQuestion.size() != 7) throw new RuntimeException();
            if (currentQuestion.get(0).equals("MCQ")) checkMultipleChoiceQuestion(currentQuestion);
            else if (currentQuestion.get(0).equals("SUBJECTIVE")) checkSubjectiveQuestion(currentQuestion);
            else throw new RuntimeException();
            validatePoints(currentQuestion.get(6).toString());
        }
    }

}
