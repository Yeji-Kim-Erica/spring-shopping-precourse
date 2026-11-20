package shopping.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;

class ProductDetailsTest {

    @Test
    void acceptsAllowedNameCharactersAtMaximumLength() {
        assertThatCode(() -> details("상품()[]+-&/_ 123", 1)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "상품!", "가나다라마바사아자차카타파하가나"})
    void rejectsInvalidNames(String name) {
        assertThatIllegalArgumentException().isThrownBy(() -> details(name, 1));
    }

    @Test
    void acceptsPositiveIntegerPriceBoundaries() {
        assertThatCode(() -> details("상품", 1)).doesNotThrowAnyException();
        assertThatCode(() -> details("상품", Integer.MAX_VALUE)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rejectsMissingOrNonPositivePrice(Integer price) {
        assertThatIllegalArgumentException().isThrownBy(() -> details("상품", price));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"image.png", "http://", "ftp://example.com/a"})
    void requiresHttpUrlWithHost(String url) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ProductDetails("상품", 1, url));
    }

    @Test
    void rejectsProfanityButAcceptsCleanName() {
        ProductDetails details = details("상품", 1);

        assertThatCode(() -> details.checkProfanity(text -> false)).doesNotThrowAnyException();
        assertThatThrownBy(() -> details.checkProfanity(text -> true))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(ErrorCode.PROFANITY_DETECTED));
    }

    private ProductDetails details(String name, Integer price) {
        return new ProductDetails(name, price, "https://example.com/image.png");
    }
}
