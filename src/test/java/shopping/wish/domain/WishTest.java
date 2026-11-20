package shopping.wish.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;

class WishTest {

    @Test
    void requiresMemberAndProductToCreateWish() {
        Product product = new Product(new ProductDetails("상품", 1000, "https://a.com"));

        assertThatCode(() -> new Wish(7L, product)).doesNotThrowAnyException();
        assertThatIllegalArgumentException().isThrownBy(() -> new Wish(null, product));
        assertThatIllegalArgumentException().isThrownBy(() -> new Wish(7L, null));
    }
}
