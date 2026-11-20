package shopping.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.persistence.ProductRepository;

@DisplayName("상품 조회 서비스")
class ProductQueryServiceTest {

    private final ProductRepository products = mock(ProductRepository.class);
    private final ProductQueryService service = new ProductQueryService(products);

    @DisplayName("상품 목록을 ID 오름차순으로 조회한다")
    @Test
    void requestsIdOrderingFromStorage() {
        Product first = mock(Product.class);
        Product second = mock(Product.class);
        when(products.findAll(Sort.by("id"))).thenReturn(List.of(first, second));

        assertThat(service.list()).containsExactly(first, second);
    }

    @DisplayName("요청한 ID의 상품을 조회한다")
    @Test
    void returnsRequestedProduct() {
        Product found = mock(Product.class);
        when(products.findById(7L)).thenReturn(Optional.of(found));

        assertThat(service.find(7L)).isSameAs(found);
    }

    @DisplayName("조회할 상품이 없으면 상품 없음 오류를 반환한다")
    @Test
    void missingProductIsNotFound() {
        when(products.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(7L))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
