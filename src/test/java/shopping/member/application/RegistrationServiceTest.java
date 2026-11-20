package shopping.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import shopping.auth.application.TokenIssuer;
import shopping.auth.domain.AccessToken;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.member.domain.Email;
import shopping.member.domain.Member;
import shopping.member.persistence.MemberRepository;

class RegistrationServiceTest {

    private final MemberRepository members = mock(MemberRepository.class);
    private final TokenIssuer tokens = mock(TokenIssuer.class);
    private final RegistrationService service = new RegistrationService(members, tokens);

    @Test
    void issuesTokenForSavedMember() {
        Member saved = mock(Member.class);
        AccessToken token = AccessToken.issue("issued", 7L, Instant.EPOCH);
        when(saved.id()).thenReturn(7L);
        when(members.saveAndFlush(any(Member.class))).thenReturn(saved);
        when(tokens.issue(7L)).thenReturn(token);

        assertThat(service.register(" yeji@example.com ", "hello123")).isSameAs(token);
        verify(members).existsByEmail(new Email("yeji@example.com"));
    }

    @Test
    void duplicateEmailDoesNotSaveOrIssueToken() {
        when(members.existsByEmail(new Email("yeji@example.com"))).thenReturn(true);

        assertThatThrownBy(() -> service.register("yeji@example.com", "hello123"))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code())
                                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));
        verify(members, never()).saveAndFlush(any());
        verifyNoInteractions(tokens);
    }
}
