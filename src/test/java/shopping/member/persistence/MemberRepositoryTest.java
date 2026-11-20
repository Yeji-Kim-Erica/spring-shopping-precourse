package shopping.member.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shopping.member.domain.Email;
import shopping.member.domain.Member;
import shopping.member.domain.Password;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository members;

    @Test
    void assignsIdAndFindsNormalizedEmailCaseSensitively() {
        Member saved = members.saveAndFlush(member(" Yeji@example.com "));

        assertThat(saved.id()).isPositive();
        assertThat(members.existsByEmail(new Email("Yeji@example.com"))).isTrue();
        assertThat(members.existsByEmail(new Email("yeji@example.com"))).isFalse();
    }

    @Test
    void databaseRejectsDuplicateEmail() {
        members.saveAndFlush(member("yeji@example.com"));

        assertThatThrownBy(() -> members.saveAndFlush(member("yeji@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Member member(String email) {
        return new Member(new Email(email), new Password("hello123"));
    }
}
