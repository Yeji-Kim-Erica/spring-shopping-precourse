package shopping.product.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class ProductQueryApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProductRepository products;

    @Test
    void readsProductWithoutAuthentication() throws Exception {
        Product saved = products.saveAndFlush(
                new Product(new ProductDetails("상품", 1000, "https://example.com/a")));

        mvc.perform(get("/api/products/{id}", saved.id())).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.id()))
                .andExpect(jsonPath("$.name").value("상품"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/a"));
    }

    @Test
    void missingProductReturnsNotFound() throws Exception {
        mvc.perform(get("/api/products/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void malformedIdReturnsInputError() throws Exception {
        mvc.perform(get("/api/products/not-a-number")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
