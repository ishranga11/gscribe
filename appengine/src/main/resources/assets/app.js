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

let auth2;

/**
 * Called on window load
 * Initializes the gapi library with client_id of the client to be able to use gapi oauth functionality
 * Initially hides all elements which should be visible if user is logged in
 * Adds listener for change in sign in status or user from logout state to login state
 */
function init() {
    gapi.load('auth2', function () {
        auth2 = gapi.auth2.init({
            'client_id': '361993398276-n4dboc83jnellr02pkg0v8rh2rvlnqn6.apps.googleusercontent.com',
            'cookiepolicy': 'single_host_origin',
            'scope': 'profile'
        });
        $('.logged-in-element').hide();
        auth2.isSignedIn.listen(signInChanged);
    });
}

/**
 * Called by gapi sign in status listener
 * Whenever user logout then the window refreshes, so this is basically used when user login
 * When user login then set page for login state by function setForLogin
 * @param val ( true if user is signed in else false )
 */
let signInChanged = function (val) {
    if (val === true) setForLogin();
};

/**
 * Called when the backend returns that the user is not authorized
 * This function asks the user to grant offline access to google sheets
 * When user grants offline access to google sheets an authorization code is generated which can be used to generate tokens
 * A post call is made to the backend with the authorization code and IDToken to authorize user
 * On success move to submitSheet function
 * On error display error in modal
 */
function sendCode() {
    let IDToken = gapi.auth2.getAuthInstance().currentUser.get().getAuthResponse().id_token;
    auth2.grantOfflineAccess({
        'scope': "https://www.googleapis.com/auth/spreadsheets"
    }).then((res) => {
        let code = res['code'];
        let request = {
            'authCode': code
        }
        jQuery.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authentication': IDToken
            },
            'type': 'POST',
            'url': "/api/authenticate",
            'data': JSON.stringify(request),
            'success': function () {
                submitSheet();
            },
            'error': function (data) {
                fillErrorModal(data);
            }
        });
    });
}

/**
 * Called when user wants to submit the question paper
 * First checks if the user is authorized for the service
 * i.e. the backend holds tokens for spreadsheet access token for this user
 * If user is authorized then move to submitSheet function
 * If user is not authorized then call sendCode function to send authorization code for user
 * Else display error in error modal
 */
function codeNeeded() {

    let IDToken = gapi.auth2.getAuthInstance().currentUser.get().getAuthResponse().id_token;
    jQuery.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authentication': IDToken
        },
        'type': 'GET',
        'url': "/api/authenticate",
        'success': function () {
            submitSheet();
        },
        'error': function (xhr, data) {
            if (xhr.status === 401) {
                sendCode();
            } else fillErrorModal(data);
        }
    });

}

/**
 * Called when user is authorized
 * spreadsheetId and sheet name are sent for sheet where the question paper lies
 * On success show exam object in the exam modal
 * On error display the error in error modal
 */
function submitSheet() {

    let IDToken = gapi.auth2.getAuthInstance().currentUser.get().getAuthResponse().id_token;
    let spreadsheetId = document.getElementById("spreadsheetId").value;
    let sheetName = document.getElementById("sheetName").value;
    let queryInfo = {
        'spreadsheetID': spreadsheetId,
        'sheetName': sheetName
    };
    jQuery.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authentication': IDToken
        },
        'type': 'POST',
        'url': "/api/exam",
        'data': JSON.stringify(queryInfo),
        'success': function (data) {
            fillExamModal(data);
        },
        'error': function (xhr) {
            let err = eval("(" + xhr.responseText + ")");
            fillErrorModal(err.message);
        }
    });

}

/**
 * Called to fill exam table on the page
 * This exam table acts as a compact way to view all exams created by user showing minimal info i.e. exam metadata
 * The table is filled with exam metadata of all exams created by user
 * @param data ( exam metadata list response object )
 */
function fillExamsTable(data) {

    $('#examsList').empty();
    let exams = data.examsList;
    for (var i = 0; i < exams.length; i++) {
        var examRow = '<tr>' +
            '<td><button class="btn btn-link" onclick= "getExam(' + exams[i].id + ')"> ' + exams[i].id + '</button></td>' +
            '<td>' + exams[i].spreadsheetID + '</td>' +
            '<td>' + exams[i].duration + '</td>' +
            '<td>' + exams[i].createdOn + '</td>' +
            '</tr>';
        $('#examsList').append(examRow);
    }
}

/**
 * Called when user clicks on exam id in exams metadata table
 * Here user wants to view the exam for corresponding exam id
 * A request is made to the backend to fetch corresponding exam object
 * On success fill exam modal with the exam object
 * On error display error in error modal
 * @param examID
 */
function getExam(examID) {
    let IDToken = gapi.auth2.getAuthInstance().currentUser.get().getAuthResponse().id_token;
    let url = "/api/exam/" + examID;
    jQuery.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authentication': IDToken
        },
        'type': 'GET',
        'url': url,
        'success': function (data) {
            fillExamModal(data);
        },
        'error': function (data) {
            fillErrorModal(data);
        }
    });
}

/**
 * Called to load exam metadata table with exam metadata of all exams created by user
 * This function is called when page loads
 * A request is made to the backend for list of exam metadata
 * On success fill the exam metadata table
 * On error display error in error modal
 */
function fetchExams() {

    let IDToken = gapi.auth2.getAuthInstance().currentUser.get().getAuthResponse().id_token;
    jQuery.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authentication': IDToken
        },
        'type': 'GET',
        'url': "/api/exam/all",
        'success': function (data) {
            fillExamsTable(data);
        },
        'error': function (data) {
            fillErrorModal(data);
        }
    });

}

/**
 * Called to fill exam modal with the exam object
 * At first the exam metadata is filled
 * Then all the questions are filled into the modal
 * And then finally display the modal
 * @param data ( exam object )
 */
function fillExamModal(data) {
    $('#response-modal-body').empty();
    document.getElementById('modal-label').innerHTML = 'Exam';
    fillMetadata(data.exam.examMetadata);
    fillQuestions(data.exam.questionsList.questions);
    $('#response-modal').modal({show: true});
}

/**
 * Called to fill error modal with error message
 * Then show the error modal
 * @param message
 */
function fillErrorModal(message) {
    document.getElementById('modal-label').innerHTML = "Error";
    document.getElementById('response-modal-body').innerHTML = JSON.stringify(message);
    $('#response-modal').modal({show: true});
}

/**
 * Called to fill exam metadata in the exam modal
 * A card is made listing all the exam metadata information
 * And finally append the card in the exam modal
 * @param data ( exam metadata )
 */
function fillMetadata(data) {
    let adder = '<div class="card">\n' +
        '  <div class="card-header"> Exam Metadata </div>\n' +
        '  <ul class="list-group list-group-flush">\n' +
        '    <li class="list-group-item">Exam ID: ' + data.id + ' </li>\n' +
        '    <li class="list-group-item">Exam Duration: ' + data.duration + ' </li>\n' +
        '    <li class="list-group-item">Spreadsheet ID: ' + data.spreadsheetID + '</li>\n' +
        '    <li class="list-group-item">Created On: ' + data.createdOn + '</li>\n' +
        '  </ul>\n' +
        '</div>';
    $('#response-modal-body').append(adder);
}

/**
 * Called to fill all questions in the exam modal
 * Filling question is done by different functions based on question type
 * @param questions ( list of all questions JSON )
 */
function fillQuestions(questions) {
    questions.forEach(question => {
        if (question.type === "MCQ") fillMCQ(question);
        else if (question.type === "SUBJECTIVE") fillSubjective(question);
    });
}

/**
 * Called to fill MCQ question into the exam modal
 * A card is made for the question listing the question fields
 * Finally the question is appended to the exam modal
 * @param question
 */
function fillMCQ(question) {
    let adder = '<div class="card">\n' +
        '  <div class="card-body">\n' +
        '    <h5 class="card-title">Question ' + question.questionNumber + ' ( ' + question.points + ' points ) </h5>\n' +
        '    <p class="card-text"> ' + question.statement + ' </p>\n' +
        '  </div>\n' +
        '  <ul class="list-group list-group-flush">\n' +
        '    <li class="list-group-item"> ' + question.options[0] + ' </li>\n' +
        '    <li class="list-group-item"> ' + question.options[1] + ' </li>\n' +
        '    <li class="list-group-item"> ' + question.options[2] + ' </li>\n' +
        '    <li class="list-group-item"> ' + question.options[3] + ' </li>\n' +
        '  </ul>' +
        '</div>';
    $('#response-modal-body').append(adder);
}

/**
 * Called to fill SUBJECTIVE question into the exam modal
 * A card is made for the question listing the question fields
 * Finally the question is appended to the exam modal
 * @param question
 */
function fillSubjective(question) {
    let adder = '<div class="card">\n' +
        '  <div class="card-body">\n' +
        '    <h5 class="card-title">Question ' + question.questionNumber + ' ( ' + question.points + ' points ) </h5>\n' +
        '    <p class="card-text"> ' + question.statement + ' </p>\n' +
        '  </div>\n' +
        '</div>';
    $('#response-modal-body').append(adder);
}

/**
 * Called to set the page for user login
 * Hide all elements meant for logout state and show all elements for login state
 * Display a welcome message mentioning user name and email id
 * Finally fill the exam metadata table using fetchExams function
 */
function setForLogin() {
    $('.logged-in-element').show();
    $('.logged-out-element').hide();
    let profile = gapi.auth2.getAuthInstance().currentUser.get().getBasicProfile();
    document.getElementById('loginCardTitle').innerText = "Hello " + profile.getName() + " ( " + profile.getEmail() + ")";
    fetchExams();
}

/**
 * Called when user clicks login button and login is done
 * Calls setForLogin function to set the page for login state
 */
function login() {
    auth2.signIn().then(function () {
        setForLogin();
    });
}

/**
 * This function is called when user clicks logout button
 * Refresh the page to set page to logout state
 */
function logout() {
    auth2.signOut().then(function () {
        location.reload();
    });
}

window.onload = init;
