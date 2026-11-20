package shopping.product.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.ProfanityChecker;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductCreationApiTest {

    private static final String BODY = "{\"name\":\"상품\",\"price\":1000,"
            + "\"imageUrl\":\"https://example.com/image.png\"}";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @MockitoBean
    private ProfanityChecker checker;

    @BeforeEach
    void clear() {
        jdbc.update("delete from products");
    }

    @Test
    void storesProductAndReturnsCreatedDetails() throws Exception {
        create(BODY).andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("상품"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/image.png"));

        assertThat(jdbc.queryForObject("select count(*) from products", Integer.class))
                .isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1.5", "2147483648", "\"1000\"", "null"})
    void invalidPriceDoesNotStoreProduct(String price) throws Exception {
        create(BODY.replace("1000", price)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        assertEmpty();
    }

    @Test
    void profanityIsInputFailureWithoutStorage() throws Exception {
        when(checker.containsProfanity(anyString())).thenReturn(true);

        create(BODY).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROFANITY_DETECTED"));

        assertEmpty();
    }

    @Test
    void checkerFailureReturnsServiceUnavailableWithoutStorage() throws Exception {
        when(checker.containsProfanity(anyString()))
                .thenThrow(new ShoppingException(ErrorCode.PROFANITY_CHECK_UNAVAILABLE));

        create(BODY).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PROFANITY_CHECK_UNAVAILABLE"));

        assertEmpty();
    }

    private void assertEmpty() {
        assertThat(jdbc.queryForObject("select count(*) from products", Integer.class)).isZero();
    }

    private ResultActions create(String body) throws Exception {
        return mvc.perform(post("/api/products").contentType("application/json").content(body));
    }
}
