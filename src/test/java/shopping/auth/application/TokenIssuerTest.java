package shopping.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import shopping.auth.domain.AccessToken;
import shopping.auth.persistence.TokenRepository;

class TokenIssuerTest {

    @Test
    void persistsGeneratedTokenUsingCurrentTime() {
        TokenRepository repository = mock(TokenRepository.class);
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        when(repository.save(any(AccessToken.class))).thenAnswer(call -> call.getArgument(0));
        TokenIssuer issuer = new TokenIssuer(repository, () -> "generated", clock);

        AccessToken issued = issuer.issue(7L);

        verify(repository).save(issued);
        assertThat(issued.value()).isEqualTo("generated");
        assertThat(issued.isExpired(Instant.EPOCH.plusSeconds(3599))).isFalse();
        assertThat(issued.isExpired(Instant.EPOCH.plusSeconds(3600))).isTrue();
    }
}
