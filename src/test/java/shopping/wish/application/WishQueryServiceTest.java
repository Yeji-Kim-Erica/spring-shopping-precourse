package shopping.wish.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shopping.wish.domain.Wish;
import shopping.wish.persistence.WishRepository;

@DisplayName("위시 조회 서비스")
class WishQueryServiceTest {

    @DisplayName("요청한 회원의 위시 목록을 조회한다")
    @Test
    void retrievesWishesForRequestedMember() {
        WishRepository wishes = mock(WishRepository.class);
        WishQueryService service = new WishQueryService(wishes);
        Wish wish = mock(Wish.class);
        when(wishes.findAllWithProducts(7L)).thenReturn(List.of(wish));

        assertThat(service.list(7L)).containsExactly(wish);
    }
}
