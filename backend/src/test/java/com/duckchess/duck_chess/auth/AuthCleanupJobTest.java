package com.duckchess.duck_chess.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthCleanupJobTest 
{

    private UserRepository users;
    private EmailVerificationRepository verifications;
    private AuthCleanupJob job;

    @BeforeEach
    void setUp() 
    {
        users = mock(UserRepository.class);
        verifications = mock(EmailVerificationRepository.class);
        AuthProperties props = new AuthProperties(60, 7, 30);
        job = new AuthCleanupJob(users, verifications, props);
    }

    @Test
    void purgesUsersOlderThanConfiguredCutoff() 
    {
        when(users.deleteUnverifiedOlderThan(any())).thenReturn(3);
        when(verifications.deleteVerifiedOrExpired(any())).thenReturn(5);

        job.purgeStale();

        ArgumentCaptor<OffsetDateTime> userCutoff = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(users).deleteUnverifiedOlderThan(userCutoff.capture());
        OffsetDateTime expected = OffsetDateTime.now().minusDays(7);
        assertThat(userCutoff.getValue()).isBetween(
                expected.minusSeconds(5), expected.plusSeconds(5));
    }

    @Test
    void runsBothCleanupsEvenWhenFirstReturnsZero() 
    {
        when(users.deleteUnverifiedOlderThan(any())).thenReturn(0);
        when(verifications.deleteVerifiedOrExpired(any())).thenReturn(0);

        job.purgeStale();

        verify(users).deleteUnverifiedOlderThan(any());
        verify(verifications).deleteVerifiedOrExpired(any());
    }
}