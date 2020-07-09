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
import com.google.googleinterns.gscribe.models.Answer;
import com.google.googleinterns.gscribe.models.Answers;
import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.skife.jdbi.v2.DBI;

import java.util.ArrayList;
import java.util.List;

public class AnswerDaoTest {

    @Inject
    ObjectMapper objectMapper;
    private AnswerDao answerDao;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        DBI dbi = new DBI("jdbc:mysql://localhost:3306/gscribe_test", "root", "pass");
        this.answerDao = dbi.onDemand(AnswerDao.class);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void getAnswersByExamInstanceID() {

    }

    @org.junit.jupiter.api.Test
    void insertAnswers() throws JsonProcessingException {
        List<Answer> answerList = new ArrayList<>();
        answerList.add(new Answer("answer1", 1));
        answerList.add(new Answer("answer2", 2));
        answerList.add(new Answer("answer3", 3));
        Answers answers = new Answers(answerList);
        String answersJSON = objectMapper.writeValueAsString(answers);
        answerDao.insertAnswers(20, answersJSON);
        Answers returnedAnswerObject = answerDao.getAnswersByExamInstanceID(20);
        Assertions.assertEquals(returnedAnswerObject, answers);
    }
}
