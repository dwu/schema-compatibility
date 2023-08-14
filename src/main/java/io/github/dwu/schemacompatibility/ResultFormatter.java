package io.github.dwu.schemacompatibility;

import io.github.dwu.schemacompatibility.desc.Case;

public interface ResultFormatter {

    public String format(String caseFileName, Case caseDescription, CompatibilityCheckResult compatibilityCheckResult);

}
