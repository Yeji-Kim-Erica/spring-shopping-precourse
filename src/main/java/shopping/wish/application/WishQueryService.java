package shopping.wish.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.wish.domain.Wish;
import shopping.wish.persistence.WishRepository;

@Service
public class WishQueryService {

    private final WishRepository wishes;

    public WishQueryService(WishRepository wishes) {
        this.wishes = wishes;
    }

    @Transactional(readOnly = true)
    public List<Wish> list(Long memberId) {
        return wishes.findAllWithProducts(memberId);
    }
}
