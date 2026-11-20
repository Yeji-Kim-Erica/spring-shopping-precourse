package shopping.product.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.domain.ProfanityChecker;
import shopping.product.persistence.ProductRepository;

@Service
public class ProductCreationService {

    private final ProductRepository products;
    private final ProfanityChecker checker;

    public ProductCreationService(ProductRepository products, ProfanityChecker checker) {
        this.products = products;
        this.checker = checker;
    }

    @Transactional
    public Product create(ProductDetails details) {
        details.checkProfanity(checker);
        return products.save(new Product(details));
    }
}
