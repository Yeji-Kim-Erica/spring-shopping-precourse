package shopping.product.application;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.persistence.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository products;

    public ProductQueryService(ProductRepository products) {
        this.products = products;
    }

    public List<Product> list() {
        return products.findAll(Sort.by("id"));
    }

    public Product find(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ShoppingException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
