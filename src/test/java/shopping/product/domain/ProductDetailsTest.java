package shopping.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;

@DisplayName("상품 정보 검증")
class ProductDetailsTest {

    @DisplayName("허용된 문자로 구성된 상품 이름은 생성할 수 있다")
    @Test
    void acceptsAllowedNameCharactersAtMaximumLength() {
        assertThatCode(() -> details("상품()[]+-&/_ 123", 1)).doesNotThrowAnyException();
    }

    @DisplayName("누락되거나 길이와 문자 규칙을 어긴 상품 이름은 거절한다")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "상품!", "가나다라마바사아자차카타파하가나"})
    void rejectsInvalidNames(String name) {
        assertThatIllegalArgumentException().isThrownBy(() -> details(name, 1));
    }

    @DisplayName("상품 가격은 1원과 정수 최댓값을 허용한다")
    @Test
    void acceptsPositiveIntegerPriceBoundaries() {
        assertThatCode(() -> details("상품", 1)).doesNotThrowAnyException();
        assertThatCode(() -> details("상품", Integer.MAX_VALUE)).doesNotThrowAnyException();
    }

    @DisplayName("가격이 누락되거나 0원 이하이면 거절한다")
    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rejectsMissingOrNonPositivePrice(Integer price) {
        assertThatIllegalArgumentException().isThrownBy(() -> details("상품", price));
    }

    @DisplayName("이미지 URL은 호스트가 있는 HTTP 또는 HTTPS 형식이어야 한다")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"image.png", "http://", "ftp://example.com/a"})
    void requiresHttpUrlWithHost(String url) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ProductDetails("상품", 1, url));
    }

    @DisplayName("비속어가 없는 상품 이름만 검사를 통과한다")
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
