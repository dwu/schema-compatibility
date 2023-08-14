package io.github.dwu.schemacompatibility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.dwu.schemacompatibility.desc.Case;

public class JsonResultFormatter implements ResultFormatter {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public String format(String caseFileName, Case caseDescription, CompatibilityCheckResult compatibilityCheckResult) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", compatibilityCheckResult.isSuccess());
        obj.addProperty("testcase", caseFileName);
        obj.addProperty("description", caseDescription.getDescription());
        obj.addProperty("expected", caseDescription.getCheck().isCompatible() ? "COMPATIBLE" : "NOT_COMPATIBLE");
        obj.addProperty("got", compatibilityCheckResult.isCompatible() ? "COMPATIBLE" : "NOT_COMPATIBLE");
        obj.addProperty("compatibility", caseDescription.getCheck().getCompatibility());
        obj.addProperty("schematype", caseDescription.getSchema().getType());
        obj.addProperty("oldschema", caseDescription.getSchema().getOldschema());
        obj.addProperty("newschema", caseDescription.getSchema().getNewschema());

        JsonArray messages = new JsonArray();
        for (String message : compatibilityCheckResult.getMessages()) {
            messages.add(message);
        }
        obj.add("messages", messages);

        return gson.toJson(obj);
    }

}
