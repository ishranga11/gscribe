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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(using = Questions.QuestionsDeserializer.class)
public class Questions {

    private List<Question> questionsList;

    public Questions() {
    }

    public List<Question> getQuestionsList() {
        return questionsList;
    }

    public void setQuestionsList(List<Question> questionsList) {
        this.questionsList = questionsList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Questions)) return false;
        Questions questions = (Questions) o;
        return questionsList.equals(questions.questionsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionsList);
    }

    static class QuestionsDeserializer extends JsonDeserializer<Questions> {

        @Override
        public Questions deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(p);
            JsonNode questions = node.get("questionsList");
            Questions questionsList = new Questions();
            questionsList.setQuestionsList(new ArrayList<>());
            for (JsonNode question : questions) {
                if (question.get("type").asText().equals("MCQ")) {
                    questionsList.getQuestionsList().add(objectMapper.treeToValue(question, MultipleChoiceQuestion.class));
                } else if (question.get("type").asText().equals("SUBJECTIVE")) {
                    questionsList.getQuestionsList().add(objectMapper.treeToValue(question, SubjectiveQuestion.class));
                }
            }
            return questionsList;
        }
    }

}
