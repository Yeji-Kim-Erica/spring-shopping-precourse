package shopping.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;

class AccessTokenTest {

    @Test
    void authenticatesUntilExpirationWithoutExtendingIt() {
        Instant issuedAt = Instant.EPOCH;
        AccessToken token = AccessToken.issue("issued", 7L, issuedAt);

        assertThat(token.authenticateAt(issuedAt.plusSeconds(3599))).isEqualTo(7L);
        assertThatThrownBy(() -> token.authenticateAt(issuedAt.plusSeconds(3600)))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(ErrorCode.INVALID_TOKEN));
    }

    @Test
    void issuedTokenExpiresExactlyOneHourLater() {
        Instant issuedAt = Instant.parse("2026-11-21T00:00:00Z");
        AccessToken token = AccessToken.issue("random-token", 7L, issuedAt);

        assertThat(token.isExpired(issuedAt.plusSeconds(3600).minusNanos(1))).isFalse();
        assertThat(token.isExpired(issuedAt.plusSeconds(3600))).isTrue();
    }
}
