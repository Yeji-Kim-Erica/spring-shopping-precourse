package shopping.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.domain.ProfanityChecker;
import shopping.product.persistence.ProductRepository;

@DisplayName("상품 수정 서비스")
class ProductUpdateServiceTest {

    private final ProductRepository products = mock(ProductRepository.class);
    private final ProfanityChecker checker = mock(ProfanityChecker.class);
    private final ProductUpdateService service = new ProductUpdateService(products, checker);
    private final Product product = new Product(new ProductDetails("기존", 1000, "https://a.com"));
    private final ProductDetails updated = new ProductDetails("수정", 2000, "https://b.com");

    @BeforeEach
    void findProduct() {
        when(products.findById(7L)).thenReturn(Optional.of(product));
    }

    @DisplayName("검증을 통과하면 조회한 상품의 정보를 변경한다")
    @Test
    void changesLoadedProductAfterValidation() {
        assertThat(service.update(7L, updated)).isSameAs(product);
        assertThat(product.name()).isEqualTo("수정");
    }

    @DisplayName("수정할 상품이 없으면 비속어 검사를 호출하지 않는다")
    @Test
    void missingProductDoesNotCallExternalChecker() {
        when(products.findById(7L)).thenReturn(Optional.empty());

        assertFailure(ErrorCode.PRODUCT_NOT_FOUND);
        verifyNoInteractions(checker);
    }

    @DisplayName("수정할 이름에 비속어가 있으면 기존 상품 정보를 유지한다")
    @Test
    void profanityLeavesOriginalDetailsUntouched() {
        when(checker.containsProfanity("수정")).thenReturn(true);

        assertFailure(ErrorCode.PROFANITY_DETECTED);
        assertUnchanged();
    }

    @DisplayName("수정 시 비속어 검사에 실패하면 기존 상품 정보를 유지한다")
    @Test
    void unavailableCheckLeavesOriginalDetailsUntouched() {
        when(checker.containsProfanity("수정"))
                .thenThrow(new ShoppingException(ErrorCode.PROFANITY_CHECK_UNAVAILABLE));

        assertFailure(ErrorCode.PROFANITY_CHECK_UNAVAILABLE);
        assertUnchanged();
    }

    private void assertFailure(ErrorCode expected) {
        assertThatThrownBy(() -> service.update(7L, updated))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(expected));
    }

    private void assertUnchanged() {
        assertThat(product.name()).isEqualTo("기존");
        assertThat(product.price()).isEqualTo(1000);
        assertThat(product.imageUrl()).isEqualTo("https://a.com");
    }
}
