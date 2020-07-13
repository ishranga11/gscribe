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

package com.google.googleinterns.gscribe.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.googleinterns.gscribe.models.MultipleChoiceQuestion;
import com.google.googleinterns.gscribe.models.Questions;
import com.google.googleinterns.gscribe.models.SubjectiveQuestion;
import com.google.inject.Inject;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public interface QuestionsDao {

    /**
     * Inserts a JSON of all questions of a particular exam to the database for a given examID
     *
     * @param examID    ( to identify particular exam )
     * @param questions ( all questions list JSON )
     */
    @SqlUpdate("insert into questions ( exam_id, questions ) values ( :examID, :questions )")
    void insertExamQuestions(@Bind("examID") int examID, @Bind("questions") String questions);

    /**
     * Queries the questions list JSON of the exam identified by exam id examID
     *
     * @param examID ( to identify particular exam )
     * @return questions object
     */
    @SqlQuery("SELECT * from questions where exam_id = :exam_id")
    Questions getExamQuestions(@Bind("exam_id") int examID);

    class QuestionsMapper implements ResultSetMapper<Questions> {

        private final ObjectMapper objectMapper;

        @Inject
        public QuestionsMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Questions map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            try {
                JsonNode node = objectMapper.readTree(resultSet.getString("questions"));
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
            } catch (JsonProcessingException e) {
                throw new SQLException("broken question format in database");
            }
        }

    }

}
