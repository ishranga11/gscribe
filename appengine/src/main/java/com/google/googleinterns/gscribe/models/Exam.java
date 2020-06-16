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

package com.google.googleinterns.gscribe.models;

import java.util.ArrayList;

public class Exam {

    private ExamMetadata examMetadata;
    private ArrayList<Question> questions;

    public Exam(ExamMetadata examMetadata, ArrayList<Question> questions) {
        this.examMetadata = examMetadata;
        this.questions = questions;
    }

    public ExamMetadata getExamMetadata() {
        return examMetadata;
    }

    public void setExamMetadata(ExamMetadata examMetadata) {
        this.examMetadata = examMetadata;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }
}
