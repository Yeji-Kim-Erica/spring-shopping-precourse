package shopping.wish.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.wish.domain.Wish;

@DisplayName("위시 삭제 API")
class WishDeletionApiTest extends WishApiSupport {

    @DisplayName("본인의 위시 중 요청한 항목만 삭제한다")
    @Test
    void deletesOnlyRequestedOwnWish() throws Exception {
        Wish target = wishes.saveAndFlush(new Wish(ownerId, product));
        Wish others = wishes.saveAndFlush(new Wish(otherId, product));
        Product next = products.saveAndFlush(
                new Product(new ProductDetails("다음상품", 2000, "https://example.com/b")));
        Wish retained = wishes.saveAndFlush(new Wish(ownerId, next));

        mvc.perform(delete("/api/wishes/{id}", target.id()).header("Authorization", owner))
                .andExpect(status().isNoContent()).andExpect(content().string(""));

        assertThat(wishes.findAll()).extracting(Wish::id)
                .containsExactlyInAnyOrder(others.id(), retained.id());
    }

    @DisplayName("다른 회원의 위시 삭제 요청은 권한 오류로 응답한다")
    @Test
    void cannotDeleteAnotherMembersWish() throws Exception {
        Wish target = wishes.saveAndFlush(new Wish(ownerId, product));

        mvc.perform(delete("/api/wishes/{id}", target.id()).header("Authorization", other))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));

        assertThat(wishes.existsById(target.id())).isTrue();
    }

    @DisplayName("존재하지 않는 위시 삭제 요청은 위시 없음 오류로 응답한다")
    @Test
    void missingWishReturnsNotFound() throws Exception {
        mvc.perform(delete("/api/wishes/999999").header("Authorization", owner))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WISH_NOT_FOUND"));
    }

    @DisplayName("인증되지 않은 위시 삭제 요청은 거절하고 항목을 유지한다")
    @Test
    void requiresAuthenticationBeforeDeleting() throws Exception {
        Wish target = wishes.saveAndFlush(new Wish(ownerId, product));

        mvc.perform(delete("/api/wishes/{id}", target.id())).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));

        assertThat(wishes.existsById(target.id())).isTrue();
    }
}
