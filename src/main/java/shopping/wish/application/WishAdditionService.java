package shopping.wish.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.product.domain.Product;
import shopping.product.persistence.ProductRepository;
import shopping.wish.domain.Wish;
import shopping.wish.persistence.WishRepository;

@Service
public class WishAdditionService {

    private final WishRepository wishes;
    private final ProductRepository products;

    public WishAdditionService(WishRepository wishes, ProductRepository products) {
        this.wishes = wishes;
        this.products = products;
    }

    @Transactional
    public Wish add(Long memberId, Long productId) {
        Product product = products.findById(productId)
                .orElseThrow(() -> new ShoppingException(ErrorCode.PRODUCT_NOT_FOUND));
        rejectDuplicate(memberId, productId);

        return wishes.saveAndFlush(new Wish(memberId, product));
    }

    private void rejectDuplicate(Long memberId, Long productId) {
        if (wishes.existsByMemberIdAndProduct_Id(memberId, productId)) {
            throw new ShoppingException(ErrorCode.WISH_ALREADY_EXISTS);
        }
    }
}
