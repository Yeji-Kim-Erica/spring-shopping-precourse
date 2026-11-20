package shopping.product.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.persistence.ProductRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("상품 삭제 API")
class ProductDeletionApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProductRepository products;

    @BeforeEach
    void clear() {
        products.deleteAll();
    }

    @DisplayName("요청한 상품만 삭제하고 본문 없는 성공 응답을 반환한다")
    @Test
    void removesOnlyRequestedProductAndReturnsNoContent() throws Exception {
        Product target = products.saveAndFlush(product("대상"));
        Product other = products.saveAndFlush(product("유지"));

        mvc.perform(delete("/api/products/{id}", target.id()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));

        assertThat(products.findAll()).extracting(Product::id).containsExactly(other.id());
    }

    @DisplayName("존재하지 않는 상품을 요청하면 상품 없음 오류를 반환한다")
    @Test
    void missingProductReturnsNotFound() throws Exception {
        mvc.perform(delete("/api/products/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    private Product product(String name) {
        return new Product(new ProductDetails(name, 1000, "https://example.com/a"));
    }
}
