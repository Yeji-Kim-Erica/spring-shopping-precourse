package shopping.product.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("상품")
class ProductTest {

    @DisplayName("상품 수정 시 이름과 가격과 이미지 URL을 함께 변경한다")
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
