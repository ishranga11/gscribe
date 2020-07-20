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

package com.google.googleinterns.gscribe.resources.io.response;

import com.google.googleinterns.gscribe.models.Exam;

public class ExamInstanceResponse {

    private Exam exam;
    private int examInstanceID;

    public ExamInstanceResponse() {
    }

    public ExamInstanceResponse(Exam exam, int examInstanceID) {
        this.examInstanceID = examInstanceID;
        this.exam = exam;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public int getExamInstanceID() {
        return examInstanceID;
    }

    public void setExamInstanceID(int examInstanceID) {
        this.examInstanceID = examInstanceID;
    }
}
