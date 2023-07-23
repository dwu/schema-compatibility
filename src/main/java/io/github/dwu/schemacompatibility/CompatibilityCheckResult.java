package io.github.dwu.schemacompatibility;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;

import java.util.List;

public class CompatibilityCheckResult {

    private CompatibilityLevel compatibilityLevel;
    private boolean compatible;
    private List<String> messages;

    private boolean success;

    public CompatibilityLevel getCompatibilityLevel() {
        return compatibilityLevel;
    }

    public void setCompatibilityLevel(CompatibilityLevel compatibilityLevel) {
        this.compatibilityLevel = compatibilityLevel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isCompatible() {
        return compatible;
    }

    public void setCompatible(boolean compatible) {
        this.compatible = compatible;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

}