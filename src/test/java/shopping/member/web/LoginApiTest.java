package shopping.member.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shopping.auth.application.AuthenticationService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private AuthenticationService authentication;

    @BeforeEach
    void clear() {
        jdbc.update("delete from access_tokens");
        jdbc.update("delete from members");
    }

    @Test
    void loginIssuesNewTokenAndKeepsPreviousTokenValid() throws Exception {
        String original = token(request("register", "yeji@example.com", "hello123"));
        String renewed = token(request("login", " yeji@example.com ", "hello123")
                .andExpect(status().isOk()));

        assertThat(renewed).isNotEqualTo(original);
        assertThat(authentication.authenticate(renewed))
                .isEqualTo(authentication.authenticate(original));
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing@example.com", "yeji@example.com"})
    void missingEmailAndWrongPasswordReturnSameError(String email) throws Exception {
        request("register", "yeji@example.com", "hello123");

        request("login", email, "wrong123").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void malformedCredentialsAreInputError() throws Exception {
        request("login", "bad", "hello123").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private ResultActions request(String action, String email, String password) throws Exception {
        String body = "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
        return mvc.perform(post("/api/members/" + action).contentType("application/json")
                .content(body));
    }

    private String token(ResultActions response) throws Exception {
        return JsonPath.read(response.andReturn().getResponse().getContentAsString(), "$.token");
    }
}
