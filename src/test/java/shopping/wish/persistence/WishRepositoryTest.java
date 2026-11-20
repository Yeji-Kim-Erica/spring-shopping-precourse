package shopping.wish.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shopping.member.domain.Email;
import shopping.member.domain.Member;
import shopping.member.domain.Password;
import shopping.member.persistence.MemberRepository;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.persistence.ProductRepository;
import shopping.wish.domain.Wish;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WishRepositoryTest {

    @Autowired
    private WishRepository wishes;
    @Autowired
    private MemberRepository members;
    @Autowired
    private ProductRepository products;
    @Autowired
    private EntityManager entityManager;
    private Long owner;
    private Long other;
    private Product product;

    @BeforeEach
    void prepare() {
        wishes.deleteAll();
        owner = member("repo-owner@example.com").id();
        other = member("repo-other@example.com").id();
        product = products.saveAndFlush(product("상품"));
    }

    @Test
    void filtersByMemberAndLoadsOrderedProductsOutsidePersistenceContext() {
        Wish first = wishes.saveAndFlush(new Wish(owner, product));
        wishes.saveAndFlush(new Wish(other, product));
        Product next = products.saveAndFlush(product("다음상품"));
        Wish second = wishes.saveAndFlush(new Wish(owner, next));
        entityManager.clear();

        List<Wish> found = wishes.findAllWithProducts(owner);
        entityManager.clear();

        assertThat(found).extracting(Wish::id).containsExactly(first.id(), second.id());
        assertThat(found).extracting(wish -> wish.product().name())
                .containsExactly("상품", "다음상품");
    }

    @Test
    void sameProductCanBeStoredForDifferentMembers() {
        wishes.saveAndFlush(new Wish(owner, product));
        wishes.saveAndFlush(new Wish(other, product));

        assertThat(wishes.existsByMemberIdAndProduct_Id(owner, product.id())).isTrue();
        assertThat(wishes.existsByMemberIdAndProduct_Id(other, product.id())).isTrue();
    }

    @Test
    void databaseRejectsDuplicateMemberProductPair() {
        wishes.saveAndFlush(new Wish(owner, product));

        assertThatThrownBy(() -> wishes.saveAndFlush(new Wish(owner, product)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void productDeletionRemovesOnlyRelatedWishesAcrossMembers() {
        wishes.saveAndFlush(new Wish(owner, product));
        wishes.saveAndFlush(new Wish(other, product));
        Product unrelated = products.saveAndFlush(product("다른상품"));
        Wish retained = wishes.saveAndFlush(new Wish(owner, unrelated));
        entityManager.clear();

        products.delete(product);
        products.flush();
        entityManager.clear();

        assertThat(wishes.findAll()).extracting(Wish::id).containsExactly(retained.id());
        assertThat(products.findById(product.id())).isEmpty();
    }

    private Member member(String email) {
        return members.saveAndFlush(new Member(new Email(email), new Password("hello123")));
    }

    private Product product(String name) {
        return new Product(new ProductDetails(name, 1000, "https://example.com/a"));
    }
}
