package shopping.wish.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.wish.domain.Wish;
import shopping.wish.persistence.WishRepository;

@Service
public class WishDeletionService {

    private final WishRepository wishes;

    public WishDeletionService(WishRepository wishes) {
        this.wishes = wishes;
    }

    @Transactional
    public void delete(Long memberId, Long wishId) {
        Wish wish = wishes.findById(wishId)
                .orElseThrow(() -> new ShoppingException(ErrorCode.WISH_NOT_FOUND));
        wish.requireOwner(memberId);

        wishes.delete(wish);
    }
}
