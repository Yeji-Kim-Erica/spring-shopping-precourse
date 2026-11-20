package shopping.auth.web;

import org.springframework.stereotype.Component;
import shopping.auth.application.AuthenticationService;

@Component
public class CurrentMember {

    private final AuthenticationService authentication;

    public CurrentMember(AuthenticationService authentication) {
        this.authentication = authentication;
    }

    public Long resolve(String authorization) {
        return authentication.authenticate(BearerToken.parse(authorization));
    }
}
