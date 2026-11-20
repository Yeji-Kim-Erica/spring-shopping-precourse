package shopping.wish.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shopping.auth.web.CurrentMember;
import shopping.wish.application.WishDeletionService;

@RestController
public class WishDeletionController {

    private final WishDeletionService deletion;
    private final CurrentMember members;

    public WishDeletionController(WishDeletionService deletion, CurrentMember members) {
        this.deletion = deletion;
        this.members = members;
    }

    @DeleteMapping("/api/wishes/{wishId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(value = "Authorization", required = false) String header,
            @PathVariable Long wishId) {
        Long memberId = members.resolve(header);
        deletion.delete(memberId, wishId);
    }
}
