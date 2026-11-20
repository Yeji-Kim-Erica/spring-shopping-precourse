package shopping.wish.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;

class WishTest {

    @Test
    void onlyOwnerCanDeleteWish() {
        Product product = new Product(new ProductDetails("상품", 1000, "https://a.com"));
        Wish wish = new Wish(7L, product);

        assertThatCode(() -> wish.requireOwner(7L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> wish.requireOwner(8L))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void requiresMemberAndProductToCreateWish() {
        Product product = new Product(new ProductDetails("상품", 1000, "https://a.com"));

        assertThatCode(() -> new Wish(7L, product)).doesNotThrowAnyException();
        assertThatIllegalArgumentException().isThrownBy(() -> new Wish(null, product));
        assertThatIllegalArgumentException().isThrownBy(() -> new Wish(7L, null));
    }
}
