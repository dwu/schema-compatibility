## Schema Compatibility

This application allows checking schema compatibility for Avro, ProtoBuf and JSON schema files.

This compatibility check is performed
using [Confluent's Schema Registry](https://github.com/confluentinc/schema-registry).

Schema compatibilty test inputs are described in a test descriptor.

## Test Descriptor Example

The following example defines a `BACKWARD` schema compatibility test for an Avro schema which is expected to fail.

```yaml
description: Change long to int
check:
  compatibility: "BACKWARD"
  compatible: false
schema:
  type: "AVRO"
  oldschema: |
    {"namespace": "example.avro",
     "type": "record",
     "name": "User",
     "fields": [
         {"name": "name", "type": "string"},
         {"name": "favorite_number",  "type": ["long", "null"]},
         {"name": "favorite_color", "type": ["string", "null"]}
     ]
    }
  newschema: |
    {"namespace": "example.avro",
     "type": "record",
     "name": "User",
     "fields": [
         {"name": "name", "type": "string"},
         {"name": "favorite_number",  "type": ["int", "null"]},
         {"name": "favorite_color", "type": ["string", "null"]}
     ]
    }
```

The result of the compatibility test execution is printed in JSON format with the following properties:

- `success` (bool): Whether the compatibility test result matches the expected result
- `testcase` (string): Name of the file with the test description
- `description` (string): Test case description
- `compatibility` (string): Expected compatibility level as defined by Confluent's Schema Registry (e.g. "BACKWARD", "
  FORWARD", "FULL")
- `expected` (string):
    - "COMPATIBLE" if the schemas have been found to be compatible with respect to the defined `compatibility` level
    - "NOT_COMPATIBLE" otherwise
- `got` (string): "COMPATIBLE" or "NOT_COMPATIBLE", depending on the compatibility test result
- `messages` (\[string\]): List of messages returned by the schema compatibility test
- `schematype` (string): "AVRO", "PROTOBUF", "JSON"
- `oldschema` (string): Source of the old schema
- `newschema` (string): Source of the new schema

## Usage

Command line arguments:

```
usage: schema-compatibility
 -d,--directory <arg>   Directory containing schema compatibility test
                        case description files (scanned recursively)
 -f,--file <arg>        Schema compatibilty test case description file
 -h,--help              Show help
```

Usage example:

```shell
$ java -jar target/schema-compatibility-1.0-SNAPSHOT-jar-with-dependencies.jar -d ./src/test/resources
{"success":true,"expected":"NOT_COMPATIBLE","got":"NOT_COMPATIBLE","compatibility":"BACKWARD","oldschema":"{\"namespace\": \"example.avro\",\n \"type\": \"record\",\n \"name\": \"User\",\n \"fields\": [\n     {\"name\": \"name\", \"type\": \"string\"},\n     {\"name\": \"favorite_number\",  \"type\": [\"long\", \"null\"]},\n     {\"name\": \"favorite_color\", \"type\": [\"string\", \"null\"]}\n ]\n}\n","newschema":"{\"namespace\": \"example.avro\",\n \"type\": \"record\",\n \"name\": \"User\",\n \"fields\": [\n     {\"name\": \"name\", \"type\": \"string\"},\n     {\"name\": \"favorite_number\",  \"type\": [\"int\", \"null\"]},\n     {\"name\": \"favorite_color\", \"type\": [\"string\", \"null\"]}\n ]\n}","messages":["{errorType:\u0027MISSING_UNION_BRANCH\u0027, description:\u0027The new schema is missing a type inside a union field at path \u0027/fields/1/type/0\u0027 in the old schema\u0027, additionalInfo:\u0027reader union lacking writer type: LONG\u0027}"]}
{"success":true,"expected":"NOT_COMPATIBLE","got":"NOT_COMPATIBLE","compatibility":"BACKWARD","oldschema":"syntax \u003d \"proto3\";\nmessage SearchRequest {\n  string query \u003d 1;\n  int32 page_number \u003d 2;\n  int32 results_per_page \u003d 3;\n}\n","newschema":"syntax \u003d \"proto3\";\nmessage SearchRequest {\n  string query \u003d 1;\n  float page_number \u003d 2;\n  int32 results_per_page \u003d 3;\n}","messages":["{errorType:\"FIELD_SCALAR_KIND_CHANGED\", description:\"The kind of a SCALAR field at path \u0027#/SearchRequest/2\u0027 in the new schema does not match its kind in the old schema\"}"]}
```