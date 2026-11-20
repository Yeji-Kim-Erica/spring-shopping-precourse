package shopping.wish.web;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shopping.auth.web.CurrentMember;
import shopping.wish.application.WishQueryService;

@RestController
public class WishQueryController {

    private final WishQueryService query;
    private final CurrentMember members;

    public WishQueryController(WishQueryService query, CurrentMember members) {
        this.query = query;
        this.members = members;
    }

    @GetMapping("/api/wishes")
    public List<WishResponse> list(
            @RequestHeader(value = "Authorization", required = false) String header,
            @RequestParam Map<String, String> parameters) {
        Long memberId = members.resolve(header);

        if (!parameters.isEmpty()) {
            throw new IllegalArgumentException("위시 목록 조회에는 쿼리 파라미터를 사용하지 않습니다.");
        }

        return query.list(memberId).stream().map(WishResponse::of).toList();
    }
}
