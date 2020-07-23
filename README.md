# GScribe: Google Scribe

## Source Code Headers

Every file containing source code must include copyright and license
information. This includes any JS/CSS files that you might be serving out to
browsers. (This is to help well-intentioned people avoid accidental copying that
doesn't comply with the license.)

Apache header:

    Copyright 2020 Google LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

Despite growing awareness of accessibility issues for blind students, 
the specially-abled students face a lot of challenges in getting the education. 
Coming to the examination scenario for such students, numerous challenges are faced among which 
shortage of scribes is one. GScribe tries to solve this problem giving a flexible and easy to
use platform using the powers of Google assistant.

This Application is divided into several components as follows:
- [Dropwizard](http://dropwizard.io/) application runs on Google Cloud Platform's
[App Engine Flexible Environment](https://cloud.google.com/appengine/docs/flexible/). 
- [Node JS](https://nodejs.org/en/) application runs on [firebase functions](https://firebase.google.com/)
- [Dialogflow](https://cloud.google.com/dialogflow/docs) agent combined with [Actions on google](https://developers.google.com/assistant)
which interacts with the Node JS application
- The database used is [MySQL](https://www.mysql.com/) hosted on [Cloud SQL](https://cloud.google.com/sql/)

## Project Requirements
The project requires following tools for building the application:
* Git
* MySQL
* Ngrok
* Node.js npm
* Firebase
* Java

### Setting up GCP Project
As the project uses Google Cloud Platform for using Google Sheets API, Dialogflow,
Actions on Google, OAuth 2.0, App engine and Cloud SQL, so it is needed to setup a
Google Cloud Platform project. Follow the following steps:
* Make a new project
* Enable Google Sheets API
* Create Credentials for OAuth as:
    * Go to: APIs and Services > Credentials
    * Create Credentials for Web Application
    * Set Authorized javascript origins <br>
    ( for local deployment :- { http://localhost, http://localhost:8080 })
    * Set Authorized redirect url <br>
    ( for local deployment :- { http://localhost:8080 })
    * Download the credentials file and rename it as credentials.json for later use
* Account linking for Actions on Google
    * Go to Dialogflow console
    * Create a new agent ( This will create a new project too )
    * Go to Actions console and select the newly created agent project
    * Go to Develop > Account linking 
    * Turn on the account linking with google sign-in linking type
    * Copy the newly created Actions Client ID and save the settings

## Running Locally

First clone the repository as:
```
> git clone https://github.com/googleinterns/gscribe
```

### Setting up the Database
For the application, two SQL dumps are provided for different versions of MySQL. <br>
**gscribe.sql** for MySQL v8.0 and **gscribe_MySQL5.7** for MySQL v5.7.
* Move to the resources folder:
```
> cd ./gscribe/appengine/src/main/resources
```
* Create a new database in mysql:
```
mysql > create database DATABASE_NAME
```
* Import the SQL dump file:
```
> mysql -u < USERNAME > -p DATABASE_NAME < gscribe.sql
OR
> mysql -u < USERNAME > -p DATABASE_NAME < gscribe_MySQL5.7
```

### Running Dropwizard Application
Setting up the application:
* Set the database connection configuration in gscribe.yaml <br>
( e.g. username-root, password-pass, url-jdbc:mysql://localhost:3306/gscribe ) 
* Set the Actions Client ID in gscribe.yaml
* Move the credentials.json file to this same resources folder <br>
( gscribe/appengine/src/main/resources )
* Copy the Client ID from credentials.json and fill in assets/app.js file
under gapi initialization.

Now, for running the application:
* Go to the appengine base folder
```
> cd ./gscribe/appengine
```
* Build the fat jar for the application and install dependencies:
```
> mvn clean package install
```
* Running the application:
```
> java -jar target/gscribe-1.0-SNAPSHOT.jar server gscribe.yaml
```
The application will be running on http://localhost:8080/

### Running Firebase functions
As the firebase functions need to interact with dropwizard APIs, for local 
deployment create a tunnel using ngrok as:
```
> ngrok http 8080
```
Setting up firebase functions:
* Copy the Https forwarding link of ngrok tunnel for dropwizard application and paste
it in the firebase functions index.js file located in ( gscribe/firebase/functions/index.js ). 
* Fill Actions Client ID in dialogflow initialization in firebase functions index.js file.
* Update project id in .firebaserc located in ./gscribe/firebase ( might be hidden initially ).
The project id is found in Dialogflow Agent general tab.

To run firebase functions locally:
* Move to firebase functions folder
```
> cd ./gscribe/firebase/functions
```
* Installing dependencies with npm:
```
> npm install
```
* Running the firebase functions locally:
```
> npm run serve
```

Firebase functions is running on http://localhost:5001. <br>
For logs - http://localhost:4000/functions

### Setting up Dialogflow Agent
As the Dialogflow agent interacts with firebase for fulfillment, create a local
tunnel using ngrok as:
```
> ngrok http 5001
```

Setting up the agent:
* To import the agent go to: Settings > Export and Import
* Import the dialogflowAgent zip file ( from ./gscribe/firebase/ folder ).
* Setup Webhook call to the firebase functions for fulfillment by providing url. The url to 
be provided is the ngrok forwarding link for firebase functions and add <br>
/{project id}/{region}/dialogflowFirebaseFulfillment. <br>
( e.g. https://000000.ngrok.io/test-id/us-central1/dialogflowFirebaseFulfillment )
* Save the settings