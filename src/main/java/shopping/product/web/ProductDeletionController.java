package shopping.product.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shopping.product.application.ProductDeletionService;

@RestController
public class ProductDeletionController {

    private final ProductDeletionService deletion;

    public ProductDeletionController(ProductDeletionService deletion) {
        this.deletion = deletion;
    }

    @DeleteMapping("/api/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long productId) {
        deletion.delete(productId);
    }
}
