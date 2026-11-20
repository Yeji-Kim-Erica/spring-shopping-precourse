package shopping.wish.web;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shopping.member.application.RegistrationService;
import shopping.member.domain.Email;
import shopping.member.persistence.MemberRepository;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.persistence.ProductRepository;
import shopping.wish.persistence.WishRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class WishApiSupport {

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected JdbcTemplate jdbc;
    @Autowired
    protected WishRepository wishes;
    @Autowired
    protected ProductRepository products;
    @Autowired
    private RegistrationService registration;
    @Autowired
    private MemberRepository members;
    protected String owner;
    protected String other;
    protected Long ownerId;
    protected Long otherId;
    protected Product product;

    @BeforeEach
    void prepare() {
        clearDatabase();
        owner = register("wish-owner@example.com");
        other = register("wish-other@example.com");
        ownerId = memberId("wish-owner@example.com");
        otherId = memberId("wish-other@example.com");
        product = products.saveAndFlush(new Product(
                new ProductDetails("상품", 1000, "https://example.com/a")));
    }

    private void clearDatabase() {
        jdbc.update("delete from wishes");
        jdbc.update("delete from access_tokens");
        jdbc.update("delete from members");
        jdbc.update("delete from products");
    }

    private String register(String email) {
        return "Bearer " + registration.register(email, "hello123").value();
    }

    private Long memberId(String email) {
        return members.findByEmail(new Email(email)).orElseThrow().id();
    }
}
