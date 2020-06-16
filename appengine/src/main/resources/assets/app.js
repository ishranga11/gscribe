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
        'error': function (xhr, data) {
            fillErrorModal(data);
        }
    });

}

function fillExamModal(data) {
    let responseModalBody = document.getElementById("response-modal-body");
    $("#response-modal").modal({show: true});
}

function fillErrorModal(message) {
    $("#response-modal").modal({show: true});
}

function setForLogin() {
    $('.logged-in-element').show();
    $('.logged-out-element').hide();
    let profile = googleUser.getBasicProfile();
    document.getElementById('loginCardTitle').innerText = "Hello " + profile.getName() + " ( " + profile.getEmail() + ")";
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
