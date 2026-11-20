package shopping.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import shopping.auth.domain.AccessToken;
import shopping.auth.persistence.TokenRepository;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;

class AuthenticationServiceTest {

    private final TokenRepository tokens = mock(TokenRepository.class);
    private final Clock clock = Clock.fixed(Instant.EPOCH.plusSeconds(3600), ZoneOffset.UTC);
    private final AuthenticationService service = new AuthenticationService(tokens, clock);

    @Test
    void identifiesMemberFromStoredToken() {
        when(tokens.findById("valid"))
                .thenReturn(Optional.of(AccessToken.issue("valid", 7L, clock.instant())));

        assertThat(service.authenticate("valid")).isEqualTo(7L);
    }

    @Test
    void missingTokenIsRejected() {
        when(tokens.findById("missing")).thenReturn(Optional.empty());

        assertInvalid("missing");
    }

    @Test
    void checksExpirationUsingCurrentTime() {
        when(tokens.findById("expired"))
                .thenReturn(Optional.of(AccessToken.issue("expired", 7L, Instant.EPOCH)));

        assertInvalid("expired");
    }

    private void assertInvalid(String value) {
        assertThatThrownBy(() -> service.authenticate(value))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(ErrorCode.INVALID_TOKEN));
    }
}
