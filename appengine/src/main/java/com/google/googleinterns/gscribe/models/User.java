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

package com.google.googleinterns.gscribe.models;

import java.sql.Timestamp;
import java.util.Objects;

public class User {

    private String id;
    private String accessToken;
    private String refreshToken;
    private Timestamp timestamp;

    public User() {
    }

    public User(String id, String accessToken, String refreshToken, Timestamp timestamp) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id.equals(user.id) &&
                accessToken.equals(user.accessToken) &&
                refreshToken.equals(user.refreshToken) &&
                Objects.equals(timestamp, user.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accessToken, refreshToken, timestamp);
    }
}
