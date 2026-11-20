package shopping.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;

class MemberTest {

    @Test
    void authenticatesOnlyMatchingPassword() {
        Member member = new Member(new Email("yeji@example.com"), new Password("hello123"));

        assertThatCode(() -> member.authenticate(new Password("hello123")))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> member.authenticate(new Password("Hello123")))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void requiresBothCredentialsToCreateMember() {
        Email email = new Email("yeji@example.com");
        Password password = new Password("hello123");

        assertThatCode(() -> new Member(email, password)).doesNotThrowAnyException();
        assertThatIllegalArgumentException().isThrownBy(() -> new Member(null, password));
        assertThatIllegalArgumentException().isThrownBy(() -> new Member(email, null));
    }
}
