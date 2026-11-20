package shopping.product.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.persistence.ProductRepository;

@Service
public class ProductDeletionService {

    private final ProductRepository products;

    public ProductDeletionService(ProductRepository products) {
        this.products = products;
    }

    @Transactional
    public void delete(Long id) {
        Product product = products.findById(id)
                .orElseThrow(() -> new ShoppingException(ErrorCode.PRODUCT_NOT_FOUND));

        products.delete(product);
    }
}
