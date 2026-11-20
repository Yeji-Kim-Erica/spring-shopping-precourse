package shopping.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.persistence.ProductRepository;

class ProductQueryServiceTest {

    private final ProductRepository products = mock(ProductRepository.class);
    private final ProductQueryService service = new ProductQueryService(products);

    @Test
    void requestsIdOrderingFromStorage() {
        Product first = mock(Product.class);
        Product second = mock(Product.class);
        when(products.findAll(Sort.by("id"))).thenReturn(List.of(first, second));

        assertThat(service.list()).containsExactly(first, second);
    }

    @Test
    void returnsRequestedProduct() {
        Product found = mock(Product.class);
        when(products.findById(7L)).thenReturn(Optional.of(found));

        assertThat(service.find(7L)).isSameAs(found);
    }

    @Test
    void missingProductIsNotFound() {
        when(products.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(7L))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
