package shopping.product.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
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
class ProductDeletionApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProductRepository products;

    @BeforeEach
    void clear() {
        products.deleteAll();
    }

    @Test
    void removesOnlyRequestedProductAndReturnsNoContent() throws Exception {
        Product target = products.saveAndFlush(product("대상"));
        Product other = products.saveAndFlush(product("유지"));

        mvc.perform(delete("/api/products/{id}", target.id()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));

        assertThat(products.findAll()).extracting(Product::id).containsExactly(other.id());
    }

    @Test
    void missingProductReturnsNotFound() throws Exception {
        mvc.perform(delete("/api/products/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    private Product product(String name) {
        return new Product(new ProductDetails(name, 1000, "https://example.com/a"));
    }
}
