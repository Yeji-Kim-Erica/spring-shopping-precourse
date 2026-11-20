package shopping.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.auth.application.TokenIssuer;
import shopping.auth.domain.AccessToken;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.member.domain.Email;
import shopping.member.domain.Member;
import shopping.member.domain.Password;
import shopping.member.persistence.MemberRepository;

@Service
public class RegistrationService {

    private final MemberRepository members;
    private final TokenIssuer tokens;

    public RegistrationService(MemberRepository members, TokenIssuer tokens) {
        this.members = members;
        this.tokens = tokens;
    }

    @Transactional
    public AccessToken register(String email, String password) {
        Member member = new Member(new Email(email), new Password(password));
        rejectDuplicate(member.email());

        Member saved = members.saveAndFlush(member);
        return tokens.issue(saved.id());
    }

    private void rejectDuplicate(Email email) {
        if (members.existsByEmail(email)) {
            throw new ShoppingException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}
