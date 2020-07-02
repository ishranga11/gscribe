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
const {WebhookClient} = require('dialogflow-fulfillment');
const axios = require('axios');

/**
 * This function handles all the webhook calls received from dialogflow
 */
exports.dialogflowFirebaseFulfillment = functions.https.onRequest((request, response) => {
    const agent = new WebhookClient({ request, response });

    /**
     * Gets the exam corresponding to the examId and returns the exam object
     * @param examId
     * @returns exam object
     */
    function getExam ( examId ){
        let url = 'https://359fd257c62a.ngrok.io/api/exam/foruser/' + examId;
        return axios.get( url ).then( requestResult => {
            return requestResult.data;
        }).catch( error =>{
            console.log( error );
            throw new Error( "Unable to get exam ");
        });
    }

    /**
     * When the user initiates the call for starting exam
     * Then take the user credentials from the context
     * Delete the user credentials context to avoid change in credentials during exam
     * return the user credentials object
     * @param agent
     * @returns {{rollNumber, examId, username}}
     */
    function getUserParameter ( agent ){
        let rollNumber = agent.context.get('exam_roll').parameters.rollNumber;
        let username = agent.context.get('exam_username').parameters.username;
        let examId = agent.context.get('exam_id').parameters.examId;
        let user = {
            'rollNumber': rollNumber,
            'username': username,
            'examId': examId
        }
        agent.context.delete('exam_roll');
        agent.context.delete('exam_username');
        agent.context.delete('exam_id');
        agent.context.delete('exam_start_request');
        return user;
    }

    /**
     * On start of exam create the answers list to be stored in context
     * Initially no answer is attempted so mark them not attempted
     * @param counter
     * @returns {{answersList: []}}
     */
    function  getAnswersParameter ( counter ) {
        let answers = [];
        for ( let i=0; i<counter; i++ ) answers.push( "Not attempted yet");
        return {'answersList': answers };
    }

    /**
     * Whenever a new question is to be loaded into the context then this function is called
     * Using the question number and exam object from the context, extract the question
     * Create the current question object to be stored into the context
     * Handle different type of questions
     * @param questionNum
     * @param examObject
     * @returns {{questionPointsString: string, questionStatementString: *, questionOptionsString: string, questionNumberString: string, questionNumber: *}}
     */
    function getCurrentQuestionParameter ( questionNum, examObject ){
        let question = examObject.exam.questions[questionNum-1];
        let questionNumberString = "question number " + question.questionNumber;
        let questionPointsString = "This question is for " + question.points + " points";
        let questionStatementString =  question.statement;
        let questionOptionsString = "This is a subjective type question. So no options are available for this question";
        if ( question.type==="MCQ" ){
            questionOptionsString = " Option A: " + question.options[0] + " ,Option B: " + question.options[1] + " ,Option C: " + question.options[2] + " ,Option D: " + question.options[3];
        }
        return {
            'questionNumberString': questionNumberString,
            'questionPointsString': questionPointsString,
            'questionStatementString': questionStatementString,
            'questionOptionsString': questionOptionsString,
            'questionNumber': questionNum
        } ;
    }

    /**
     * Whenever a new question is to be loaded into the context then this function is called
     * Using the question number of current question and answers list from context load the current answer
     * @param questionNum
     * @param answerObject
     * @returns {{answer: *}}
     */
    function getCurrentAnswerParameter ( questionNum, answerObject ){
        return { 'answer':answerObject[questionNum-1]};
    }

    /**
     * Whenever a new question is to be loaded into the context then this function is called
     * This function returns a conversational message to be sent to the agent.
     * Here it normally lists all the question parameters to be told to the user
     * @param questionObject
     * @returns {string}
     */
    function returnQuestionString ( questionObject ){
        return ( questionObject.questionNumberString + ". " + questionObject.questionPointsString + ". " + questionObject.questionStatementString + ". " + questionObject.questionOptionsString );
    }

    /**
     * This function is called when the exam starts
     * First current user object is created containing user credentials like ( roll number, username ) and examId ti be stored in context
     * A new answers list is made to be stored in the context
     * Exam object is fetched to be loaded into the context
     * current question and current answer ( here for question 1 ) are loaded into the context
     * Response sent is the current question
     * @param agent
     */
    async function startExam(agent){
        try {
            let currentExamObject = await getExam(agent.context.get('exam_id').parameters.examId);
            let answersParameter = await getAnswersParameter(currentExamObject.exam.questions.length);
            let newQuestionParameter = await getCurrentQuestionParameter(1, currentExamObject);
            let userParameter = await getUserParameter(agent);
            let currentAnswerParameter = {'answer': 'Not attempted yet'};
            agent.context.set('exam', 1000, currentExamObject);
            agent.context.set('answers', 1000, answersParameter);
            agent.context.set('current_question', 1000, newQuestionParameter);
            agent.context.set('user', 1000, userParameter);
            agent.context.set('current_answer', 1000, currentAnswerParameter);
            agent.addResponse_(returnQuestionString(newQuestionParameter));
        } catch (e) {
            agent.addResponse_( e.message );
        }
    }

    /**
     * When user asks for previous question then this function is called
     * It extracts examObject, answersObject and question number from the context
     * If the question is already question 1 then there is no previous question to it
     * Using the details in context we load the new question and corresponding answer stored
     * Response sent is the new question
     * @param agent
     */
    function setPreviousQuestion ( agent ){
        let questionNum = agent.context.get('current_question').parameters.questionNumber;
        let examObject = agent.context.get('exam').parameters;
        let answersObject = agent.context.get('answers').parameters.answersList;
        if ( questionNum === 1 ){
            agent.addResponse_('This is question Number 1. No question previous to this one.');
        } else {
            let newQuestionParameter = getCurrentQuestionParameter(questionNum - 1, examObject);
            let newAnswerParameter = getCurrentAnswerParameter(questionNum - 1, answersObject);
            agent.context.set('current_question', 1000, newQuestionParameter);
            agent.context.set('current_answer', 1000, newAnswerParameter);
            agent.addResponse_(returnQuestionString(newQuestionParameter));
        }
    }

    /**
     * When user asks for next question then this function is called
     * It extracts examObject, answersObject and question number from the context
     * If the question is already last question then there is no next question to it
     * Using the details in context we load the new question and corresponding answer stored
     * Response sent is the new question
     * @param agent
     */
    function setNextQuestion ( agent ){
        let questionNum = agent.context.get('current_question').parameters.questionNumber;
        let examObject = agent.context.get('exam').parameters;
        let answersObject = agent.context.get('answers').parameters.answersList;
        if ( questionNum === examObject.exam.questions.length ){
            agent.addResponse_('This is the last question. No next question available.');
        } else {
            let newQuestionParameter = getCurrentQuestionParameter(questionNum + 1, examObject);
            let newAnswerParameter = getCurrentAnswerParameter(questionNum + 1, answersObject);
            agent.context.set('current_question', 1000, newQuestionParameter);
            agent.context.set('current_answer', 1000, newAnswerParameter);
            agent.addResponse_(returnQuestionString(newQuestionParameter));
        }
    }

    /**
     * When user asks to jump or move to another question then this function is called
     * It extracts examObject, answersObject from the context
     * It takes the new question requested from the query
     * If the question is not in a valid range of 1 - number of questions in exam, then throw error response
     * Using the details in context we load the new question and corresponding answer stored
     * Response sent is the new question
     * @param agent
     */
    function setQuestionJump ( agent ){
        let examObject = agent.context.get('exam').parameters;
        let answersObject = agent.context.get('answers').parameters.answersList;
        let newQuestionNumber = parseInt( agent.parameters.questionNumber , 10 );
        if (newQuestionNumber<1 || newQuestionNumber>examObject.exam.questions.size ){
            agent.addResponse_('Question should be between 1 and ' + examObject.exam.questions.size + ' , both included ');
        } else {
            let newQuestionParameter = getCurrentQuestionParameter(newQuestionNumber, examObject);
            let newAnswerParameter = getCurrentAnswerParameter(newQuestionNumber, answersObject);
            agent.context.set('current_question', 1000, newQuestionParameter);
            agent.context.set('current_answer', 1000, newAnswerParameter);
            agent.addResponse_(returnQuestionString(newQuestionParameter));
        }
    }

    /**
     * This function is called when the user submits a question
     * It extracts the question number and answers list from the context
     * Updates the answer list and current answer with the query received
     * Returns a response saying that the answer is recorded
     * @param agent
     */
    function setAnswer ( agent ){
        let questionNum = agent.context.get('current_question').parameters.questionNumber;
        let answersObject = agent.context.get('answers').parameters.answersList;
        let answer = agent.query;
        answersObject[questionNum-1] = answer;
        agent.context.set( 'current_answer', 1000, { 'answer':answer} );
        agent.context.set( 'answers', 1000, { 'answersList': answersObject} );
        agent.addResponse_( 'Answer recorded: ' + answer );
    }

    function submitExam ( agent ) {
        let answersObject = agent.context.get('answers').parameters.answersList;
        let userCredentials =  agent.context.get('user').parameters;
        let examObject = agent.context.get('exam').parameters;
        console.log ( answersObject );
        console.log ( userCredentials );
        console.log ( examObject );
        agent.context.delete('user');
        agent.context.delete('answers');
        agent.context.delete('current_question');
        agent.context.delete('current_answer');
        agent.context.delete('exam');
        agent.addResponse_("Exam Submitted successfully");
    }

    /**
     * Sets the authentication failure for the agent
     * @param agent
     */
    function setAuthorizationError ( agent ){
        agent.addResponse_("Authentication Failed");
    }

    /**
     * This map matches all the intents to the corresponding function to be called
     * @type {Map<any, any>}
     */
    let intentMap = new Map();
    intentMap.set('exam.start.submitRequest', startExam);
    intentMap.set('question.previous', setPreviousQuestion);
    intentMap.set('question.next', setNextQuestion);
    intentMap.set('question.jump', setQuestionJump);
    intentMap.set('answer', setAnswer);
    intentMap.set('exam.submit', submitExam );

    /**
     * This function checks if the basic authorization is present in header validating the source
     * From the header get authorization field
     * Check the type of authorization is Basic
     * Convert it from base64 encoding to ascii
     * Check that the username and password matches
     * If the authentication fails then call setAuthorizationError function
     */
    new Promise( (resolve,reject) =>{
        let authHeader = request.headers.authorization;
        let username = "testing1";
        let password = "testing2";
        let input = authHeader.split(/\s+/);
        if (input[0] !== "Basic") {
            reject(new Error("Incorrect origin") );
        } else {
            let data = input[1];
            let buff = new Buffer.from(data, 'base64');
            let decoded = buff.toString('ascii');
            let splitAuth = decoded.split(':');
            let usernameFromHeader = splitAuth[0];
            let passwordFromHeader = splitAuth[1];
            if (usernameFromHeader !== username || passwordFromHeader !== password) {
                reject(new Error("Incorrect origin") );
            } else resolve();
        }
    }).then( ()=>{
        return agent.handleRequest(intentMap)
    }).catch( ()=>{
        return agent.handleRequest(setAuthorizationError);
    });
});