package shopping.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.persistence.ProductRepository;

@DisplayName("상품 삭제 서비스")
class ProductDeletionServiceTest {

    private final ProductRepository products = mock(ProductRepository.class);
    private final ProductDeletionService service = new ProductDeletionService(products);

    @DisplayName("삭제 요청한 상품을 조회해 삭제한다")
    @Test
    void deletesRequestedProduct() {
        Product product = mock(Product.class);
        when(products.findById(7L)).thenReturn(Optional.of(product));

        service.delete(7L);

        verify(products).delete(product);
    }

    @DisplayName("삭제할 상품이 없으면 오류를 반환하고 삭제하지 않는다")
    @Test
    void rejectsMissingProductWithoutDeletion() {
        when(products.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND));
        verify(products, never()).delete(any(Product.class));
    }
}
