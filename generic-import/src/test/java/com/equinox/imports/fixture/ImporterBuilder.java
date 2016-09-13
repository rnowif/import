package com.equinox.imports.fixture;

import com.equinox.imports.ImportFactoryBean;
import com.equinox.imports.Importer;

public class ImporterBuilder {

    private String mappingFile;
    private String mappingName = "myMapping";

    public static ImporterBuilder anImporter() {
        return new ImporterBuilder();
    }

    public ImporterBuilder withMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
        return this;
    }

    public Importer build() {
        ImportFactoryBean factory = new ImportFactoryBean();
        factory.setMappingResources(mappingFile);
        factory.buildImportFactory();

        return factory.buildImporter(mappingName);
    }
}
