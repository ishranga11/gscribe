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

SET MODE MYSQL;

DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS exam_instance;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS exam;
DROP TABLE IF EXISTS user;

create table user (
  id varchar(300) not null,
  access_token varchar(2048) not null,
  refresh_token varchar(512) not null,
  timestamp timestamp not null DEFAULT CURRENT_TIMESTAMP,
  primary key (id)
);

create table exam (
  id int not null AUTO_INCREMENT,
  created_by varchar(300) not null,
  spreadsheet_id varchar(100) not null,
  duration int not null,
  created_on timestamp not null DEFAULT CURRENT_TIMESTAMP,
  primary key (id),
  KEY exam_db_to_user_db_id_idx (created_by),
  CONSTRAINT examDB_to_userDB_id FOREIGN KEY (created_by) REFERENCES user (id)
);

create table questions (
  exam_id int not null,
  questions varchar(5000) not null,
  primary key (exam_id),
  CONSTRAINT questionDB_to_examDB_examID FOREIGN KEY (exam_id) REFERENCES exam (id)
);

create table exam_instance (
  id int not null AUTO_INCREMENT,
  exam_id int not null,
  user_id varchar(300) not null,
  student_roll_num int not null,
  start_time timestamp not null DEFAULT CURRENT_TIMESTAMP,
  end_time timestamp NULL DEFAULT NULL,
  primary key (id),
  KEY examtakenDB_to_examDB_examID_idx (exam_id),
  CONSTRAINT examinstanceDB_to_examDB_exam_id FOREIGN KEY (exam_id) REFERENCES exam (id)
);

create table answers (
  exam_instance_id int not null,
  answers varchar(5000) not null,
  primary key (exam_instance_id),
  CONSTRAINT answersDB_to_examInstanceDB_id FOREIGN KEY (exam_instance_id) REFERENCES exam_instance (id)
);

