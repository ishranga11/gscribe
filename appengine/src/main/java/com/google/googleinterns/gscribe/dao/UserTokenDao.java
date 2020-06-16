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

package com.google.googleinterns.gscribe.dao;

import com.google.googleinterns.gscribe.models.UserToken;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface UserTokenDao {

    @Mapper(UserTokenMapper.class)
    @SqlQuery("SELECT * from user where id = :id")
    UserToken getUserToken(@Bind("id") String id);

    @SqlUpdate("INSERT INTO user( id, access_token, refresh_token ) VALUES ( :id, :accessToken, :refreshToken )")
    void insertUserToken(@BindBean UserToken userToken);

    @SqlUpdate("UPDATE user set access_token = :accessToken, refresh_token = :refreshToken, timestamp = CURRENT_TIMESTAMP where id = :id;")
    void updateTokens(@BindBean UserToken userToken);

    class UserTokenMapper implements ResultSetMapper<UserToken> {
        @Override
        public UserToken map(int i, ResultSet resultSet, StatementContext statementContext)
                throws SQLException {
            return new UserToken(
                    resultSet.getString("id"),
                    resultSet.getString("access_token"),
                    resultSet.getString("refresh_token"),
                    resultSet.getTimestamp("timestamp")
            );
        }
    }

}
