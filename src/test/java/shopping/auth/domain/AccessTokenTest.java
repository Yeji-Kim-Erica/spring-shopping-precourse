package shopping.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AccessTokenTest {

    @Test
    void issuedTokenExpiresExactlyOneHourLater() {
        Instant issuedAt = Instant.parse("2026-11-21T00:00:00Z");
        AccessToken token = AccessToken.issue("random-token", 7L, issuedAt);

        assertThat(token.isExpired(issuedAt.plusSeconds(3600).minusNanos(1))).isFalse();
        assertThat(token.isExpired(issuedAt.plusSeconds(3600))).isTrue();
    }
}
