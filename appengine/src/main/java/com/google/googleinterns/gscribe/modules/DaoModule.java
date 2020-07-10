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

package com.google.googleinterns.gscribe.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.googleinterns.gscribe.dao.*;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.skife.jdbi.v2.DBI;

public class DaoModule extends AbstractModule {

    @Inject
    @Provides
    @Singleton
    public UserTokenDao userTokenDaoProvider(DBI dbi) {
        return dbi.onDemand(UserTokenDao.class);
    }

    @Inject
    @Provides
    @Singleton
    public ExamMetadataDao examMetadataDaoProvider(DBI dbi) {
        return dbi.onDemand(ExamMetadataDao.class);
    }

    @Inject
    @Provides
    @Singleton
    public QuestionsDao examDaoProvider(DBI dbi, ObjectMapper objectMapper) {
        dbi.registerMapper(new QuestionsDao.QuestionsMapper(objectMapper));
        return dbi.onDemand(QuestionsDao.class);
    }

    @Inject
    @Provides
    @Singleton
    public ExamInstanceDao examInstanceDaoProvider(DBI dbi) {
        return dbi.onDemand(ExamInstanceDao.class);
    }

    @Inject
    @Provides
    @Singleton
    public AnswerDao answerDaoProvider(DBI dbi, ObjectMapper objectMapper) {
        dbi.registerMapper(new AnswerDao.AnswersMapper(objectMapper));
        return dbi.onDemand(AnswerDao.class);
    }

}
