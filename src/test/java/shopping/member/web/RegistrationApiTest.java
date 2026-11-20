package shopping.member.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shopping.auth.application.TokenGenerator;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationApiTest {

    private static final String BODY =
            "{\"email\":\" yeji@example.com \",\"password\":\"hello123\"}";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @MockitoSpyBean
    private TokenGenerator generator;

    @BeforeEach
    void clear() {
        jdbc.update("delete from wishes");
        jdbc.update("delete from access_tokens");
        jdbc.update("delete from members");
    }

    @Test
    void returnsIssuedTokenAndStoresNormalizedMember() throws Exception {
        register(BODY).andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());

        assertThat(jdbc.queryForObject("select email from members", String.class))
                .isEqualTo("yeji@example.com");
        assertThat(jdbc.queryForObject("select count(*) from access_tokens", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        register(BODY).andExpect(status().isCreated());

        register(BODY).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{}", "{\"email\":\"bad\",\"password\":\"hello123\"}",
            "{\"email\":\"yeji@example.com\",\"password\":12345}",
            "{\"email\":\"yeji@example.com\",\"password\":\"hello123\",\"id\":1}"})
    void invalidInputReturnsCommonError(String body) throws Exception {
        register(body).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void issuingFailureRollsBackMemberStorage() throws Exception {
        doThrow(new IllegalStateException("issuance failed")).when(generator).generate();

        register(BODY).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));

        assertThat(jdbc.queryForObject("select count(*) from members", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from access_tokens", Integer.class))
                .isZero();
    }

    private ResultActions register(String body) throws Exception {
        return mvc.perform(post("/api/members/register").contentType("application/json")
                .content(body));
    }
}
