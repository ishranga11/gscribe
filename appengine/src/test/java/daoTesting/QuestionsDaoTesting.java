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

package daoTesting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.googleinterns.gscribe.dao.QuestionsDao;
import com.google.googleinterns.gscribe.models.MultipleChoiceQuestion;
import com.google.googleinterns.gscribe.models.Questions;
import com.google.googleinterns.gscribe.models.SubjectiveQuestion;
import module.DBIProvider;
import org.junit.jupiter.api.*;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionsDaoTesting {

    static Handle handle;
    static DBI dbi;
    static int EXAM_ID = 100;
    static private QuestionsDao questionsDao;

    @BeforeAll
    public static void init() {
        dbi = new DBIProvider().getDBI();
        dbi.registerMapper(new QuestionsDao.QuestionsMapper(new ObjectMapper()));
        questionsDao = dbi.onDemand(QuestionsDao.class);
        handle = dbi.open();
        handle.insert("delete from exam where id = " + EXAM_ID);
        handle.insert("delete from user where id = 'user'");
        handle.insert("insert into user(id,access_token,refresh_token) values ( 'user','a_token','r_token')");
        handle.insert("insert into exam(id,created_by,spreadsheet_id,duration) values ( " + EXAM_ID + " ,'user','spreadsheet_id',100)");
        handle.close();
    }

    @AfterAll
    public static void tearDownAll() {
        handle = dbi.open();
        handle.insert("delete from exam where id=" + EXAM_ID);
        handle.insert("delete from user where id = 'user'");
        handle.close();
    }

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void addQuestionsTest() {

        int examID = EXAM_ID;
        String questionsJSON = "{\"questions\": [{\"type\": \"SUBJECTIVE\", \"points\": 5, \"statement\": \"SUB1\", \"questionNumber\": 1}, {\"type\": \"MCQ\", \"points\": 4, \"options\": [\"OP1\", \"OP2\", \"OP3\", \"OP4\"], \"statement\": \"MCQ1\", \"questionNumber\": 2}]}";

        questionsDao.insertExamQuestions(examID, questionsJSON);
        handle = dbi.open();
        List<Map<String, Object>> list = handle.createQuery("select * from questions where exam_id=" + examID).list();
        handle.close();

        assertFalse(list.isEmpty());
        Map<String, Object> addedQuestion = list.get(0);
        assertEquals(examID, addedQuestion.get("exam_id"));
        assertEquals(questionsJSON, addedQuestion.get("questions"));
    }

    @Test
    public void addQuestionFailingExamIDForeignKeyTest() {

        Assertions.assertThrows(UnableToExecuteStatementException.class, () -> {
            int examID = 400;
            String questionsJSON = "{\"questions\": [{\"type\": \"SUBJECTIVE\", \"points\": 5, \"statement\": \"SUB1\", \"questionNumber\": 1}, {\"type\": \"MCQ\", \"points\": 4, \"options\": [\"OP1\", \"OP2\", \"OP3\", \"OP4\"], \"statement\": \"MCQ1\", \"questionNumber\": 2}]}";

            questionsDao.insertExamQuestions(examID, questionsJSON);
        });

    }

    @Test
    public void addQuestionNotInJSONFormatTest() {

        Assertions.assertThrows(UnableToExecuteStatementException.class, () -> {
            int examID = EXAM_ID;
            String questionsJSON = " random string ";
            questionsDao.insertExamQuestions(examID, questionsJSON);
        });

    }

    @Test
    void addQuestionsForSameExamIDTwice() {

        assertThrows(UnableToExecuteStatementException.class, () -> {
            int examID = EXAM_ID;
            String questionsJSON = "{\"questions\": [{\"type\": \"SUBJECTIVE\", \"points\": 5, \"statement\": \"SUB1\", \"questionNumber\": 1}, {\"type\": \"MCQ\", \"points\": 4, \"options\": [\"OP1\", \"OP2\", \"OP3\", \"OP4\"], \"statement\": \"MCQ1\", \"questionNumber\": 2}]}";
            String questionsJSON2 = "{\"questions\": [{\"type\": \"SUBJECTIVE\", \"points\": 5, \"statement\": \"SUB2\", \"questionNumber\": 1}, {\"type\": \"MCQ\", \"points\": 4, \"options\": [\"OP1\", \"OP2\", \"OP3\", \"OP4\"], \"statement\": \"MCQ2\", \"questionNumber\": 2}]}";
            questionsDao.insertExamQuestions(examID, questionsJSON);
            questionsDao.insertExamQuestions(examID, questionsJSON2);
        });

    }

    @Test
    public void getQuestionsTest() {
        int examID = EXAM_ID;
        String questionsJSON = "{\"questions\": [{\"type\": \"SUBJECTIVE\", \"points\": 5, \"statement\": \"SUB1\", \"questionNumber\": 1}, {\"type\": \"MCQ\", \"points\": 4, \"options\": [\"OP1\", \"OP2\", \"OP3\", \"OP4\"], \"statement\": \"MCQ1\", \"questionNumber\": 2}]}";
        List<String> options = new ArrayList<>();
        options.add("OP1");
        options.add("OP2");
        options.add("OP3");
        options.add("OP4");
        MultipleChoiceQuestion mcqQuestion = new MultipleChoiceQuestion("MCQ1", 4, 2, options);
        SubjectiveQuestion subjectiveQuestion = new SubjectiveQuestion("SUB1", 5, 1);

        handle = dbi.open();
        handle.insert("insert into questions(exam_id,questions) values ( " + examID + ", '" + questionsJSON + "' )");
        handle.close();
        Questions questions = questionsDao.getExamQuestions(examID);

        assertNotEquals(null, questions);
        assertEquals(2, questions.getQuestions().size());
        SubjectiveQuestion subjectiveQuestionReceived = (SubjectiveQuestion) questions.getQuestions().get(0);
        assertNotEquals(null, subjectiveQuestionReceived);
        MultipleChoiceQuestion mcqQuestionReceived = (MultipleChoiceQuestion) questions.getQuestions().get(1);
        assertNotEquals(null, mcqQuestion);
        assertEquals(subjectiveQuestion.getStatement(), subjectiveQuestionReceived.getStatement());
        assertEquals(subjectiveQuestion.getPoints(), subjectiveQuestionReceived.getPoints());
        assertEquals(subjectiveQuestion.getQuestionNumber(), subjectiveQuestionReceived.getQuestionNumber());
        assertEquals(subjectiveQuestion.getType(), subjectiveQuestionReceived.getType());
        assertEquals(mcqQuestion.getStatement(), mcqQuestionReceived.getStatement());
        assertEquals(mcqQuestion.getPoints(), mcqQuestionReceived.getPoints());
        assertEquals(mcqQuestion.getQuestionNumber(), mcqQuestionReceived.getQuestionNumber());
        assertEquals(mcqQuestion.getType(), mcqQuestionReceived.getType());
        assertEquals(mcqQuestion.getOptions(), mcqQuestionReceived.getOptions());
    }

    @Test
    public void getQuestionsForInvalidExamIDTest() {
        Questions questions = questionsDao.getExamQuestions(1000);
        assertNull(questions);
    }

    @AfterEach
    public void tearDown() {
        handle = dbi.open();
        handle.insert("delete from questions where exam_id=" + EXAM_ID);
        handle.close();
    }

}
