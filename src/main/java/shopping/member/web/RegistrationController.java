package shopping.member.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shopping.auth.web.TokenResponse;
import shopping.member.application.RegistrationService;

@RestController
public class RegistrationController {

    private final RegistrationService registration;

    public RegistrationController(RegistrationService registration) {
        this.registration = registration;
    }

    @PostMapping("/api/members/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@RequestBody MemberRequest request) {
        String token = registration.register(request.email(), request.password()).value();
        return new TokenResponse(token);
    }
}
