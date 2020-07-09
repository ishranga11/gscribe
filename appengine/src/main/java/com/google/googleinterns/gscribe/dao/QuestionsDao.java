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
import com.google.googleinterns.gscribe.models.Questions;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface QuestionsDao {

    /**
     * Inserts a JSON of all questions of a particular exam to the database for a given examID
     *
     * @param examID    ( to identify particular exam )
     * @param questions ( all questions list JSON )
     */
    @SqlBatch("insert into questions( exam_id, questions ) values ( :examID, :question )")
    void insertExamQuestions(@Bind("examID") int examID, @Bind("questions") String questions);

    /**
     * Queries the questions list JSON of the exam identified by exam id examID
     *
     * @param examID ( to identify particular exam )
     * @return questions object
     */
    @Mapper(ExamMapper.class)
    @SqlQuery("SELECT * from questions where exam_id = :exam_id")
    Questions getExamQuestions(@Bind("exam_id") int examID);

    /**
     * A Mapper class to map a questions list response to questions object
     */
    class ExamMapper implements ResultSetMapper<Questions> {
        @Override
        public Questions map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(resultSet.getString("questions"), Questions.class);
            } catch (JsonProcessingException e) {
                throw new SQLException("broken question format in database");
            }
        }
    }

}
