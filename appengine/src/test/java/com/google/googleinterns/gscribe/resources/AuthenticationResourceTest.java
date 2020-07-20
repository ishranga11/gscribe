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

package com.google.googleinterns.gscribe.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.googleinterns.gscribe.dao.UserTokenDao;
import com.google.googleinterns.gscribe.models.User;
import com.google.googleinterns.gscribe.resources.io.exception.InvalidRequestException;
import com.google.googleinterns.gscribe.resources.io.response.AuthenticationResponse;
import com.google.googleinterns.gscribe.services.TokenService;
import com.google.googleinterns.gscribe.services.data.TokenResponse;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(DropwizardExtensionsSupport.class)
public class AuthenticationResourceTest {

    private static final UserTokenDao userTokenDao = mock(UserTokenDao.class);
    private static final TokenService tokenService = mock(TokenService.class);
    private static final AuthenticationResource authenticationResource = new AuthenticationResource(tokenService, userTokenDao);

    private static final ResourceExtension ext = ResourceExtension.builder().addResource(authenticationResource).build();

    private static final String IDTokenValid = "IDTokenValid";
    private static final String newUserIDToken = "newUserIDToken";
    private static final String IDTokenInvalid = "IDTokenInvalid";
    private static final String randomIDTokenThrowGeneralSecurityException = "randomIDTokenThrowGeneralSecurityException";
    private static final String randomIDTokenThrowIOException = "randomIDTokenThrowIOException";
    private static final String authCodeRequest = "{ \"authCode\": \"authCode\" }";
    private static final User user = new User("userID", "accessToken", "refreshToken", null);

    @BeforeAll
    public static void setup() throws GeneralSecurityException, IOException, InvalidRequestException {
        String userID = "userID";
        String newUserID = "newUsedID";
        TokenResponse tokenResponse = new TokenResponse(user.getAccessToken(), user.getRefreshToken(), IDTokenValid);

        when(tokenService.verifyIDToken(IDTokenValid)).thenReturn(userID);
        when(tokenService.verifyIDToken(newUserIDToken)).thenReturn(newUserID);
        when(tokenService.verifyIDToken(IDTokenInvalid)).thenThrow(InvalidRequestException.class);
        when(tokenService.verifyIDToken(randomIDTokenThrowGeneralSecurityException)).thenThrow(GeneralSecurityException.class);
        when(tokenService.verifyIDToken(randomIDTokenThrowIOException)).thenThrow(IOException.class);
        when(userTokenDao.getUserToken(userID)).thenReturn(user);
        when(userTokenDao.getUserToken(newUserID)).thenReturn(null);
        when(tokenService.generateToken("authCode")).thenReturn(tokenResponse);
    }

    @Test
    public void tokenAvailableTest() {
        final AuthenticationResponse response = ext.target("/authenticate").request().header("Authentication", IDTokenValid).get(AuthenticationResponse.class);

        assertEquals("User is Authorized", response.getMessage());
    }

    @Test
    public void tokenAvailableNoHeaderTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/authenticate").request().get(AuthenticationResponse.class);
        });
    }

    @Test
    public void tokenAvailableUnauthorizedUserTest() {
        assertThrows(NotAuthorizedException.class, () -> {
            ext.target("/authenticate").request().header("Authentication", newUserIDToken).get(AuthenticationResponse.class);
        });
    }

    @Test
    public void tokenAvailableBadIDTokenTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/authenticate").request().header("Authentication", IDTokenInvalid).get(AuthenticationResponse.class);
        });
    }

    @Test
    public void tokenAvailableCatchInternalServerErrorTest() {
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/authenticate").request().header("Authentication", randomIDTokenThrowGeneralSecurityException).get(AuthenticationResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/authenticate").request().header("Authentication", randomIDTokenThrowIOException).get(AuthenticationResponse.class);
        });
    }

    @Test
    public void saveTokenTest() throws JsonProcessingException {
        AuthenticationResponse response = ext.target("/authenticate").request().header("Authentication", IDTokenValid).post(Entity.entity(authCodeRequest, MediaType.APPLICATION_JSON_TYPE), AuthenticationResponse.class);
        verify(userTokenDao).insertUserToken(user);
        assertEquals("User Authorized", response.getMessage());
    }

    @Test
    public void saveTokenNoHeaderTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/authenticate").request().post(Entity.entity(authCodeRequest, MediaType.APPLICATION_JSON_TYPE), AuthenticationResponse.class);
        });
    }

    @Test
    public void saveTokenNoAuthCodeTest() {
        String requests = "{ \"randomField\": \"randomData\" }";
        assertThrows(BadRequestException.class, () -> {
            ext.target("/authenticate").request().header("Authentication", IDTokenValid).post(Entity.entity(requests, MediaType.APPLICATION_JSON_TYPE), AuthenticationResponse.class);
        });
    }

    @Test
    public void saveTokenBadIDTokenTest() {
        assertThrows(BadRequestException.class, () -> {
            ext.target("/authenticate").request().header("Authentication", IDTokenInvalid).post(Entity.entity(authCodeRequest, MediaType.APPLICATION_JSON_TYPE), AuthenticationResponse.class);
        });
    }

    @Test
    public void saveTokenBadAuthCodeTest() throws GeneralSecurityException, IOException, InvalidRequestException {
        String requests = "{ \"authCode\": \"badAuthCode\" }";
        when(tokenService.generateToken("badAuthCode")).thenThrow(InvalidRequestException.class);
        assertThrows(BadRequestException.class, () -> {
            ext.target("/authenticate").request().header("Authentication", IDTokenValid).post(Entity.entity(requests, MediaType.APPLICATION_JSON_TYPE), AuthenticationResponse.class);
        });
    }

    @Test
    public void saveTokenAuthCodeForDifferentUserTest() {
        String requests = "{ \"authCode\": \"differentAuthCode\" }";
        TokenResponse tokenResponse = new TokenResponse("AToken", "RToken", "differentIDToken");

        assertThrows(BadRequestException.class, () -> {
            when(tokenService.generateToken("differentAuthCode")).thenReturn(tokenResponse);
            when(tokenService.verifyIDToken("differentIDToken")).thenReturn("differentUser");
            ext.target("/authenticate").request().header("Authentication", IDTokenValid).post(Entity.entity(requests, MediaType.APPLICATION_JSON_TYPE), AuthenticationResponse.class);
        });

    }

    @Test
    public void saveTokenCatchInternalServerErrorTest() {
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/authenticate").request().header("Authentication", randomIDTokenThrowGeneralSecurityException).post(Entity.entity(authCodeRequest, MediaType.APPLICATION_JSON_TYPE), AuthenticationResponse.class);
        });
        assertThrows(InternalServerErrorException.class, () -> {
            ext.target("/authenticate").request().header("Authentication", randomIDTokenThrowIOException).post(Entity.entity(authCodeRequest, MediaType.APPLICATION_JSON_TYPE), AuthenticationResponse.class);
        });
    }

}
