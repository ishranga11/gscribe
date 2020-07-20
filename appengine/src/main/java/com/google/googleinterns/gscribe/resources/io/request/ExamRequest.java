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

package com.google.googleinterns.gscribe.resources.io.request;

import java.util.Objects;

public class ExamRequest {

    private String spreadsheetID;
    private String sheetName;

    public ExamRequest() {
    }

    public ExamRequest(String spreadsheetID, String sheetName) {
        this.spreadsheetID = spreadsheetID;
        this.sheetName = sheetName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExamRequest)) return false;
        ExamRequest that = (ExamRequest) o;
        return Objects.equals(getSpreadsheetID(), that.getSpreadsheetID()) &&
                Objects.equals(getSheetName(), that.getSheetName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSpreadsheetID(), getSheetName());
    }
}
