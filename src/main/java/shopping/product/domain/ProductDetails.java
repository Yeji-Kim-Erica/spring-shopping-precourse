package shopping.product.domain;

import java.net.URI;
import java.util.regex.Pattern;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;

public record ProductDetails(String name, Integer price, String imageUrl) {

    private static final Pattern NAME = Pattern.compile("[\\p{L}\\p{N} ()\\[\\]+&/_-]+");
    private static final int MAX_NAME_LENGTH = 15;

    public ProductDetails {
        validateName(name);
        validatePrice(price);
        validateImageUrl(imageUrl);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품 이름은 필수입니다.");
        }
        if (name.codePointCount(0, name.length()) > MAX_NAME_LENGTH
                || !NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("상품 이름의 길이 또는 문자가 올바르지 않습니다.");
        }
    }

    private static void validatePrice(Integer price) {
        if (price == null || price < 1) {
            throw new IllegalArgumentException("가격은 1원 이상의 정수여야 합니다.");
        }
    }

    private static void validateImageUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        }

        URI uri = URI.create(url);
        boolean http = "http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme());
        if (!http || uri.getHost() == null) {
            throw new IllegalArgumentException("호스트가 있는 HTTP URL이어야 합니다.");
        }
    }

    public void checkProfanity(ProfanityChecker checker) {
        if (checker.containsProfanity(name)) {
            throw new ShoppingException(ErrorCode.PROFANITY_DETECTED);
        }
    }
}
