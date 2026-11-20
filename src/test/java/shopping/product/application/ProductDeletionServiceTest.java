package shopping.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.persistence.ProductRepository;

class ProductDeletionServiceTest {

    private final ProductRepository products = mock(ProductRepository.class);
    private final ProductDeletionService service = new ProductDeletionService(products);

    @Test
    void deletesRequestedProduct() {
        Product product = mock(Product.class);
        when(products.findById(7L)).thenReturn(Optional.of(product));

        service.delete(7L);

        verify(products).delete(product);
    }

    @Test
    void rejectsMissingProductWithoutDeletion() {
        when(products.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND));
        verify(products, never()).delete(any(Product.class));
    }
}
