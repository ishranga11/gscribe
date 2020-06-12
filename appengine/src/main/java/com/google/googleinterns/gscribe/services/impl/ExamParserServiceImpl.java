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

import com.google.googleinterns.gscribe.models.Exam;
import com.google.googleinterns.gscribe.models.ExamMetadata;
import com.google.googleinterns.gscribe.services.ExamParserService;
import com.google.googleinterns.gscribe.utils.ExamGenerator;
import com.google.googleinterns.gscribe.utils.ExamValidator;
import com.google.googleinterns.gscribe.utils.GetExamFromSpreadsheet;

import java.util.List;

public class ExamParserServiceImpl implements ExamParserService {

    private final ExamValidator examValidator;
    private final ExamGenerator examGenerator;
    private final GetExamFromSpreadsheet getExamFromSpreadsheet;

    public ExamParserServiceImpl(ExamValidator examValidator, ExamGenerator examGenerator, GetExamFromSpreadsheet getExamFromSpreadsheet) {
        this.examGenerator = examGenerator;
        this.examValidator = examValidator;
        this.getExamFromSpreadsheet = getExamFromSpreadsheet;
    }

    @Override
    public List<List<Object>> getExam(ExamMetadata metadata) {
        return null;
    }

    @Override
    public Exam generateExam(List<List<Object>> examObject, ExamMetadata metadata) {
        return null;
    }

    @Override
    public String validateExam(List<List<Object>> examObject) {
        return null;
    }
}
