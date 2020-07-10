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

package daoTesting;

import com.google.googleinterns.gscribe.dao.UserTokenDao;
import com.google.googleinterns.gscribe.models.User;
import module.DBIProvider;
import org.junit.jupiter.api.*;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserTokenDaoTesting {

    static Handle handle;
    static DBI dbi;
    static private UserTokenDao userTokenDao;

    @BeforeAll
    public static void init() {
        dbi = new DBIProvider().getDBI();
        userTokenDao = dbi.onDemand(UserTokenDao.class);
    }

    @AfterAll
    public static void tearDownAll() {
        handle = dbi.open();
        handle.insert("delete from user where id = 'user'");
        handle.close();
    }

    @BeforeEach
    public void setUp() {
        handle = dbi.open();
        handle.insert("delete from user where id = 'user'");
        handle.close();
    }

    @Test
    public void insertUserTokenTest() {

        User userToAdd = new User("user", "a_token", "r_token", null);

        userTokenDao.insertUserToken(userToAdd);
        handle = dbi.open();
        List<Map<String, Object>> result = handle.createQuery("select * from user where id = 'user'").list();
        handle.close();

        assertFalse(result.isEmpty());
        Map<String, Object> user = result.get(0);
        assertEquals(userToAdd.getId(), user.get("id"));
        assertEquals(userToAdd.getAccessToken(), user.get("access_token"));
        assertEquals(userToAdd.getRefreshToken(), user.get("refresh_token"));

    }

    @Test
    public void insertUserTokenTwiceUpdateTest() {

        User userToAdd = new User("user", "a_token", "r_token", null);
        User userToAdd2 = new User("user", "a_token2", "r_token2", null);

        userTokenDao.insertUserToken(userToAdd);
        userTokenDao.insertUserToken(userToAdd2);
        handle = dbi.open();
        List<Map<String, Object>> result = handle.createQuery("select * from user where id = 'user'").list();
        handle.close();

        assertFalse(result.isEmpty());
        Map<String, Object> user = result.get(0);
        assertEquals(userToAdd2.getId(), user.get("id"));
        assertEquals(userToAdd2.getAccessToken(), user.get("access_token"));
        assertEquals(userToAdd2.getRefreshToken(), user.get("refresh_token"));

    }

    @Test
    public void insertUserTokenWithIDNullTest() {

        assertThrows(UnableToExecuteStatementException.class, () -> {
            User userToAdd = new User(null, "a_token", "r_token", null);
            userTokenDao.insertUserToken(userToAdd);
        });

    }

    @Test
    public void insertUserTokenWithAccessTokenNullTest() {

        assertThrows(UnableToExecuteStatementException.class, () -> {
            User userToAdd = new User("user", null, "r_token", null);
            userTokenDao.insertUserToken(userToAdd);
        });

    }

    @Test
    public void insertUserTokenWithRefreshTokenNullTest() {

        assertThrows(UnableToExecuteStatementException.class, () -> {
            User userToAdd = new User("user", "a_token", null, null);
            userTokenDao.insertUserToken(userToAdd);
        });

    }

    @Test
    public void getUserTokenTest() {

        handle = dbi.open();
        handle.insert("insert into user(id,access_token,refresh_token) values ('user','a_token','r_token') ");
        handle.close();

        User user = userTokenDao.getUserToken("user");
        assertEquals("user", user.getId());
        assertEquals("a_token", user.getAccessToken());
        assertEquals("r_token", user.getRefreshToken());

    }

    @Test
    public void getUserTokenNotInDatabaseTest() {
        User user = userTokenDao.getUserToken("user2");
        assertNull(user);
    }

    @Test
    public void getUserTokenByExamIDTest() {

        handle = dbi.open();
        handle.insert("insert into user(id,access_token,refresh_token) values ('user','a_token','r_token') ");
        handle.insert("insert into exam(id,created_by,spreadsheet_id,duration) values ( 1,'user','spreadsheet_id',100)");
        handle.close();

        User user = userTokenDao.getUserTokenByExamID(1);
        assertEquals("user", user.getId());
        assertEquals("a_token", user.getAccessToken());
        assertEquals("r_token", user.getRefreshToken());

        handle = dbi.open();
        handle.insert("delete from exam where id = 1");
        handle.close();

    }

    @Test
    public void getUserTokenByExamIDExamNotPresentTest() {

        handle = dbi.open();
        handle.insert("insert into user(id,access_token,refresh_token) values ('user','a_token','r_token') ");
        handle.close();

        User user = userTokenDao.getUserTokenByExamID(1);
        assertNull(user);

    }

    @AfterEach
    public void tearDown() {

    }

}
