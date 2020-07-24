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

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.googleinterns.gscribe.dao.UserTokenDao;
import com.google.googleinterns.gscribe.services.ExamService;
import com.google.googleinterns.gscribe.services.SpreadsheetService;
import com.google.googleinterns.gscribe.services.TokenService;
import com.google.googleinterns.gscribe.services.impl.ExamServiceImpl;
import com.google.googleinterns.gscribe.services.impl.SpreadsheetServiceImpl;
import com.google.googleinterns.gscribe.services.impl.TokenServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ServicesModule extends AbstractModule {

    private final String actionsClientID;

    public ServicesModule(String actionsClientID) {
        this.actionsClientID = actionsClientID;
    }

    /**
     * TODO:
     * - Add Inject constructors in the all Service implementations
     * - Remove the injection here for the implementations
     */

    @Inject
    @Provides
    @Singleton
    public ExamService examParserServiceProvider(SpreadsheetService spreadsheetService) {
        return new ExamServiceImpl(spreadsheetService);
    }

    @Inject
    @Provides
    @Singleton
    public TokenService tokenServiceProvider(GoogleClientSecrets googleClientSecrets, NetHttpTransport netHttpTransport) {
        return new TokenServiceImpl(googleClientSecrets, netHttpTransport, actionsClientID);
    }

    @Inject
    @Provides
    @Singleton
    public SpreadsheetService sheetServiceProvider(TokenService tokenService, UserTokenDao userTokenDao, NetHttpTransport http_transport) {
        return new SpreadsheetServiceImpl(tokenService, userTokenDao, http_transport);
    }

}
