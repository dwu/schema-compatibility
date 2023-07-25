package io.github.dwu.schemacompatibility.desc;

public class Schema {

    private String type;
    private String oldschema;
    private String newschema;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOldschema() {
        return oldschema;
    }

    public void setOldschema(String oldschema) {
        this.oldschema = oldschema;
    }

    public String getNewschema() {
        return newschema;
    }

    public void setNewschema(String newschema) {
        this.newschema = newschema;
    }
}
