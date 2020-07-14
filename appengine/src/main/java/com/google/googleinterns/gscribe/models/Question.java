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

import java.util.Objects;

public class Question {

    private QuestionType type;
    private String statement;
    private int points;
    private int questionNumber;

    public Question() {
    }

    public Question(QuestionType type, String statement, int points, int questionNumber) {
        this.type = type;
        this.statement = statement;
        this.points = points;
        this.questionNumber = questionNumber;
    }

    public QuestionType getType() {
        return type;
    }

    public String getStatement() {
        return statement;
    }

    public int getPoints() {
        return points;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question question = (Question) o;
        return getPoints() == question.getPoints() &&
                getQuestionNumber() == question.getQuestionNumber() &&
                getType() == question.getType() &&
                getStatement().equals(question.getStatement());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getStatement(), getPoints(), getQuestionNumber());
    }
}
