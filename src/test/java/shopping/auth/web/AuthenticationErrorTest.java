package shopping.auth.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.common.web.ErrorHandler;

class AuthenticationErrorTest {

    @Test
    void authenticationFailureReturnsBearerChallenge() {
        var response = new ErrorHandler().handleShopping(
                new ShoppingException(ErrorCode.INVALID_TOKEN));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE))
                .isEqualTo("Bearer");
    }
}
