package io.github.dwu.schemacompatibility;

import io.github.dwu.schemacompatibility.desc.Case;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public abstract class TestBase {

    protected CompatibilityChecker compatibilityChecker = new CompatibilityChecker();

    protected Case loadCase(String resource) {
        Yaml yaml = new Yaml(new Constructor(Case.class, new LoaderOptions()));

        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(resource);

        return yaml.load(inputStream);
    }

}
