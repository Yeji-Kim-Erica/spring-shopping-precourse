package shopping.auth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;

class BearerTokenTest {

    @Test
    void extractsTokenIgnoringSchemeCase() {
        assertThat(BearerToken.parse("bEaReR random-token")).isEqualTo("random-token");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Basic abc", "Bearer", "Bearer ", "Bearer a b"})
    void rejectsAbsentOrMalformedHeader(String header) {
        assertThatThrownBy(() -> BearerToken.parse(header))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(ErrorCode.INVALID_TOKEN));
    }
}
