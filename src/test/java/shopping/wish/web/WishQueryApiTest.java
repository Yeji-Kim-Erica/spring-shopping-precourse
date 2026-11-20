package shopping.wish.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.wish.domain.Wish;

class WishQueryApiTest extends WishApiSupport {

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

    @Test
    void returnsEmptyArrayWhenOnlyOtherMemberHasWishes() throws Exception {
        wishes.saveAndFlush(new Wish(otherId, product));

        mvc.perform(get("/api/wishes").header("Authorization", owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

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

    @Test
    void rejectsMemberSelectionByQueryParameter() throws Exception {
        mvc.perform(get("/api/wishes").param("memberId", otherId.toString())
                .header("Authorization", owner)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/wishes")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }
}
