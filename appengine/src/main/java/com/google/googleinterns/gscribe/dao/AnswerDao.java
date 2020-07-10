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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.googleinterns.gscribe.models.Answers;
import com.google.inject.Inject;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface AnswerDao {

    /**
     * Queries list of all answers for a particular exam instance identified by an exam instance id examInstanceID
     *
     * @param examInstanceID ( to identify particular exam instance )
     * @return List of answers
     */
    @SqlQuery("SELECT * from answers where exam_instance_id = :exam_instance_id")
    Answers getAnswersByExamInstanceID(@Bind("exam_instance_id") int examInstanceID);

    /**
     * Inserts All responses for a corresponding exam instance
     *
     * @param examInstanceID ( to identify particular exam instance )
     * @param answersJSON    ( answers object JSON containing all answers list )
     */
    @SqlUpdate("INSERT INTO answers ( exam_instance_id, answers ) VALUES ( :exam_instance_id, :answers )")
    void insertAnswers(@Bind("exam_instance_id") int examInstanceID, @Bind("answers") String answersJSON);

    class AnswersMapper implements ResultSetMapper<Answers> {

        ObjectMapper objectMapper;

        @Inject
        public AnswersMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Answers map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            try {
                return objectMapper.readValue(resultSet.getString("answers"), Answers.class);
            } catch (JsonProcessingException e) {
                throw new SQLException("broken answer format in database");
            }
        }

    }

}
