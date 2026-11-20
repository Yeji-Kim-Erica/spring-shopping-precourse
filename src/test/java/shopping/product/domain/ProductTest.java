package shopping.product.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void replacesAllDetailsTogether() {
        Product product = new Product(new ProductDetails("기존상품", 1000, "https://example.com/a"));
        ProductDetails updated = new ProductDetails("새상품", 2000, "https://example.com/b");

        product.changeTo(updated);

        assertThat(product.name()).isEqualTo("새상품");
        assertThat(product.price()).isEqualTo(2000);
        assertThat(product.imageUrl()).isEqualTo("https://example.com/b");
    }
}
