package shopping.product.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.domain.ProductDetails;
import shopping.product.domain.ProfanityChecker;
import shopping.product.persistence.ProductRepository;

@Service
public class ProductUpdateService {

    private final ProductRepository products;
    private final ProfanityChecker checker;

    public ProductUpdateService(ProductRepository products, ProfanityChecker checker) {
        this.products = products;
        this.checker = checker;
    }

    @Transactional
    public Product update(Long id, ProductDetails details) {
        Product product = products.findById(id)
                .orElseThrow(() -> new ShoppingException(ErrorCode.PRODUCT_NOT_FOUND));
        details.checkProfanity(checker);

        product.changeTo(details);
        return product;
    }
}
