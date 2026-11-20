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
public class LoginService {

    private final MemberRepository members;
    private final TokenIssuer tokens;

    public LoginService(MemberRepository members, TokenIssuer tokens) {
        this.members = members;
        this.tokens = tokens;
    }

    @Transactional
    public AccessToken login(String email, String password) {
        Email address = new Email(email);
        Password candidate = new Password(password);
        Member member = members.findByEmail(address)
                .orElseThrow(() -> new ShoppingException(ErrorCode.INVALID_CREDENTIALS));

        member.authenticate(candidate);
        return tokens.issue(member.id());
    }
}
