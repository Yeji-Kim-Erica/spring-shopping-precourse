package shopping.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.domain.ProfanityChecker;
import shopping.product.persistence.ProductRepository;

@DisplayName("상품 추가 서비스")
class ProductCreationServiceTest {

    private final ProductRepository products = mock(ProductRepository.class);
    private final ProfanityChecker checker = mock(ProfanityChecker.class);
    private final ProductCreationService service = new ProductCreationService(products, checker);
    private final ProductDetails details = new ProductDetails("상품", 1000, "https://example.com/a");

    @DisplayName("상품 이름의 비속어 검사를 통과하면 저장된 상품을 반환한다")
    @Test
    void returnsSavedProductAfterCheckingItsName() {
        Product saved = mock(Product.class);
        when(products.save(any(Product.class))).thenReturn(saved);

        assertThat(service.create(details)).isSameAs(saved);
        verify(checker).containsProfanity("상품");
    }

    @DisplayName("상품 이름에 비속어가 있으면 저장하지 않는다")
    @Test
    void profanityRejectionDoesNotSave() {
        when(checker.containsProfanity("상품")).thenReturn(true);

        assertThatThrownBy(() -> service.create(details)).isInstanceOf(ShoppingException.class);
        verifyNoInteractions(products);
    }

    @DisplayName("비속어 검사에 실패하면 상품을 저장하지 않는다")
    @Test
    void unavailableCheckerDoesNotSave() {
        ShoppingException failure = new ShoppingException(ErrorCode.PROFANITY_CHECK_UNAVAILABLE);
        when(checker.containsProfanity("상품")).thenThrow(failure);

        assertThatThrownBy(() -> service.create(details)).isSameAs(failure);
        verifyNoInteractions(products);
    }
}
