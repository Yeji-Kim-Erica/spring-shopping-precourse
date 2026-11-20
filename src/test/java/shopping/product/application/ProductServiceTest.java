package shopping.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.domain.ProfanityChecker;
import shopping.product.persistence.ProductRepository;

@DisplayName("상품 서비스")
class ProductServiceTest {

    private final ProductRepository products = mock(ProductRepository.class);
    private final ProfanityChecker checker = mock(ProfanityChecker.class);
    private final ProductService service = new ProductService(products, checker);
    private final ProductDetails details = new ProductDetails("상품", 1000, "https://example.com/a");
    private final Product product = new Product(new ProductDetails("기존", 1000, "https://a.com"));
    private final ProductDetails updated = new ProductDetails("수정", 2000, "https://b.com");

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

    @DisplayName("검증을 통과하면 조회한 상품의 정보를 변경한다")
    @Test
    void changesLoadedProductAfterValidation() {
        when(products.findById(7L)).thenReturn(Optional.of(product));

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
        when(products.findById(7L)).thenReturn(Optional.of(product));
        when(checker.containsProfanity("수정")).thenReturn(true);

        assertFailure(ErrorCode.PROFANITY_DETECTED);
        assertUnchanged();
    }

    @DisplayName("수정 시 비속어 검사에 실패하면 기존 상품 정보를 유지한다")
    @Test
    void unavailableCheckLeavesOriginalDetailsUntouched() {
        when(products.findById(7L)).thenReturn(Optional.of(product));
        when(checker.containsProfanity("수정"))
                .thenThrow(new ShoppingException(ErrorCode.PROFANITY_CHECK_UNAVAILABLE));

        assertFailure(ErrorCode.PROFANITY_CHECK_UNAVAILABLE);
        assertUnchanged();
    }

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
