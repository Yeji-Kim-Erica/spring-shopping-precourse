package shopping.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30)
    private String name;
    @Column(nullable = false)
    private Integer price;
    @Lob
    @Column(nullable = false)
    private String imageUrl;

    protected Product() {
    }

    public Product(ProductDetails details) {
        apply(details);
    }

    public void changeTo(ProductDetails details) {
        apply(details);
    }

    private void apply(ProductDetails details) {
        this.name = details.name();
        this.price = details.price();
        this.imageUrl = details.imageUrl();
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Integer price() {
        return price;
    }

    public String imageUrl() {
        return imageUrl;
    }
}
