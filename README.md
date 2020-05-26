# gScribe: Google Scribe

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

This application provides an audio interface for conducting tests.


This [Dropwizard](http://dropwizard.io/) application runs on Google Cloud Platform's
[App Engine Flexible Environment](https://cloud.google.com/appengine/docs/flexible/).

## Basic Instructions

```
gcloud config set project <project id>
mvn gcloud:deploy
```

If all succeeds, you should be able to browse to http://&lt;project-id&gt;.appspot.com/ and receive
a lovely greeting.

## Running Locally

Import the project into IntelliJ, and build the fat jar:

```
mvn package
```

Start the server:

```
java -jar target/gscribe-1.0-SNAPSHOT.jar server gscribe.yaml
```

Server will be up and running at http://localhost:8080/.