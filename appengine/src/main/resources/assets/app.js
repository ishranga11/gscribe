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
    auth2.grantOfflineAccess({
        'scope': "https://www.googleapis.com/auth/spreadsheets"
    }).then((res) => {
        let code = res['code'];
        jQuery.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            'type': 'POST',
            'url': "/api/token",
            'data': code,
            'dataType': 'text',
            'success': function (data) {
                if (data === "Done") submitSheet();
            }
        });
    });
}

function codeNeeded() {

    let IDToken = gapi.auth2.getAuthInstance().currentUser.get().getAuthResponse().id_token;
    jQuery.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        'type': 'POST',
        'url': "/api/token/present",
        'data': IDToken,
        'dataType': 'text',
        'success': function (data) {
            if (data === "Needed") {
                sendCode();
            } else submitSheet();
        }
    });

}

function submitSheet() {

    let IDToken = gapi.auth2.getAuthInstance().currentUser.get().getAuthResponse().id_token;
    let spreadsheetId = document.getElementById("spreadsheetId").value;
    let sheetId = document.getElementById("sheetId").value;
    let queryInfo = {
        'IDToken': IDToken,
        'spreadsheetId': spreadsheetId,
        'sheetId': sheetId
    };

    $("#response-modal").modal({show: true});

    jQuery.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        'type': 'POST',
        'url': "/api/exam",
        'data': JSON.stringify(queryInfo),
        'dataType': 'json',
        'success': function (data) {
            let responseModalBody = document.getElementById("response-modal-body");
            responseModalBody.innerHTML = data;
        }
    });

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
