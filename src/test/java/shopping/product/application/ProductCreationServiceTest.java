package shopping.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.domain.ProfanityChecker;
import shopping.product.persistence.ProductRepository;

class ProductCreationServiceTest {

    private final ProductRepository products = mock(ProductRepository.class);
    private final ProfanityChecker checker = mock(ProfanityChecker.class);
    private final ProductCreationService service = new ProductCreationService(products, checker);
    private final ProductDetails details = new ProductDetails("상품", 1000, "https://example.com/a");

    @Test
    void returnsSavedProductAfterCheckingItsName() {
        Product saved = mock(Product.class);
        when(products.save(any(Product.class))).thenReturn(saved);

        assertThat(service.create(details)).isSameAs(saved);
        verify(checker).containsProfanity("상품");
    }

    @Test
    void profanityRejectionDoesNotSave() {
        when(checker.containsProfanity("상품")).thenReturn(true);

        assertThatThrownBy(() -> service.create(details)).isInstanceOf(ShoppingException.class);
        verifyNoInteractions(products);
    }

    @Test
    void unavailableCheckerDoesNotSave() {
        ShoppingException failure = new ShoppingException(ErrorCode.PROFANITY_CHECK_UNAVAILABLE);
        when(checker.containsProfanity("상품")).thenThrow(failure);

        assertThatThrownBy(() -> service.create(details)).isSameAs(failure);
        verifyNoInteractions(products);
    }
}
