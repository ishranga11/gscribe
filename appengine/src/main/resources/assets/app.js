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