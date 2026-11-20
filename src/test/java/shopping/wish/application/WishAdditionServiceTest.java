package shopping.wish.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.persistence.ProductRepository;
import shopping.wish.domain.Wish;
import shopping.wish.persistence.WishRepository;

class WishAdditionServiceTest {

    private final WishRepository wishes = mock(WishRepository.class);
    private final ProductRepository products = mock(ProductRepository.class);
    private final WishAdditionService service = new WishAdditionService(wishes, products);

    @Test
    void addsExistingProductAfterMemberScopedDuplicateCheck() {
        when(products.findById(9L)).thenReturn(Optional.of(mock(Product.class)));
        Wish saved = mock(Wish.class);
        when(wishes.saveAndFlush(any(Wish.class))).thenReturn(saved);

        assertThat(service.add(7L, 9L)).isSameAs(saved);
        verify(wishes).existsByMemberIdAndProduct_Id(7L, 9L);
    }

    @Test
    void missingProductDoesNotAccessWishlist() {
        when(products.findById(9L)).thenReturn(Optional.empty());

        assertFailure(ErrorCode.PRODUCT_NOT_FOUND);
        verifyNoInteractions(wishes);
    }

    @Test
    void duplicateWishDoesNotSave() {
        when(products.findById(9L)).thenReturn(Optional.of(mock(Product.class)));
        when(wishes.existsByMemberIdAndProduct_Id(7L, 9L)).thenReturn(true);

        assertFailure(ErrorCode.WISH_ALREADY_EXISTS);
        verify(wishes, never()).saveAndFlush(any(Wish.class));
    }

    private void assertFailure(ErrorCode code) {
        assertThatThrownBy(() -> service.add(7L, 9L))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(code));
    }
}
