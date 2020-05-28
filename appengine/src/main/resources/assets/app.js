// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
//     You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
//     Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//     See the License for the specific language governing permissions and
// limitations under the License.

const firebaseConfig = {
    apiKey: "AIzaSyAuxD5s2daPcMJPgiFrTNNyH9YKz9vcohA",
    authDomain: "scribe-for-blind.firebaseapp.com",
    databaseURL: "https://scribe-for-blind.firebaseio.com",
    projectId: "scribe-for-blind",
    storageBucket: "scribe-for-blind.appspot.com",
    messagingSenderId: "693599161458",
    appId: "1:693599161458:web:5ea82cb14e1dcbe1bac6eb"
};
firebase.initializeApp(firebaseConfig);

let displayName;
let email;
let accessToken;
let refreshToken;

function login(){
    function newLogin(user){
        let notLoginCard = document.getElementById("notLoginCard");
        let loginCard = document.getElementById("loginCard");
        let submitSheetForm = document.getElementById("submitSheetForm");
        let loginCardTitle = document.getElementById("loginCardTitle");
        let logoutButton = document.getElementById("logout");
        let loginButton = document.getElementById("login");
        if ( user ){
            notLoginCard.style.display = "none";
            submitSheetForm.style.display = "visible";
            loginCard.style.display = "visible";
            loginButton.style.display = "none";
            logoutButton.style.display = "visible";

            displayName =  user['displayName'];
            email = user['email'];
            refreshToken = user['refreshToken'];
            loginCardTitle.innerHTML = "Hello: " + displayName + " ( " + email + " )";
            user.getIdToken().then( function (idToken){
                accessToken = idToken;
            })

        } else {
            notLoginCard.style.display = "visible";
            loginCard.style.display = "none";
            submitSheetForm.style.display = "none";
            loginButton.style.display = "visible";
            logoutButton.style.display = "none";

            let provider = new firebase.auth.GoogleAuthProvider();
            provider.addScope('https://www.googleapis.com/auth/spreadsheets');
            provider.addScope('https://www.googleapis.com/auth/drive.readonly');
            firebase.auth().signInWithRedirect(provider).then(function(result) {
                let token = result.credential.accessToken;
                let user = result.user;
            }).catch(function(error) {
                let errorCode = error.code;
                let errorMessage = error.message;
                let email = error.email;
                let credential = error.credential;
            });
        }
    }
    firebase.auth().onAuthStateChanged(newLogin);
}

function submitSheet() {

    let spreadsheetId = document.getElementById("spreadsheetId").value;
    let sheetId = document.getElementById("sheetId").value;
    let queryInfo = {
        'name': displayName,
        'email': email,
        'accessToken': accessToken,
        'refreshToken': refreshToken,
        'spreadsheetId': spreadsheetId,
        'sheetId': sheetId
    };
    let finalQuery = JSON.stringify(queryInfo);

}

window.onload = login;

function logout(){

    firebase.auth().signOut().then(function() {
    }).catch(function(error) {
    });

}