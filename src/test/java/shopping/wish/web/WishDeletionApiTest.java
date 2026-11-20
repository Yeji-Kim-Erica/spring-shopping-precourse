package shopping.wish.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.wish.domain.Wish;

class WishDeletionApiTest extends WishApiSupport {

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

    @Test
    void cannotDeleteAnotherMembersWish() throws Exception {
        Wish target = wishes.saveAndFlush(new Wish(ownerId, product));

        mvc.perform(delete("/api/wishes/{id}", target.id()).header("Authorization", other))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));

        assertThat(wishes.existsById(target.id())).isTrue();
    }

    @Test
    void missingWishReturnsNotFound() throws Exception {
        mvc.perform(delete("/api/wishes/999999").header("Authorization", owner))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WISH_NOT_FOUND"));
    }

    @Test
    void requiresAuthenticationBeforeDeleting() throws Exception {
        Wish target = wishes.saveAndFlush(new Wish(ownerId, product));

        mvc.perform(delete("/api/wishes/{id}", target.id())).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));

        assertThat(wishes.existsById(target.id())).isTrue();
    }
}
