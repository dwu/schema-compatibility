package io.github.dwu.schemacompatibility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.SchemaProvider;
import io.confluent.kafka.schemaregistry.SimpleParsedSchemaHolder;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.exceptions.InvalidSchemaException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import io.github.dwu.schemacompatibility.desc.Case;

import java.util.*;

public class CompatibilityChecker {

    private final Gson gson = new GsonBuilder().create();
    private final Map<String, SchemaProvider> schemaProviderMap = new HashMap<String, SchemaProvider>();

    public CompatibilityChecker() {
        schemaProviderMap.put(AvroSchema.TYPE, new AvroSchemaProvider());
        schemaProviderMap.put(ProtobufSchema.TYPE, new ProtobufSchemaProvider());
        schemaProviderMap.put(JsonSchema.TYPE, new JsonSchemaProvider());
    }

    public CompatibilityCheckResult checkCase(Case c) throws InvalidSchemaException {
        CompatibilityLevel compatibiltyLevel = CompatibilityLevel.valueOf(c.getCheck().getCompatibility());
        String schemaType = c.getSchema().getType();
        String writerSchemaString = c.getSchema().getWriter();
        String readerSchemaString = c.getSchema().getReader();

        List<String> checkResult = checkSchemaCompatibility(schemaType, compatibiltyLevel, writerSchemaString, readerSchemaString);

        CompatibilityCheckResult result = new CompatibilityCheckResult();
        result.setCompatibilityLevel(compatibiltyLevel);
        result.setCompatible(checkResult.isEmpty());
        result.setSuccess(checkResult.isEmpty() == c.getCheck().isCompatible());

        result.setMessages(checkResult);
        // cleanup results
        result.getMessages().removeIf(s -> s.startsWith("{oldSchema: "));

        return result;
    }

    public List<String> checkSchemaCompatibility(String schemaType, CompatibilityLevel compatibilityLevel, String writerSchemaString, String readerSchemaString) throws InvalidSchemaException {
        SchemaProvider schemaProvider = schemaProviderMap.get(schemaType);

        Optional<ParsedSchema> writerSchema = schemaProvider.parseSchema(writerSchemaString, Collections.emptyList());
        Optional<ParsedSchema> readerSchema = schemaProvider.parseSchema(readerSchemaString, Collections.emptyList());

        if (!(writerSchema.isPresent() && readerSchema.isPresent())) {
            throw new InvalidSchemaException();
        }

        return readerSchema.get().isCompatible(compatibilityLevel, Collections.singletonList(new SimpleParsedSchemaHolder(writerSchema.get())));
    }

}
