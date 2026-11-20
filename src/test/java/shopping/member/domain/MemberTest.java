package shopping.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void requiresBothCredentialsToCreateMember() {
        Email email = new Email("yeji@example.com");
        Password password = new Password("hello123");

        assertThatCode(() -> new Member(email, password)).doesNotThrowAnyException();
        assertThatIllegalArgumentException().isThrownBy(() -> new Member(null, password));
        assertThatIllegalArgumentException().isThrownBy(() -> new Member(email, null));
    }
}
