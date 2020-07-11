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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.googleinterns.gscribe.models.Answers;
import com.google.googleinterns.gscribe.provider.DBIProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnswersDaoTest {

    private static final int EXAM_INSTANCE_ID = 100;
    private static Handle handle;
    private static AnswerDao answerDao;

    @BeforeAll
    public static void init() throws IOException {
        DBI dbi = new DBIProvider().getDBI();
        handle = dbi.open();

        handle.insert("delete from exam_instance where id = " + EXAM_INSTANCE_ID);
        handle.insert("delete from exam where id = 1");
        handle.insert("delete from user where id = 'user'");
        handle.insert("insert into user(id,access_token,refresh_token) values ( 'user','a_token','r_token')");
        handle.insert("insert into exam(id,created_by,spreadsheet_id,duration) values ( 1 ,'user','spreadsheet_id',100)");
        handle.insert("insert into exam_instance(id,exam_id,user_id,student_roll_num) values (100,1,'user_id',20)");

        dbi.registerMapper(new AnswerDao.AnswersMapper(new ObjectMapper()));
        answerDao = dbi.onDemand(AnswerDao.class);
    }

    @BeforeEach
    public void setUp() {
        handle.insert("delete from answers where exam_instance_id=" + EXAM_INSTANCE_ID);
    }

    @Test
    public void addAnswersTest() {

        int examInstanceID = EXAM_INSTANCE_ID;
        String answersJSON = "{\"answers\": [{\"answer\": \"ANSWER1\", \"questionNumber\": 1}, {\"answer\": \"ANSWER2\", \"questionNumber\": 2}]}";

        answerDao.insertAnswers(examInstanceID, answersJSON);
        List<Map<String, Object>> list = handle.createQuery("select * from answers where exam_instance_id=" + examInstanceID).list();

        assertFalse(list.isEmpty());
        Map<String, Object> addedAnswer = list.get(0);
        assertEquals(examInstanceID, addedAnswer.get("exam_instance_id"));
        assertEquals(answersJSON, addedAnswer.get("answers"));
    }

    @Test
    public void addAnswerFailingExamInstanceIDForeignKeyTest() {

        Assertions.assertThrows(UnableToExecuteStatementException.class, () -> {
            int examInstanceID = 400;
            String answersJSON = "{\"answers\": [{\"answer\": \"ANSWER1\", \"questionNumber\": 1}, {\"answer\": \"ANSWER2\", \"questionNumber\": 2}]}";

            answerDao.insertAnswers(examInstanceID, answersJSON);
        });

    }

    @Test
    void addAnswerForSameExamInstanceIDTwiceTest() {

        assertThrows(UnableToExecuteStatementException.class, () -> {
            int examInstanceID = EXAM_INSTANCE_ID;
            String answersJSON = "{\"answers\": [{\"answer\": \"ANSWER1\", \"questionNumber\": 1}, {\"answer\": \"ANSWER2\", \"questionNumber\": 2}]}";
            String answersJSON2 = "{\"answers\": [{\"answer\": \"ANSWER21\", \"questionNumber\": 1}, {\"answer\": \"ANSWER22\", \"questionNumber\": 2}]}";

            answerDao.insertAnswers(examInstanceID, answersJSON);
            answerDao.insertAnswers(examInstanceID, answersJSON2);

        });

    }

    @Test
    public void getAnswersTest() {
        int examInstanceID = EXAM_INSTANCE_ID;
        String answersJSON = "{\"answers\": [{\"answer\": \"ANSWER1\", \"questionNum\": 1}, {\"answer\": \"ANSWER2\", \"questionNum\": 2}]}";

        handle.insert("insert into answers(exam_instance_id,answers) values ( " + examInstanceID + ", '" + answersJSON + "' )");
        Answers answers = answerDao.getAnswersByExamInstanceID(examInstanceID);

        assertNotEquals(null, answers);
        assertEquals(2, answers.getAnswersList().size());
        assertNotNull(answers.getAnswersList().get(0));
        assertNotNull(answers.getAnswersList().get(1));

        assertEquals("ANSWER1", answers.getAnswersList().get(0).getAnswer());
        assertEquals("ANSWER2", answers.getAnswersList().get(1).getAnswer());
        assertEquals(1, answers.getAnswersList().get(0).getQuestionNum());
        assertEquals(2, answers.getAnswersList().get(1).getQuestionNum());
    }

    @Test
    public void getAnswersWithNotUsedExamInstanceIDTest() {
        Answers answers = answerDao.getAnswersByExamInstanceID(EXAM_INSTANCE_ID);
        assertNull(answers);
    }

}
