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
import com.google.googleinterns.gscribe.models.ExamInstance;
import com.google.googleinterns.gscribe.provider.DBIProvider;
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

public class ExamInstanceDaoTest {

    private static final int EXAM_ID = 100;
    private static Handle handle;
    private static ExamInstanceDao examInstanceDao;

    @BeforeAll
    public static void init() throws IOException {
        DBI dbi = new DBIProvider().getDBI();
        handle = dbi.open();
        handle.insert("delete from exam where id = " + EXAM_ID);
        handle.insert("delete from user where id = 'user'");
        handle.insert("insert into user(id,access_token,refresh_token) values ( 'user','a_token','r_token')");
        handle.insert("insert into exam(id,created_by,spreadsheet_id,duration) values ( " + EXAM_ID + " ,'user','spreadsheet_id',100)");
        dbi.registerMapper(new QuestionsDao.QuestionsMapper(new ObjectMapper()));
        examInstanceDao = dbi.onDemand(ExamInstanceDao.class);
    }

    @BeforeEach
    public void setUp() {
        handle.insert("delete from exam_instance where exam_id=" + EXAM_ID);
    }

    @Test
    public void insertExamInstanceTest() {

        int examID = EXAM_ID;
        String userID = "user_id";
        int studentRollNumber = 100;
        ExamInstance examInstance = new ExamInstance(examID, userID, studentRollNumber);

        int examInstanceID = examInstanceDao.insertExamInstance(examInstance);
        List<Map<String, Object>> result = handle.createQuery("select * from exam_instance where id = " + examInstanceID).list();

        assertFalse(result.isEmpty());
        Map<String, Object> row = result.get(0);
        assertNotNull(row);
        assertEquals(examID, row.get("exam_id"));
        assertEquals(examInstanceID, row.get("id"));
        assertEquals(userID, row.get("user_id"));
        assertEquals(studentRollNumber, row.get("student_roll_num"));

    }

    @Test
    public void insertExamInstanceNullUserIDTest() {

        assertThrows(UnableToExecuteStatementException.class, () -> {

            int examID = EXAM_ID;
            String userID = null;
            int studentRollNumber = 100;

            ExamInstance examInstance = new ExamInstance(examID, userID, studentRollNumber);

            examInstanceDao.insertExamInstance(examInstance);

        });

    }

    @Test
    public void getExamInstanceByExamInstanceIDTest() {

        int examID = EXAM_ID;
        handle.insert("insert into exam_instance(id,exam_id,user_id,student_roll_num) values (1," + examID + ",'user_id',20)");

        ExamInstance examInstance = examInstanceDao.getExamInstanceByExamInstanceID(1);

        assertNotNull(examInstance);
        assertEquals(1, examInstance.getId());
        assertEquals(examID, examInstance.getExamID());
        assertEquals("user_id", examInstance.getUserID());
        assertEquals(20, examInstance.getStudentRollNum());

    }

    @Test
    public void getExamInstanceWithNotUsedExamInstanceIDTest() {
        int examID = EXAM_ID;
        handle.insert("insert into exam_instance(id,exam_id,user_id,student_roll_num) values (1," + examID + ",'user_id',20)");

        ExamInstance examInstance = examInstanceDao.getExamInstanceByExamInstanceID(10);

        assertNull(examInstance);
    }

    @Test
    public void getExamInstanceByUserDetailsTest() {

        int examID = EXAM_ID;
        handle.insert("insert into exam_instance(id,exam_id,user_id,student_roll_num) values (1," + examID + ",'user_id',20)");

        ExamInstance examInstance = examInstanceDao.getExamInstanceByUserDetails(examID, 20);

        assertNotNull(examInstance);
        assertEquals(1, examInstance.getId());
        assertEquals(examID, examInstance.getExamID());
        assertEquals("user_id", examInstance.getUserID());
        assertEquals(20, examInstance.getStudentRollNum());

    }

    @Test
    public void getExamInstanceWithNotUsedUserDetailsTest() {
        int examID = EXAM_ID;
        handle.insert("insert into exam_instance(id,exam_id,user_id,student_roll_num) values (1," + examID + ",'user_id',20)");

        ExamInstance examInstance = examInstanceDao.getExamInstanceByUserDetails(examID, 21);

        assertNull(examInstance);
    }

    @Test
    public void updateEndTimeTest() {

        int examID = EXAM_ID;

        handle.insert("insert into exam_instance(id,exam_id,user_id,student_roll_num) values (1," + examID + ",'user_id',20)");
        List<Map<String, Object>> result = handle.createQuery("select * from exam_instance where id = 1").list();

        assertNull(result.get(0).get("end_time"));

        examInstanceDao.updateExamInstanceEndTime(1);
        result = handle.createQuery("select * from exam_instance where id = 1").list();

        assertNotNull(result.get(0).get("end_time"));
    }

}
