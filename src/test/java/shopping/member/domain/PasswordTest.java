package shopping.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {

    @ParameterizedTest
    @ValueSource(ints = {5, 16})
    void acceptsBoundaryLengths(int length) {
        assertThatCode(() -> new Password("a".repeat(length))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abcd", "12345678901234567", " abcd", "ab cd", "abcd\t"})
    void rejectsMissingOutOfRangeOrWhitespacePassword(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Password(value));
    }
}
