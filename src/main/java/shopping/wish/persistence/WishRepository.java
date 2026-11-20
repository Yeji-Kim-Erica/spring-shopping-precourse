package shopping.wish.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import shopping.wish.domain.Wish;

public interface WishRepository extends JpaRepository<Wish, Long> {

    boolean existsByMemberIdAndProduct_Id(Long memberId, Long productId);
}
