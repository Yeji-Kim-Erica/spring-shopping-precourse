package shopping.wish.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;
import shopping.wish.domain.Wish;
import shopping.wish.persistence.WishRepository;

@DisplayName("위시 삭제 서비스")
class WishDeletionServiceTest {

    private final WishRepository wishes = mock(WishRepository.class);
    private final WishDeletionService service = new WishDeletionService(wishes);

    @DisplayName("위시 도메인의 소유자 확인을 거친 뒤 삭제한다")
    @Test
    void deletesAfterDomainOwnershipCheck() {
        Wish wish = mock(Wish.class);
        when(wishes.findById(9L)).thenReturn(Optional.of(wish));

        service.delete(7L, 9L);

        verify(wish).requireOwner(7L);
        verify(wishes).delete(wish);
    }

    @DisplayName("소유자 확인에 실패하면 위시 항목을 삭제하지 않는다")
    @Test
    void domainRejectionPreventsDeletion() {
        Wish wish = mock(Wish.class);
        when(wishes.findById(9L)).thenReturn(Optional.of(wish));
        doThrow(new ShoppingException(ErrorCode.FORBIDDEN)).when(wish).requireOwner(7L);

        assertFailure(ErrorCode.FORBIDDEN);
        verify(wishes, never()).delete(any(Wish.class));
    }

    @DisplayName("위시 항목이 없으면 오류를 반환하고 삭제하지 않는다")
    @Test
    void missingWishPreventsDeletion() {
        when(wishes.findById(9L)).thenReturn(Optional.empty());

        assertFailure(ErrorCode.WISH_NOT_FOUND);
        verify(wishes, never()).delete(any(Wish.class));
    }

    private void assertFailure(ErrorCode code) {
        assertThatThrownBy(() -> service.delete(7L, 9L))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code()).isEqualTo(code));
    }
}
