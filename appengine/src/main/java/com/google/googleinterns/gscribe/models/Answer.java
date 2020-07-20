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

public class Answer {

    private String answer;
    private int questionNum;

    public Answer() {
    }

    public Answer(String answer, int questionNum) {
        this.answer = answer;
        this.questionNum = questionNum;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getQuestionNum() {
        return questionNum;
    }

    public void setQuestionNum(int questionNum) {
        this.questionNum = questionNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Answer)) return false;
        Answer answer1 = (Answer) o;
        return questionNum == answer1.questionNum &&
                answer.equals(answer1.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answer, questionNum);
    }
}
