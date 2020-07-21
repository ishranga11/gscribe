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

'use strict';

const functions = require('firebase-functions');
const {dialogflow} = require('actions-on-google');
const axios = require('axios');
const app = dialogflow({
    clientId: 'ACTIONS_ON_GOOGLE_CLIENT_ID_PLACEHOLDER',
});
const backendServiceBaseUrl = 'BACKEND_SERVICE_HOSTING_URL_PLACEHOLDER';

/**
 * Called to create user data object when starting exam intent is triggered
 * When the user initiates the call for starting exam then take the user credentials from the context
 * return the user credentials object with roll_number and exam_instance_id
 * This object will be used at the end to submit the exam
 * @param conv
 * @returns {{rollNumber, examId}}
 */
function getUserData ( conv ){
    try {
        const roll_number = conv.contexts.get('roll_number').parameters['roll_number'];
        const exam_id = conv.contexts.get('exam_id').parameters['exam_id'];
        return {
            roll_number: roll_number,
            exam_id: exam_id
        };
    } catch (e) {
        throw new Error("Failed to get user credentials");
    }
}

/**
 * Called to fetch exam object from database
 * Sends the request to the backend to get exam object corresponding to the exam id
 * Sends roll number to create a new exam instance in the database
 * Authentication token header takes the IDToken which is used for authentication
 * On success return exam object else throw an error
 * @param user ( contains user data like roll_number, exam_id )
 * @param token ( IDToken of the logged in user account )
 * @returns exam object
 */
function getExamObject ( user, token ){
    let url = backendServiceBaseUrl + '/api/exam/start';
    return axios.post( url, {
        studentRollNum : user.roll_number,
        examID: user.exam_id
    }, {
        headers: { Authentication: token }
    }).then( requestResult => {
        return requestResult.data;
    }).catch( error =>{
        throw new Error( "Unable to get exam ");
    });
}

/**
 * Called to create answers context object when starting exam intent is triggered
 * On start of exam create the answers list to be stored in context
 * Initially no answer is attempted so mark them not attempted
 * Size of the list is set as the number of questions
 * @param numberOfQuestions ( number of questions in the exam )
 * @returns {{answers_list: []}}
 */
function  getAnswersParameter ( numberOfQuestions ) {
    if ( numberOfQuestions<=0 ) throw new Error("Failed to fetch exam");
    let answers = [];
    for ( let i=0; i<numberOfQuestions; i++ ) answers.push( "Not attempted yet");
    return {
        answers_list: answers
    };
}

/**
 * Called to load a new question into the current question context
 * Using the question number and exam object from the context, extracts the question
 * Creates the current question object to be stored into the context
 * Handles different type of questions ( here MCQ and SUBJECTIVE )
 * @param questionNum ( question number of question to be loaded )
 * @param examObject ( exam object )
 * @returns {{question_statement: (string), options: string, question_number: *, points:  number}}
 */
function getCurrentQuestionParameter ( questionNum, examObject ){
    let question = examObject.exam.questions.questionsList[questionNum-1];
    let questionStatementString =  question.statement;
    let questionOptionsString;
    if ( question.type==="MCQ" ){
        questionOptionsString = " Option A: " + question.options[0] + " ,Option B: " + question.options[1] + " ,Option C: " + question.options[2] + " ,Option D: " + question.options[3];
    } else if ( question.type==="SUBJECTIVE" ){
        questionOptionsString = "This is a subjective type question. So no options are available for this question";
    }
    return {
        points: question.points,
        question_statement: questionStatementString,
        options: questionOptionsString,
        question_number: questionNum
    } ;
}

/**
 * Called when a new question is to be loaded into the current question context then accordingly have to set current answer
 * Using the question number of current question and answers list from context load the current answer
 * @param questionNum ( question number of current question )
 * @param answerObject
 * @returns {{answer: *}}
 */
function getCurrentAnswerParameter ( questionNum, answerObject ){
    return {
        answer:answerObject[questionNum-1]
    };
}

/**
 * Called when a new question is to be loaded into the context and then the question needs to be converted into a statement
 * This function returns a conversational message to be sent to the agent.
 * Here it normally lists all the question parameters to be told to the user
 * @param questionObject
 * @returns {string}
 */
function returnQuestionString ( questionObject ){
    return ( "Question number " + questionObject.question_number + " for "
        + questionObject.points + " points. " + questionObject.question_statement + ". " + questionObject.options );
}

/**
 * Called to create user context object when starting exam intent is triggered
 * Takes all the user context parameters as arguments of function and forms a new user context object
 * @param user ( contains user roll number and exam id )
 * @param exam_instance_id
 * @param end_time ( a timestamp for end time of exam )
 * @returns {{end_time: *, roll_number: *, exam_instance_id: *, exam_id: *}}
 */
function getUserContextParameter ( user, exam_instance_id, end_time ){

    return {
        roll_number: user.roll_number,
        exam_id: user.exam_id,
        exam_instance_id: exam_instance_id,
        end_time: end_time
    }

}

/**
 * Handles the exam.start.submit_request intent
 * contexts set -> user ( containing user data to be used when submitting exam )
 *                 exam ( exam object )
 *                 answers ( answers object )
 *                 current_question ( statement, options, points, question number )
 *                 current_answer ( answer )
 * contexts deleted to avoid confusion and avoiding editing by user:
 *                 roll_number, exam_instance_id, exam_start_request
 * If error is encountered then send appropriate message in response
 */
app.intent( 'exam.start.submit_request', async conv => {

    let userData;
    let examObject;
    try {
        userData = await getUserData(conv);
        examObject = await getExamObject(userData, conv.user.profile.token);
    } catch (e) {
        return conv.ask(e.message);
    }
    let currentTime = Date.now();
    let endTime = new Date( currentTime + examObject.exam.examMetadata.duration*60000);
    const userParameters = await getUserContextParameter ( userData, examObject.examInstanceID, endTime);
    const answersObject = await getAnswersParameter(examObject.exam.questions.length);
    const currentAnswer = await getCurrentAnswerParameter(1,answersObject);
    const currentQuestion = await getCurrentQuestionParameter(1,examObject);

    conv.contexts.set('exam', 1000, examObject);
    conv.contexts.set('user', 1000, userParameters);
    conv.contexts.set('answers', 1000, answersObject);
    conv.contexts.set('current_answer', 1000, currentAnswer);
    conv.contexts.set('current_question', 1000, currentQuestion);
    conv.contexts.delete('roll_number');
    conv.contexts.delete('exam_id');
    conv.contexts.delete('exam_start_request');
    conv.ask( returnQuestionString(currentQuestion) );

});

/**
 * Handles question_previous intent, called when user asks for previous question
 * It extracts examObject, answersObject and question number from the context
 * If the question is already question 1 then there is no previous question to it
 * Using the details in context load the new question and corresponding answer stored
 * Response sent is the new question statement
 */
app.intent( 'question.previous', async conv => {
    const questionNum = conv.contexts.get('current_question').parameters.question_number;
    const examObject = conv.contexts.get('exam').parameters;
    const answersObject = conv.contexts.get('answers').parameters.answers_list;
    if ( questionNum === 1 ){
        conv.ask('This is question Number 1. No question previous to this one.');
    } else {
        const newQuestion = getCurrentQuestionParameter(questionNum - 1, examObject);
        const newAnswer = getCurrentAnswerParameter(questionNum - 1, answersObject);
        conv.contexts.set('current_question', 1000, newQuestion);
        conv.contexts.set('current_answer', 1000, newAnswer);
        conv.ask( returnQuestionString(newQuestion) );
    }
});

/**
 * Handles question_next intent, called when user asks for next question
 * It extracts examObject, answersObject and question number from the context
 * If the question is already last question then there is no next question to it
 * Using the details in context load the new question and corresponding answer stored
 * Response sent is the new question statement
 */
app.intent( 'question.next', async conv => {
    const questionNum = conv.contexts.get('current_question').parameters.question_number;
    const examObject = conv.contexts.get('exam').parameters;
    const answersObject = conv.contexts.get('answers').parameters.answers_list;
    if ( questionNum === examObject.exam.questions.questionsList.length ){
        conv.ask('This is the last question. No next question available.');
    } else {
        const newQuestion = getCurrentQuestionParameter(questionNum + 1, examObject);
        const newAnswer = getCurrentAnswerParameter(questionNum + 1, answersObject);
        conv.contexts.set('current_question', 1000, newQuestion);
        conv.contexts.set('current_answer', 1000, newAnswer);
        conv.ask( returnQuestionString(newQuestion) );
    }
});

/**
 * Handles question_jump intent, called when user asks to jump or move to any question
 * It extracts examObject, answersObject and question number from the context
 * It takes the new question requested from the query
 * If the question is not in a valid range of ( 1 - number of questions in exam ) then throw error response
 * Using the details in context load the new question and corresponding answer stored
 * Response sent is the new question
 */
app.intent( 'question.jump', async conv => {
    const newQuestionNumber = parseInt( conv.parameters.question_number , 10 );
    const examObject = conv.contexts.get('exam').parameters;
    const answersObject = conv.contexts.get('answers').parameters.answers_list;
    if ( newQuestionNumber<1 || newQuestionNumber > examObject.exam.questions.questionsList.length ){
        conv.ask('Question should be between 1 and ' + examObject.exam.questions.questionsList.length );
    } else {
        const newQuestion = getCurrentQuestionParameter(newQuestionNumber, examObject);
        const newAnswer = getCurrentAnswerParameter(newQuestionNumber, answersObject);
        conv.contexts.set('current_question', 1000, newQuestion);
        conv.contexts.set('current_answer', 1000, newAnswer);
        conv.ask( returnQuestionString(newQuestion) );
    }
});

/**
 * Handles answer intent, called when user wants to submit answer
 * Extracts the question number and answers list from the context
 * If the question is of type MCQ and the response is not just ( A,B,C or D ) then give hint to user to record answer as A,B,C or D only
 * Updates the answer list and current answer with the query received
 * Returns a response saying that the answer is recorded
 */
app.intent( 'answer', async conv => {
    const questionNum = conv.contexts.get('current_question').parameters.question_number;
    const exam = conv.contexts.get('exam').parameters.exam;
    let answersObject = conv.contexts.get('answers').parameters.answers_list;
    const answer = conv.query;
    if (  exam.questions.questionsList[questionNum-1].type === "MCQ" ){
        answer.toUpperCase();
        if ( answer!=="A" && answer!=="B" && answer !=="C" && answer!=="D" ) return conv.ask("Did you mean A, B, C or D");
    }
    answersObject[questionNum-1] = answer;
    conv.contexts.set( 'current_answer', 1000, { answer:answer} );
    conv.contexts.set( 'answers', 1000, { answers_list: answersObject } );
    conv.ask( 'Answer recorded: ' + answer );
});

/**
 * Handles exam.submit intent, called when user wants to submit the exam
 * It extracts responses from context along with user details to submit exam
 * Finally close the conversation by sending a successful response to user
 * Or in case of any error send an error message to the user
 */
app.intent( 'exam.submit', async conv => {
    const answersObject = conv.contexts.get('answers').parameters.answers_list;
    const user =  conv.contexts.get('user').parameters;
    let answersList = [];
    for ( let i=0; i<answersObject.length; i++ ){
        if ( answersObject[i]==="Not attempted yet" ) answersObject[i] = "";
        answersList.push({
            answer: answersObject[i],
            questionNum: i+1
        })
    }
    let answers = {
        answersList: answersList
    }
    const request = {
        id: user.exam_instance_id,
        examID: user.exam_id,
        studentRollNum: user.roll_number,
        answers: answers
    }
    let url = backendServiceBaseUrl + '/api/exam/submit';
    return axios.post( url, {
        examInstance: request
    }, {
        headers: { Authentication: conv.user.profile.token }
    }).then( ()=> {
        return conv.close("Exam Submitted successfully");
    }).catch( error =>{
        return conv.ask( error.message );
    });


});

/**
 * Handles exam.timeleft intent, when user asks to know how much time is left
 * Extracts endTime from the context and finds difference with respect to current time
 * Convert the difference from milliseconds to minutes
 * Return the response to the user
 */
app.intent( 'exam.timeleft', async conv => {
    const currentTime = Date.now();
    const endTime = new Date( conv.contexts.get('user').parameters.end_time );
    let difference = endTime-currentTime;
    difference = Math.floor(difference/60000 );
    conv.ask( difference + " minutes ");
})

exports.dialogflowFirebaseFulfillment = functions.https.onRequest(app);
