package shopping.product.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shopping.product.application.ProductUpdateService;

@RestController
public class ProductUpdateController {

    private final ProductUpdateService update;

    public ProductUpdateController(ProductUpdateService update) {
        this.update = update;
    }

    @PutMapping("/api/products/{productId}")
    public ProductResponse update(@PathVariable Long productId,
            @RequestBody ProductRequest request) {
        return ProductResponse.of(update.update(productId, request.details()));
    }
}
