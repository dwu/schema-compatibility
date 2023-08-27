package io.github.dwu.schemacompatibility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.exceptions.InvalidSchemaException;
import io.github.dwu.schemacompatibility.desc.Case;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class App {

    private final Gson gson = new GsonBuilder().create();
    private final CompatibilityChecker compatibilityChecker = new CompatibilityChecker();
    private ResultFormatter resultFormatter = null;

    public App(ResultFormatter resultFormatter) {
        this.resultFormatter = resultFormatter;
    }

    public void checkCaseDirectory(String caseDirectory) throws InvalidSchemaException, IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(caseDirectory))) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> {
                        String lcp = p.getFileName().toString().toLowerCase();
                        return lcp.endsWith(".yml") || lcp.endsWith(".yaml");
                    })
                    .sorted()
                    .forEach(p -> {
                        try {
                            checkCaseFile(p.toString());
                        } catch (InvalidSchemaException | FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    public void checkCaseFile(String caseFile) throws InvalidSchemaException, FileNotFoundException {
        Yaml yaml = new Yaml(new Constructor(Case.class, new LoaderOptions()));
        InputStream inputStream = new FileInputStream(caseFile);
        Case caseDescription = yaml.load(inputStream);

        CompatibilityCheckResult compatibilityCheckResult = compatibilityChecker.checkCase(caseDescription);

        System.out.println(resultFormatter.format(caseFile, caseDescription, compatibilityCheckResult));

        if (!compatibilityCheckResult.isSuccess()) {
            System.exit(1);
        }
    }

    public void checkCompatibility(String schemaType, String oldschemaFilename, String newschemaFilename) throws InvalidSchemaException, IOException {
        String oldschema = FileUtils.readFileToString(new File(oldschemaFilename), "UTF-8");
        String newschema  = FileUtils.readFileToString(new File(newschemaFilename), "UTF-8");
        List<CompatibilityLevel> levels = Arrays.asList(CompatibilityLevel.BACKWARD, CompatibilityLevel.FORWARD, CompatibilityLevel.FULL);
        for (CompatibilityLevel level : levels) {
            List<String> compatibilityCheckResult = compatibilityChecker.checkSchemaCompatibility(schemaType, level, oldschema, newschema);
            compatibilityCheckResult.removeIf(s -> s.startsWith("{oldSchema: "));

            String success = compatibilityCheckResult.size() == 0 ? "OK" : "FAIL";
            System.out.println(level.toString() + ": " + success);

            for (String message : compatibilityCheckResult) {
                System.out.println("  - " + message);
            }
        }
    }

    public void setResultFormatter(ResultFormatter resultFormatter) {
        this.resultFormatter = resultFormatter;
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("j", "json", false, "Show result in JSON format for further processing");
        options.addOption("f", "file", true, "Schema compatibilty test case description file");
        options.addOption("d", "directory", true, "Directory containing schema compatibility test case description files (scanned recursively)");
        options.addOption("t", "test", false, "Run in test mode; perform all possible compatibility checks (requires -o and -n)");
        options.addOption("o", "oldschema", true, "Old schema file (used only when in 'test' mode)");
        options.addOption("n", "newschema", true, "New schema file (used only when in 'test' mode)");
        options.addOption("s", "schematype", true, "Type of the schema, i.e. 'AVRO', 'PROTOBUF', 'JSON' (used only when in 'test' mode)");
        options.addOption("h", "help", false, "Show help");

        CommandLineParser parser = new DefaultParser();
        CommandLine cli = parser.parse(options, args);

        if (cli.hasOption("f") && cli.hasOption("d")) {
            System.err.println("ERROR: Only one of 'file' and 'path' arguments may be specified");
            printHelpAndExit(options);
        }

        App app;
        if (cli.hasOption("j")) {
            app = new App(new JsonResultFormatter());
        } else {
            app = new App(new PlaintextResultFormatter());
        }

        CompatibilityCheckResult compatibilityCheckResult = null;
        if (cli.hasOption("f")) {
            app.checkCaseFile(cli.getOptionValue("f"));
        } else if (cli.hasOption("d")) {
            app.checkCaseDirectory(cli.getOptionValue("d"));
        } else if (cli.hasOption("t")) {
            if (!cli.hasOption("o") || !cli.hasOption("n") || !cli.hasOption("s")) {
                System.err.println("ERROR: Test mode requires 'schematype', 'oldschema' and 'newschema' parameters");
                printHelpAndExit(options);
            }
            app.checkCompatibility(cli.getOptionValue("s"), cli.getOptionValue("o"), cli.getOptionValue("n"));
        } else if (cli.hasOption("h")) {
            printHelpAndExit(options);
        } else {
            System.err.println("ERROR: One of 'test', 'file' or 'path' arguments must be specified");
            printHelpAndExit(options);
        }
    }

    private static void printHelpAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("schema-compatibility", options);
        System.exit(1);
    }
}