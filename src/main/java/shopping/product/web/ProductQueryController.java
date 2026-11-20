package shopping.product.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import shopping.product.application.ProductQueryService;

@RestController
public class ProductQueryController {

    private final ProductQueryService query;

    public ProductQueryController(ProductQueryService query) {
        this.query = query;
    }

    @GetMapping("/api/products/{productId}")
    public ProductResponse find(@PathVariable Long productId) {
        return ProductResponse.of(query.find(productId));
    }
}
