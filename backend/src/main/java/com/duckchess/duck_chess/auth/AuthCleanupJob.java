package com.duckchess.duck_chess.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
public class AuthCleanupJob 
{

    private static final Logger log = LoggerFactory.getLogger(AuthCleanupJob.class);

    private final UserRepository users;
    private final EmailVerificationRepository verifications;
    private final AuthProperties props;

    public AuthCleanupJob(
            UserRepository users,
            EmailVerificationRepository verifications,
            AuthProperties props
    ) 
    {
        this.users = users;
        this.verifications = verifications;
        this.props = props;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeStale() 
    {
        OffsetDateTime userCutoff = OffsetDateTime.now()
                .minusDays(props.unverifiedUserExpirationDays());
        OffsetDateTime codeCutoff = OffsetDateTime.now();

        int usersDeleted = users.deleteUnverifiedOlderThan(userCutoff);
        int codesDeleted = verifications.deleteVerifiedOrExpired(codeCutoff);

        if(usersDeleted > 0 || codesDeleted > 0) 
        {
            log.info("Cleanup complete: purged {} unverified users, {} used/expired codes",
                    usersDeleted, codesDeleted);
        }
    }

}