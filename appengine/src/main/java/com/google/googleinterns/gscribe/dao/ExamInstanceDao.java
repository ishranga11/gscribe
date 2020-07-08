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

import com.google.googleinterns.gscribe.models.ExamInstance;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ExamInstanceDao {

    /**
     * Creation of exam instance using ( exam id, user id, student roll number )
     * User id represents the unique user id of the gmail account used by the user to give exam
     * Sets the start time to current timestamp automatically and creates a new exam instance id
     *
     * @param examInstance ( exam instance object )
     * @return exam instance id
     */
    @SqlUpdate("INSERT into exam_instance(exam_id,user_id,student_roll_num) values (:examID,:userID,:studentRollNum) ")
    @GetGeneratedKeys
    int insertExamInstance(@BindBean ExamInstance examInstance);

    /**
     * Queries exam instance for exam instance identified by exam instance id examInstanceID
     *
     * @param examInstanceID ( to identify particular exam instance )
     * @return exam instance object
     */
    @Mapper(ExamInstanceMapper.class)
    @SqlQuery("SELECT * from exam_instance where id = :exam_instance_id")
    ExamInstance getExamInstanceByExamInstanceID(@Bind("exam_instance_id") int examInstanceID);

    /**
     * Queries exam instance using exam id and student roll number
     * Needed to check if a student identified by roll number rollNumber already attempted the exam represented by examID
     *
     * @param examID     ( to identify particular exam )
     * @param rollNumber ( roll number of student )
     * @return exam instance object
     */
    @Mapper(ExamInstanceMapper.class)
    @SqlQuery("SELECT * from exam_instance where exam_id = :exam_id and student_roll_num = :student_roll_num")
    ExamInstance getExamInstanceByUserDetails(@Bind("exam_id") int examID, @Bind("student_roll_num") int rollNumber);

    /**
     * Updates end time of the exam instance identified by exam instance id examInstanceID
     *
     * @param examInstanceID ( to identify particular exam instance )
     */
    @SqlUpdate("UPDATE exam_instance set end_time=CURRENT_TIMESTAMP where id=:exam_instance_id")
    void updateExamInstanceEndTime(@Bind("exam_instance_id") int examInstanceID);

    /**
     * A mapper class to map exam instance responses to exam instance object
     */
    class ExamInstanceMapper implements ResultSetMapper<ExamInstance> {
        @Override
        public ExamInstance map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new ExamInstance(
                    resultSet.getInt("id"),
                    resultSet.getInt("exam_id"),
                    resultSet.getString("user_id"),
                    resultSet.getInt("student_roll_num"),
                    resultSet.getTimestamp("start_time"),
                    resultSet.getTimestamp("end_time")
            );
        }
    }

}
