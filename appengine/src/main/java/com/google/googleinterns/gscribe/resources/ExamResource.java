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

import javax.ws.rs.*;

@Path("/exam")
@Produces("application/json")
public class ExamResource {

    @POST
    /*
    Input - IDToken, spreadsheetId, sheetName
    Function - validate the exam from spreadsheet and then post exam in database
    Output - if successfully validated then return examId
     */
    public String postExam() {
        return "/exam";
    }

    @GET
    @Path("/all")
    /*
    Input - IDToken
    Function - get all exam ids for the exams posted by the user ( userId from IDToken )
    Output - return exam ids for all exam posted by the user
     */
    public String getAllExamsId() {
        return "/exam/all";
    }

    @GET
    @Path("/{id}")
    /*
    Input - IDToken
    Function - validate if user from IDToken posted the exam and then return exam with examID id
    Output - exam represented by examID id
     */
    public String getExam(@PathParam("id") String id) {
        return "/exam/{id}";
    }

}
