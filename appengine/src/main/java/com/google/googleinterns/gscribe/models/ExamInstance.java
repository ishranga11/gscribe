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

package com.google.googleinterns.gscribe.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.Objects;

public class ExamInstance {

    private int id;
    private int examID;
    private String userID;
    private int studentRollNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp endTime;
    private Answers answers;

    public ExamInstance() {
    }

    public ExamInstance(int examID, String userID, int studentRollNum) {
        this.examID = examID;
        this.studentRollNum = studentRollNum;
        this.userID = userID;
    }

    public ExamInstance(int id, int examID, String userID, int studentRollNum, Timestamp startTime, Timestamp endTime) {
        this.id = id;
        this.examID = examID;
        this.studentRollNum = studentRollNum;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userID = userID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamID() {
        return examID;
    }

    public void setExamID(int examID) {
        this.examID = examID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getStudentRollNum() {
        return studentRollNum;
    }

    public void setStudentRollNum(int studentRollNum) {
        this.studentRollNum = studentRollNum;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Answers getAnswers() {
        return answers;
    }

    public void setAnswers(Answers answers) {
        this.answers = answers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExamInstance)) return false;
        ExamInstance that = (ExamInstance) o;
        return id == that.id &&
                examID == that.examID &&
                studentRollNum == that.studentRollNum &&
                userID.equals(that.userID) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(answers, that.answers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, examID, userID, studentRollNum, startTime, endTime, answers);
    }
}
