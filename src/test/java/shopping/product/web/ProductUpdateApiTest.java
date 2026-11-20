package shopping.product.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.domain.ProfanityChecker;
import shopping.product.persistence.ProductRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductUpdateApiTest {

    private static final String BODY =
            "{\"name\":\"변경\",\"price\":2000,\"imageUrl\":\"https://b.com\"}";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProductRepository products;
    @MockitoBean
    private ProfanityChecker checker;
    private Long id;

    @BeforeEach
    void prepare() {
        products.deleteAll();
        id = products.saveAndFlush(new Product(new ProductDetails("기존", 1000, "https://a.com")))
                .id();
    }

    @Test
    void updatesAndPersistsAllFields() throws Exception {
        update(id, BODY).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("변경"))
                .andExpect(jsonPath("$.price").value(2000))
                .andExpect(jsonPath("$.imageUrl").value("https://b.com"));

        Product stored = products.findById(id).orElseThrow();
        assertThat(stored.name()).isEqualTo("변경");
        assertThat(stored.price()).isEqualTo(2000);
        assertThat(stored.imageUrl()).isEqualTo("https://b.com");
    }

    @Test
    void missingFieldsLeaveStoredProductUnchanged() throws Exception {
        update(id, "{}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        assertUnchanged();
    }

    @Test
    void profanityLeavesStoredProductUnchanged() throws Exception {
        when(checker.containsProfanity(anyString())).thenReturn(true);

        update(id, BODY).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROFANITY_DETECTED"));

        assertUnchanged();
    }

    @Test
    void unavailableCheckLeavesStoredProductUnchanged() throws Exception {
        when(checker.containsProfanity(anyString()))
                .thenThrow(new ShoppingException(ErrorCode.PROFANITY_CHECK_UNAVAILABLE));

        update(id, BODY).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PROFANITY_CHECK_UNAVAILABLE"));

        assertUnchanged();
    }

    @Test
    void missingProductReturnsNotFound() throws Exception {
        update(999999L, BODY).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    private void assertUnchanged() {
        Product stored = products.findById(id).orElseThrow();
        assertThat(stored.name()).isEqualTo("기존");
        assertThat(stored.price()).isEqualTo(1000);
        assertThat(stored.imageUrl()).isEqualTo("https://a.com");
    }

    private ResultActions update(Long id, String body) throws Exception {
        return mvc.perform(put("/api/products/{id}", id)
                .contentType("application/json").content(body));
    }
}
