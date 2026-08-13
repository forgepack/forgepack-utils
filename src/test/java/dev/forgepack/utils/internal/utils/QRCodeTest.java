package dev.forgepack.utils.internal.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class QRCodeTest {

    @Test
    void generateQRCodeBytes_shouldReturnNonEmptyByteArray() {
        byte[] result = QRCode.generateQRCodeBytes("https://example.com", 200);
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    void generateQRCodeBytes_shouldReturnValidPngSignature() {
        byte[] result = QRCode.generateQRCodeBytes("test content", 200);
        // PNG magic bytes: 89 50 4E 47
        assertThat(result[0]).isEqualTo((byte) 0x89);
        assertThat(result[1]).isEqualTo((byte) 0x50);
        assertThat(result[2]).isEqualTo((byte) 0x4E);
        assertThat(result[3]).isEqualTo((byte) 0x47);
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 200, 300, 500})
    void generateQRCodeBytes_withVariousSizes_shouldSucceed(int size) {
        byte[] result = QRCode.generateQRCodeBytes("content", size);
        assertThat(result).isNotEmpty();
    }

    @Test
    void generateQRCodeBytes_withNullText_shouldThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> QRCode.generateQRCodeBytes(null, 200));
    }

    @Test
    void generateQRCodeBytes_withBlankText_shouldThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> QRCode.generateQRCodeBytes("   ", 200));
    }

    @Test
    void generateQRCodeBytes_withZeroSize_shouldThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> QRCode.generateQRCodeBytes("content", 0));
    }

    @Test
    void generateQRCodeBytes_withNegativeSize_shouldThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> QRCode.generateQRCodeBytes("content", -1));
    }
}
