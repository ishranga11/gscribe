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
let googleUser;

function init() {
    gapi.load('auth2', function () {
        auth2 = gapi.auth2.init({
            'client_id': '361993398276-n4dboc83jnellr02pkg0v8rh2rvlnqn6.apps.googleusercontent.com',
            'cookiepolicy': 'single_host_origin',
            'scope': 'profile'
        });
        $('.logged-in-element').hide();
        auth2.isSignedIn.listen(signinChanged);
        auth2.currentUser.listen(userChanged);
    });
}

var signinChanged = function (val) {
    if (val === true) setForLogin();
};

var userChanged = function (user) {
    if (user.getId()) {
        googleUser = user;
    }
};

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
        }
    });
}

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
        }
    });

}

function fillExamModal(data) {
    $('#response-modal-body').empty();
    document.getElementById('modal-label').innerHTML = 'Exam';
    fillMetadata(data.exam.examMetadata);
    fillQuestions(data.exam.questions);
    $('#response-modal').modal({show: true});
}

function fillErrorModal(message) {
    document.getElementById('modal-label').innerHTML = "Error";
    document.getElementById('response-modal-body').innerHTML = JSON.stringify(message);
    $('#response-modal').modal({show: true});
}

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

function fillQuestions(questions) {
    questions.forEach(question => {
        if (question.type === "MCQ") fillMCQ(question);
        else if (question.type === "SUBJECTIVE") fillSubjective(question);
    });
}

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

function fillSubjective(question) {
    let adder = '<div class="card">\n' +
        '  <div class="card-body">\n' +
        '    <h5 class="card-title">Question ' + question.questionNumber + ' ( ' + question.points + ' points ) </h5>\n' +
        '    <p class="card-text"> ' + question.statement + ' </p>\n' +
        '  </div>\n' +
        '</div>';
    $('#response-modal-body').append(adder);
}


function setForLogin() {
    $('.logged-in-element').show();
    $('.logged-out-element').hide();
    let profile = googleUser.getBasicProfile();
    document.getElementById('loginCardTitle').innerText = "Hello " + profile.getName() + " ( " + profile.getEmail() + ")";
    fetchExams();
}

function login() {
    auth2.signIn().then(function () {
        googleUser = auth2.currentUser.get();
        setForLogin();
    });
}

function logout() {
    auth2.signOut().then(function () {
        location.reload();
    });
}

window.onload = init;
