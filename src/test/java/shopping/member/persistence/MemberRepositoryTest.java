package shopping.member.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
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
    @Autowired
    private EntityManager entityManager;

    @Test
    void retrievesStoredCredentialsForAuthentication() {
        Member saved = members.saveAndFlush(member("yeji@example.com"));
        entityManager.clear();

        Member found = members.findByEmail(new Email("yeji@example.com")).orElseThrow();

        assertThat(found.id()).isEqualTo(saved.id());
        assertThatCode(
                () -> found.authenticate(new Password("hello123"))).doesNotThrowAnyException();
    }

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
