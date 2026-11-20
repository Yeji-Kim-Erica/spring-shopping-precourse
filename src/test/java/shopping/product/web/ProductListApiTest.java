package shopping.product.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.transaction.annotation.Transactional;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.persistence.ProductRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("상품 목록 조회 API")
class ProductListApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProductRepository products;

    @BeforeEach
    void clear() {
        products.deleteAll();
    }

    @DisplayName("상품 목록을 상품 ID 오름차순으로 반환한다")
    @Test
    void returnsProductsInIdOrder() throws Exception {
        Product first = products.saveAndFlush(product("첫째"));
        Product second = products.saveAndFlush(product("둘째"));

        mvc.perform(get("/api/products")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(first.id()))
                .andExpect(jsonPath("$[1].id").value(second.id()));
    }

    @DisplayName("등록된 상품이 없으면 빈 배열을 반환한다")
    @Test
    void returnsEmptyArrayWhenNoProductsExist() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @DisplayName("지원하지 않는 응답 형식 요청에는 JSON 오류를 반환한다")
    @Test
    void unsupportedResponseFormatUsesJsonError() throws Exception {
        mvc.perform(get("/api/products").accept("text/plain"))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.code").value("NOT_ACCEPTABLE"));
    }

    private Product product(String name) {
        return new Product(new ProductDetails(name, 1000, "https://example.com/a"));
    }
}
