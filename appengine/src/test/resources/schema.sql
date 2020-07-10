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

CREATE TABLE user (
  id varchar(300) NOT NULL,
  access_token varchar(2048) NOT NULL,
  refresh_token varchar(512) NOT NULL,
  timestamp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE exam (
  id int NOT NULL AUTO_INCREMENT,
  created_by varchar(300) NOT NULL,
  spreadsheet_id varchar(100) NOT NULL,
  duration int NOT NULL,
  created_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY exam_db_to_user_db_id_idx (created_by),
  CONSTRAINT examDB_to_userDB_id FOREIGN KEY (created_by) REFERENCES user (id)
);

CREATE TABLE questions (
  exam_id int NOT NULL,
  questions json NOT NULL,
  PRIMARY KEY (exam_id),
  CONSTRAINT questionDB_to_examDB_examID FOREIGN KEY (exam_id) REFERENCES exam (id)
);

CREATE TABLE exam_instance (
  id int NOT NULL AUTO_INCREMENT,
  exam_id int NOT NULL,
  user_id varchar(300) NOT NULL,
  student_roll_num int NOT NULL,
  start_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time timestamp NULL DEFAULT NULL,
  PRIMARY KEY (id),
  KEY examtakenDB_to_examDB_examID_idx (exam_id),
  CONSTRAINT examinstanceDB_to_examDB_exam_id FOREIGN KEY (exam_id) REFERENCES exam (id)
);

CREATE TABLE answers (
  exam_instance_id int NOT NULL,
  answers json NOT NULL,
  PRIMARY KEY (exam_instance_id),
  CONSTRAINT answersDB_to_examInstanceDB_id FOREIGN KEY (exam_instance_id) REFERENCES exam_instance (id)
);