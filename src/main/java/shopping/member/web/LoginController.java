package shopping.member.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shopping.auth.web.TokenResponse;
import shopping.member.application.LoginService;

@RestController
public class LoginController {

    private final LoginService login;

    public LoginController(LoginService login) {
        this.login = login;
    }

    @PostMapping("/api/members/login")
    public TokenResponse login(@RequestBody MemberRequest request) {
        return new TokenResponse(login.login(request.email(), request.password()).value());
    }
}
