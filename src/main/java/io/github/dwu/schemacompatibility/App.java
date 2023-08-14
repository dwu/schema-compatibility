package io.github.dwu.schemacompatibility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.confluent.kafka.schemaregistry.exceptions.InvalidSchemaException;
import io.github.dwu.schemacompatibility.desc.Case;
import org.apache.commons.cli.*;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public void setResultFormatter(ResultFormatter resultFormatter) {
        this.resultFormatter = resultFormatter;
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("j", "json", false, "Show result in JSON format for further processing");
        options.addOption("f", "file", true, "Schema compatibilty test case description file");
        options.addOption("d", "directory", true, "Directory containing schema compatibility test case description files (scanned recursively)");
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
        } else if (cli.hasOption("h")) {
            printHelpAndExit(options);
        } else {
            System.err.println("ERROR: One of 'file' and 'path' arguments must be specified");
            printHelpAndExit(options);
        }
    }

    private static void printHelpAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("schema-compatibility", options);
        System.exit(1);
    }
}