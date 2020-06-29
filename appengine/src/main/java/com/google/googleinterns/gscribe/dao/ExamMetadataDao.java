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
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ExamMetadataDao {

    @Mapper(ExamMetadataDao.ExamMetadataMapper.class)
    @SqlQuery("SELECT * from exam where id = :id and created_by=:userId")
    ExamMetadata getExamMetadataById(@Bind("id") String id, @Bind("userId") String userId);

    @Mapper(ExamMetadataDao.ExamMetadataMapper.class)
    @SqlQuery("SELECT * from exam where created_by = :userID")
    List<ExamMetadata> getExamMetadataByUser(@Bind("userID") String userID);

    @SqlUpdate("INSERT INTO exam( created_by, spreadsheet_id, duration ) VALUES ( :userID, :spreadsheetID, :duration )")
    @GetGeneratedKeys
    int insertExamMetadata(@BindBean ExamMetadata examMetadata);

    class ExamMetadataMapper implements ResultSetMapper<ExamMetadata> {
        @Override
        public ExamMetadata map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new ExamMetadata(
                    resultSet.getString("spreadsheet_id"),
                    resultSet.getInt("duration"),
                    resultSet.getInt("id"),
                    resultSet.getTimestamp("created_on")
            );
        }
    }

}
