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

package com.google.googleinterns.gscribe.resources;

import com.google.googleinterns.gscribe.resources.io.request.ExamRequest;
import com.google.googleinterns.gscribe.resources.io.response.ExamResponse;
import com.google.googleinterns.gscribe.resources.io.response.ExamsListResponse;

import javax.ws.rs.*;

@Path("/exam")
@Produces("application/json")
public class ExamResource {

    /**
     * Get corresponding userID from the IDToken using tokenVerifier
     * Get tokens for the user from the database
     * Use the tokens to read exam from the spreadsheet
     * Validate exam
     * Convert exam from List<List<Object>> to Exam object
     * post examMetadata in database to get examID
     * post exam into the database
     *
     * @param IDToken ( from header )
     * @param request ( must contain spreadsheetID, sheetName )
     * @return Exam object
     */
    @POST
    public ExamResponse postExam(@HeaderParam("authorization-code") String IDToken, ExamRequest request) {
        return null;
    }

    /**
     * Get corresponding userID from the IDToken using tokenVerifier
     * using userID get all exams metadata from database for current user
     *
     * @param IDToken ( from header )
     * @return List of exam metadata for current user
     */
    @GET
    @Path("/all")
    public ExamsListResponse getAllExamsId(@HeaderParam("authorization-code") String IDToken) {
        return null;
    }

    /**
     * Get corresponding userID from the IDToken using tokenVerifier
     * Check if exam with given examID was given by current user
     * Fetch exam metadata for given examID
     * Fetch exam questions for given examID
     *
     * @param IDToken ( from header )
     * @param id      ( examID for some exam )
     * @return exam object for given examID
     */
    @GET
    @Path("/{id}")
    public ExamResponse getExam(@HeaderParam("authorization-code") String IDToken, @PathParam("id") String id) {
        return null;
    }

}
