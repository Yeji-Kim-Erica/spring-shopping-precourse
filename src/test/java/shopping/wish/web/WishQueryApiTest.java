package shopping.wish.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.wish.domain.Wish;

@DisplayName("위시 조회 API")
class WishQueryApiTest extends WishApiSupport {

    @DisplayName("본인의 위시만 ID 오름차순으로 반환한다")
    @Test
    void returnsOnlyOwnWishesInIdOrder() throws Exception {
        Wish first = wishes.saveAndFlush(new Wish(ownerId, product));
        wishes.saveAndFlush(new Wish(otherId, product));
        Product next = products.saveAndFlush(
                new Product(new ProductDetails("다음상품", 2000, "https://example.com/b")));
        Wish second = wishes.saveAndFlush(new Wish(ownerId, next));

        mvc.perform(get("/api/wishes").header("Authorization", owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(first.id()))
                .andExpect(jsonPath("$[1].id").value(second.id()));
    }

    @DisplayName("다른 회원의 위시만 있으면 빈 배열을 반환한다")
    @Test
    void returnsEmptyArrayWhenOnlyOtherMemberHasWishes() throws Exception {
        wishes.saveAndFlush(new Wish(otherId, product));

        mvc.perform(get("/api/wishes").header("Authorization", owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @DisplayName("위시 조회 시 상품의 현재 정보를 반환한다")
    @Test
    void returnsCurrentProductDetails() throws Exception {
        wishes.saveAndFlush(new Wish(ownerId, product));
        product.changeTo(new ProductDetails("현재상품", 2000, "https://example.com/new"));
        products.saveAndFlush(product);

        mvc.perform(get("/api/wishes").header("Authorization", owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].product.name").value("현재상품"))
                .andExpect(jsonPath("$[0].product.price").value(2000))
                .andExpect(jsonPath("$[0].product.imageUrl").value("https://example.com/new"));
    }

    @DisplayName("위시 조회 시 쿼리 파라미터로 회원을 지정할 수 없다")
    @Test
    void rejectsMemberSelectionByQueryParameter() throws Exception {
        mvc.perform(get("/api/wishes").param("memberId", otherId.toString())
                .header("Authorization", owner)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @DisplayName("위시 조회에는 토큰 인증이 필요하다")
    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/wishes")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }
}
