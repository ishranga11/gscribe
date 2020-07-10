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

import com.google.googleinterns.gscribe.models.ExamMetadata;
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

public class ExamMetadataDaoTest {

    private static final String USER = "user";
    private static Handle handle;
    private static ExamMetadataDao examMetadataDao;

    @BeforeAll
    public static void init() throws IOException {
        DBI dbi = new DBIProvider().getDBI();
        handle = dbi.open();
        handle.insert("delete from user where id = '" + USER + "'");
        handle.insert("insert into user(id,access_token,refresh_token) values ( '" + USER + "','a_token','r_token')");
        examMetadataDao = dbi.onDemand(ExamMetadataDao.class);
    }

    @BeforeEach
    public void setUp() {
        handle.insert("delete from exam where created_by='" + USER + "'");
    }

    @Test
    public void insertExamTest() {

        String createdBy = USER;
        String spreadsheetID = "spreadsheet_id";
        String sheetName = "sheet_name";
        int duration = 100;
        ExamMetadata examMetadata = new ExamMetadata(spreadsheetID, sheetName, createdBy, duration);

        int examID = examMetadataDao.insertExamMetadata(examMetadata);
        List<Map<String, Object>> result = handle.createQuery("select * from exam where id= " + examID).list();

        assertFalse(result.isEmpty());
        Map<String, Object> row = result.get(0);
        assertEquals(examID, row.get("id"));
        assertEquals(createdBy, row.get("created_by"));
        assertEquals(spreadsheetID, row.get("spreadsheet_id"));
        assertEquals(duration, row.get("duration"));

    }

    @Test
    public void insertExamFailUserForeignKeyTest() {

        assertThrows(UnableToExecuteStatementException.class, () -> {
            String createdBy = "user2";
            String spreadsheetID = "spreadsheet_id";
            String sheetName = "sheet_name";
            int duration = 100;
            ExamMetadata examMetadata = new ExamMetadata(spreadsheetID, sheetName, createdBy, duration);

            examMetadataDao.insertExamMetadata(examMetadata);
        });

    }

    @Test
    public void insertExamNullUserTest() {

        assertThrows(UnableToExecuteStatementException.class, () -> {
            String createdBy = null;
            String spreadsheetID = "spreadsheet_id";
            String sheetName = "sheet_name";
            int duration = 100;
            ExamMetadata examMetadata = new ExamMetadata(spreadsheetID, sheetName, createdBy, duration);

            examMetadataDao.insertExamMetadata(examMetadata);
        });

    }

    @Test
    public void insertExamNullSpreadsheetIDTest() {

        assertThrows(UnableToExecuteStatementException.class, () -> {
            String createdBy = USER;
            String spreadsheetID = null;
            String sheetName = "sheet_name";
            int duration = 100;
            ExamMetadata examMetadata = new ExamMetadata(spreadsheetID, sheetName, createdBy, duration);

            examMetadataDao.insertExamMetadata(examMetadata);
        });

    }

    @Test
    public void getExamMetadataByUserDetailsTest() {

        String createdBy = USER;
        String spreadsheetID = "spreadsheet_id";
        int duration = 100;
        int examID = 100;

        handle.insert("insert into exam (id,created_by,spreadsheet_id,duration) values (" + examID + ",'" + createdBy + "','" + spreadsheetID + "', " + duration + " )");
        ExamMetadata examMetadata = examMetadataDao.getExamMetadataByUser(examID, createdBy);

        assertNotNull(examMetadata);
        assertEquals(spreadsheetID, examMetadata.getSpreadsheetID());
        assertEquals(duration, examMetadata.getDuration());
        assertEquals(examID, examMetadata.getId());

    }

    @Test
    public void getExamMetadataWithNotUsedUserDetailsTest() {

        String createdBy = USER;
        String spreadsheetID = "spreadsheet_id";
        int duration = 100;
        int examID = 100;

        handle.insert("insert into exam (id,created_by,spreadsheet_id,duration) values (" + examID + ",'" + createdBy + "','" + spreadsheetID + "', " + duration + " )");
        ExamMetadata examMetadata = examMetadataDao.getExamMetadataByUser(examID, "anotherUser");

        assertNull(examMetadata);

    }

    @Test
    public void getExamMetadataByExamIDTest() {

        String createdBy = USER;
        String spreadsheetID = "spreadsheet_id";
        int duration = 100;
        int examID = 100;

        handle.insert("insert into exam (id,created_by,spreadsheet_id,duration) values (" + examID + ",'" + createdBy + "','" + spreadsheetID + "', " + duration + " )");
        ExamMetadata examMetadata = examMetadataDao.getExamMetadataByExamId(examID);

        assertNotNull(examMetadata);
        assertEquals(spreadsheetID, examMetadata.getSpreadsheetID());
        assertEquals(duration, examMetadata.getDuration());
        assertEquals(examID, examMetadata.getId());

    }

    @Test
    public void getExamMetadataByNotUsedExamIDTest() {

        String createdBy = USER;
        String spreadsheetID = "spreadsheet_id";
        int duration = 100;
        int examID = 100;

        handle.insert("insert into exam (id,created_by,spreadsheet_id,duration) values (" + examID + ",'" + createdBy + "','" + spreadsheetID + "', " + duration + " )");
        ExamMetadata examMetadata = examMetadataDao.getExamMetadataByExamId(1);

        assertNull(examMetadata);

    }

    @Test
    public void getExamMetadataListByUserIDTest() {

        String createdBy = USER;

        handle.insert("insert into exam (created_by,spreadsheet_id,duration) values ('" + createdBy + "','sheet1', 120 )");
        handle.insert("insert into exam (created_by,spreadsheet_id,duration) values ('" + createdBy + "','sheet2', 130 )");
        handle.insert("insert into exam (created_by,spreadsheet_id,duration) values ('" + createdBy + "','sheet3', 140 )");
        List<ExamMetadata> examMetadataList = examMetadataDao.getExamMetadataListByUser(createdBy);

        assertEquals(3, examMetadataList.size());

        ExamMetadata metadata1, metadata2, metadata3;
        metadata1 = examMetadataList.get(0);
        metadata2 = examMetadataList.get(1);
        metadata3 = examMetadataList.get(2);

        assertEquals("sheet1", metadata1.getSpreadsheetID());
        assertEquals("sheet2", metadata2.getSpreadsheetID());
        assertEquals("sheet3", metadata3.getSpreadsheetID());
        assertEquals(120, metadata1.getDuration());
        assertEquals(130, metadata2.getDuration());
        assertEquals(140, metadata3.getDuration());

    }

    @Test
    public void getExamMetadataListByNotUsedUserIDTest() {

        String createdBy = USER;

        handle.insert("insert into exam (created_by,spreadsheet_id,duration) values ('" + createdBy + "','sheet1', 120 )");
        handle.insert("insert into exam (created_by,spreadsheet_id,duration) values ('" + createdBy + "','sheet2', 130 )");
        handle.insert("insert into exam (created_by,spreadsheet_id,duration) values ('" + createdBy + "','sheet3', 140 )");
        List<ExamMetadata> examMetadataList = examMetadataDao.getExamMetadataListByUser("different_user");

        assertEquals(0, examMetadataList.size());

    }

}
