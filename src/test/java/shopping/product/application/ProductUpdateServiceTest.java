package shopping.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.domain.ProfanityChecker;
import shopping.product.persistence.ProductRepository;

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

    @Test
    void changesLoadedProductAfterValidation() {
        assertThat(service.update(7L, updated)).isSameAs(product);
        assertThat(product.name()).isEqualTo("수정");
    }

    @Test
    void missingProductDoesNotCallExternalChecker() {
        when(products.findById(7L)).thenReturn(Optional.empty());

        assertFailure(ErrorCode.PRODUCT_NOT_FOUND);
        verifyNoInteractions(checker);
    }

    @Test
    void profanityLeavesOriginalDetailsUntouched() {
        when(checker.containsProfanity("수정")).thenReturn(true);

        assertFailure(ErrorCode.PROFANITY_DETECTED);
        assertUnchanged();
    }

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
