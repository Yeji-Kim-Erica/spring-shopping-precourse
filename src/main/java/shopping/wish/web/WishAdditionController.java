package shopping.wish.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shopping.auth.web.CurrentMember;
import shopping.wish.application.WishAdditionService;

@RestController
public class WishAdditionController {

    private final WishAdditionService addition;
    private final CurrentMember members;

    public WishAdditionController(WishAdditionService addition, CurrentMember members) {
        this.addition = addition;
        this.members = members;
    }

    @PostMapping("/api/wishes")
    @ResponseStatus(HttpStatus.CREATED)
    public WishResponse add(@RequestHeader(value = "Authorization", required = false) String header,
            @Valid @RequestBody WishRequest request) {
        Long memberId = members.resolve(header);
        return WishResponse.of(addition.add(memberId, request.productId()));
    }
}
