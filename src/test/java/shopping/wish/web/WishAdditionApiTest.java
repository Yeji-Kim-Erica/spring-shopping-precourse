package shopping.wish.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("위시 추가 API")
class WishAdditionApiTest extends WishApiSupport {

    @DisplayName("인증된 회원의 위시에 상품을 추가한다")
    @Test
    void addsProductForAuthenticatedMember() throws Exception {
        add(owner).andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.product.id").value(product.id()));

        assertThat(wishes.existsByMemberIdAndProduct_Id(ownerId, product.id())).isTrue();
        assertThat(wishes.existsByMemberIdAndProduct_Id(otherId, product.id())).isFalse();
    }

    @DisplayName("위시 중복 추가 여부는 인증된 회원을 기준으로 판단한다")
    @Test
    void duplicateCheckIsScopedToAuthenticatedMember() throws Exception {
        add(owner).andExpect(status().isCreated());

        add(owner).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WISH_ALREADY_EXISTS"));
        add(other).andExpect(status().isCreated());
    }

    @DisplayName("존재하지 않는 상품을 요청하면 상품 없음 오류를 반환한다")
    @Test
    void missingProductReturnsNotFound() throws Exception {
        request(owner, "{\"productId\":999999}").andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @DisplayName("위시 추가 시 상품 ID 누락이나 회원 직접 지정은 거절한다")
    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"productId\":1,\"memberId\":999}"})
    void rejectsMissingProductIdOrClientSuppliedMember(String body) throws Exception {
        request(owner, body).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @DisplayName("위시 추가에는 유효한 인증 토큰이 필요하다")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"Basic invalid", "Bearer unknown"})
    void requiresValidToken(String authorization) throws Exception {
        request(authorization, "{\"productId\":" + product.id() + "}")
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));

        assertThat(wishes.count()).isZero();
    }

    @DisplayName("만료된 토큰으로 위시에 상품을 추가할 수 없다")
    @Test
    void expiredTokenCannotAddWish() throws Exception {
        jdbc.update("update access_tokens set expires_at = "
                + "timestamp with time zone '2000-01-01 00:00:00Z'");

        add(owner).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
        assertThat(wishes.count()).isZero();
    }

    private ResultActions add(String authorization) throws Exception {
        return request(authorization, "{\"productId\":" + product.id() + "}");
    }

    private ResultActions request(String authorization, String body) throws Exception {
        var request = post("/api/wishes").contentType("application/json").content(body);
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        return mvc.perform(request);
    }
}
