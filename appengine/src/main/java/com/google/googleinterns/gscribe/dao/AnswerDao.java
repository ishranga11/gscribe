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

import com.google.googleinterns.gscribe.models.Answer;
import com.google.gson.Gson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface AnswerDao {

    /**
     * Queries list of all answers for a particular exam instance identified by an exam instance id examInstanceID
     *
     * @param examInstanceID ( to identify particular exam instance )
     * @return List of answers
     */
    @Mapper(AnswerDao.AnswerMapper.class)
    @SqlQuery("SELECT * from answers where exam_instance_id = :exam_instance_id")
    List<Answer> getAnswersByExamInstanceID(@Bind("exam_instance_id") int examInstanceID);

    /**
     * Called to insert answers into the database for an exam instance ID
     * Inserts All responses for a corresponding exam instance
     *
     * @param examInstanceID ( to identify particular exam instance )
     * @param questionNumber ( question number of corresponding answer )
     * @param answers        ( answer JSON )
     */
    @SqlBatch("INSERT INTO answers ( exam_instance_id, answer, question_num ) VALUES ( :exam_instance_id, :answer, :question_num )")
    void insertAnswers(@Bind("exam_instance_id") int examInstanceID, @Bind("question_num") List<Integer> questionNumber, @Bind("answer") List<String> answers);

    /**
     * A Mapper class to map answer JSON object from MySQL database to Answer class
     */
    class AnswerMapper implements ResultSetMapper<Answer> {
        @Override
        public Answer map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new Gson().fromJson(resultSet.getString("answer"), Answer.class);
        }
    }

}
