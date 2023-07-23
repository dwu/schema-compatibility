package io.github.dwu.schemacompatibility;

import io.github.dwu.schemacompatibility.desc.Case;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestProtobuf extends TestBase {

    @Test
    void testNotBackwardCompatible() {
        Case c = loadCase("protobuf-not-backward-compatible.yml");

        CompatibilityCheckResult compatibilityCheckResult = assertDoesNotThrow(() -> compatibilityChecker.checkCase(c));
        assertTrue(compatibilityCheckResult.isSuccess());
        assertEquals(1, compatibilityCheckResult.getMessages().size());
    }

}