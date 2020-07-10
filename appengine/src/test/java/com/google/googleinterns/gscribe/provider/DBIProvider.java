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

package com.google.googleinterns.gscribe.provider;

import com.google.googleinterns.gscribe.dao.QuestionsDaoTest;
import org.apache.commons.io.IOUtils;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class DBIProvider {

    public DBI getDBI() throws IOException {

        DBI dbi = new DBI("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        InputStream in = QuestionsDaoTest.class.getResourceAsStream("/schema.sql");
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer, "UTF-8");
        String sql = writer.toString();
        Handle handle = dbi.open();
        handle.createScript(sql).executeAsSeparateStatements();
        handle.close();
        return dbi;

    }

}
