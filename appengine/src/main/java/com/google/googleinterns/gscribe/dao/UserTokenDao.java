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

import com.google.googleinterns.gscribe.models.User;
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

    /**
     * Queries user object for user identified with id userID
     * Called to check the availability of tokens or to use tokens for this user
     *
     * @param userID ( unique user id of user, here paper setter )
     * @return user object
     */
    @Mapper(UserTokenMapper.class)
    @SqlQuery("SELECT * from user where id = :user_id")
    User getUserToken(@Bind("user_id") String userID);

    /**
     * Queries user object for user who created exam with exam id examID
     * Called when a response is to be submitted to response sheet in google sheets for the exam identified by examID
     * As to access the spreadsheet instance user token is needed so this function is called to retrieve user object
     *
     * @param examID ( to identify particular exam )
     * @return user object
     */
    @Mapper(UserTokenMapper.class)
    @SqlQuery("SELECT user.id,access_token,refresh_token,user.timestamp FROM user inner join exam on exam.created_by=user.id where exam.id = :exam_id;")
    User getUserTokenByExamID(@Bind("exam_id") int examID);

    /**
     * Inserts new user with ( id, access token, refresh_token )
     * Or to Update the tokens for the user
     * id here denotes the unique user id of the user
     * timestamp denotes the latest time when the tokens were created or updated
     *
     * @param user ( user object )
     */
    @SqlUpdate("INSERT INTO user( id, access_token, refresh_token ) VALUES ( :id, :accessToken, :refreshToken ) " +
            "ON DUPLICATE KEY UPDATE access_token=:accessToken, refresh_token=:refreshToken, timestamp=CURRENT_TIMESTAMP")
    void insertUserToken(@BindBean User user);

    /**
     * A mapper class to map user response to user object
     */
    class UserTokenMapper implements ResultSetMapper<User> {
        @Override
        public User map(int i, ResultSet resultSet, StatementContext statementContext)
                throws SQLException {
            return new User(
                    resultSet.getString("id"),
                    resultSet.getString("access_token"),
                    resultSet.getString("refresh_token"),
                    resultSet.getTimestamp("timestamp")
            );
        }
    }

}
