package shopping.product.web;

import shopping.product.domain.ProductDetails;

public record ProductRequest(String name, Integer price, String imageUrl) {

    public ProductDetails details() {
        return new ProductDetails(name, price, imageUrl);
    }
}
