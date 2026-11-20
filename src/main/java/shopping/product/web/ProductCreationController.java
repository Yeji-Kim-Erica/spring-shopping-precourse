package shopping.product.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shopping.product.application.ProductCreationService;

@RestController
public class ProductCreationController {

    private final ProductCreationService creation;

    public ProductCreationController(ProductCreationService creation) {
        this.creation = creation;
    }

    @PostMapping("/api/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@RequestBody ProductRequest request) {
        return ProductResponse.of(creation.create(request.details()));
    }
}
