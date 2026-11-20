package shopping.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Test
    void comparesTrimmedAddressesWithoutIgnoringCase() {
        Email email = new Email(" Yeji@example.com ");

        assertThat(email).isEqualTo(new Email("Yeji@example.com"));
        assertThat(email).isNotEqualTo(new Email("yeji@example.com"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "user", "@example.com", "user@", "u ser@example.com"})
    void rejectsMissingOrMalformedAddress(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Email(value));
    }

    @Test
    void limitsNormalizedAddressTo254Characters() {
        String prefix = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + ".";
        String address = prefix + "d".repeat(254 - prefix.length());

        assertThat(new Email(" " + address + " ").value()).isEqualTo(address);
        assertThatIllegalArgumentException().isThrownBy(() -> new Email(address + "d"));
    }
}
