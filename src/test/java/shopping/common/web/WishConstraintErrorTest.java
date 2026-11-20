package shopping.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.dao.DataIntegrityViolationException;

class WishConstraintErrorTest {

    @ParameterizedTest
    @CsvSource({"UQ_WISHES_MEMBER_PRODUCT,409,WISH_ALREADY_EXISTS",
            "FK_WISHES_PRODUCT,404,PRODUCT_NOT_FOUND"})
    void translatesStorageConflictsToDocumentedErrors(String constraint, int status, String code) {
        var response = new ErrorHandler().handleConstraint(
                new DataIntegrityViolationException(constraint));

        assertThat(response.getStatusCode().value()).isEqualTo(status);
        assertThat(response.getBody()).isInstanceOfSatisfying(ErrorResponse.class,
                body -> assertThat(body.code()).isEqualTo(code));
    }
}
