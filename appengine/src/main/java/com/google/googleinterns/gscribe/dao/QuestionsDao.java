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

import com.google.googleinterns.gscribe.models.MultipleChoiceQuestion;
import com.google.googleinterns.gscribe.models.Question;
import com.google.googleinterns.gscribe.models.QuestionType;
import com.google.googleinterns.gscribe.models.SubjectiveQuestion;
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

public interface QuestionsDao {

    /**
     * Inserts a list of all questions of a particular exam to the database
     * A JSON object of the question is inserted for the question
     *
     * @param question    ( question JSON string )
     * @param examID      ( to identify particular exam )
     * @param questionNum ( question number of corresponding question )
     */
    @SqlBatch("insert into questions( exam_id, question, question_num ) values ( :examID, :question, :questionNum )")
    void insertExamQuestions(@Bind("question") List<String> question, @Bind("examID") int examID, @Bind("questionNum") List<Integer> questionNum);

    /**
     * Queries all the questions of the exam identified by exam id examID
     *
     * @param examID ( to identify particular exam )
     * @return list of question objects
     */
    @Mapper(ExamMapper.class)
    @SqlQuery("SELECT * from questions where exam_id = :exam_id")
    List<Question> getExamQuestions(@Bind("exam_id") int examID);

    /**
     * A Mapper class to map a question response to question object
     * Mapping is done based on the question type
     */
    class ExamMapper implements ResultSetMapper<Question> {
        @Override
        public Question map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            Question question = new Gson().fromJson(resultSet.getString("question"), Question.class);
            if (question.getType().equals(QuestionType.MCQ)) {
                return new Gson().fromJson(resultSet.getString("question"), MultipleChoiceQuestion.class);
            } else if (question.getType().equals(QuestionType.SUBJECTIVE)) {
                return new Gson().fromJson(resultSet.getString("question"), SubjectiveQuestion.class);
            }
            return null;
        }
    }

}
