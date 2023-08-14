package io.github.dwu.schemacompatibility;

import io.github.dwu.schemacompatibility.desc.Case;

public class PlaintextResultFormatter implements ResultFormatter {

    @Override
    public String format(String caseFileName, Case caseDescription, CompatibilityCheckResult compatibilityCheckResult) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append(String.format("Success: %b\n", compatibilityCheckResult.isSuccess()));
        sb.append(String.format("Testcase: %s\n", caseFileName));
        sb.append(String.format("Description: %s\n", caseDescription.getDescription()));
        sb.append(String.format("Expected: %s\n", caseDescription.getCheck().isCompatible() ? "COMPATIBLE" : "NOT_COMPATIBLE"));
        sb.append(String.format("Got: %s\n", compatibilityCheckResult.isCompatible() ? "COMPATIBLE" : "NOT_COMPATIBLE"));
        sb.append(String.format("Compatibility: %s\n", caseDescription.getCheck().getCompatibility()));
        sb.append(String.format("Schema type: %s\n", caseDescription.getSchema().getType()));
        sb.append(String.format("\nOld schema:\n-----------\n%s\n", caseDescription.getSchema().getOldschema()));
        sb.append(String.format("\nNew schema:\n-----------\n%s\n", caseDescription.getSchema().getNewschema()));

        return sb.toString();
    }

}
