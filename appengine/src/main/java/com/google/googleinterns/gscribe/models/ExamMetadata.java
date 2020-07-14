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

public class ExamMetadata {

    // TODO: add version for exams
    private String spreadsheetID;
    private String sheetName;
    private String userID;
    private int id;
    private int duration;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp createdOn;

    public ExamMetadata() {
    }

    public ExamMetadata(String spreadsheetID, String sheetName, String userID, int id, int duration, Timestamp createdOn) {
        this.spreadsheetID = spreadsheetID;
        this.sheetName = sheetName;
        this.userID = userID;
        this.id = id;
        this.duration = duration;
        this.createdOn = createdOn;
    }

    public ExamMetadata(String spreadsheetID, String sheetName, String userID, int duration) {
        this.spreadsheetID = spreadsheetID;
        this.sheetName = sheetName;
        this.userID = userID;
        this.duration = duration;
    }

    public ExamMetadata(String spreadsheetID, int id, int duration, Timestamp createdOn) {
        this.spreadsheetID = spreadsheetID;
        this.duration = duration;
        this.createdOn = createdOn;
        this.id = id;
    }

    public String getSpreadsheetID() {
        return spreadsheetID;
    }

    public void setSpreadsheetID(String spreadsheetID) {
        this.spreadsheetID = spreadsheetID;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExamMetadata)) return false;
        ExamMetadata metadata = (ExamMetadata) o;
        return getId() == metadata.getId() &&
                getDuration() == metadata.getDuration() &&
                getSpreadsheetID().equals(metadata.getSpreadsheetID()) &&
                Objects.equals(getSheetName(), metadata.getSheetName()) &&
                Objects.equals(getUserID(), metadata.getUserID()) &&
                Objects.equals(getCreatedOn(), metadata.getCreatedOn());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSpreadsheetID(), getSheetName(), getUserID(), getId(), getDuration(), getCreatedOn());
    }
}
