package com.equinox.imports.feature;

import com.equinox.imports.ImportFactoryBean;
import com.equinox.imports.Importer;
import com.equinox.imports.exception.ImportException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ImportDTOFromCSVFileTest {

    @Test
    public void should_import_dto_from_csv_file() throws ImportException {
        Importer importer = buildImporter("features/csv/mapping.xml");
        List<MyObject> objects = importer.importFrom(MyObject.class, pathFrom("features/csv/file.csv"));

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

    @Test
    public void should_skip_lines() throws ImportException {
        Importer importer = buildImporter("features/csv/mapping-skip-lines.xml");
        List<MyObject> objects = importer.importFrom(MyObject.class, pathFrom("features/csv/file-skip-lines.csv"));

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

    @Test
    public void should_read_between_two_tags_when_start_tag_and_end_tag_specified() throws ImportException {
        Importer importer = buildImporter("features/csv/mapping-tags.xml");
        List<MyObject> objects = importer.importFrom(MyObject.class, pathFrom("features/csv/file-tags.csv"));

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

    @Test
    @Ignore("There is a bug with this behavior: https://github.com/rnowif/import/issues/8")
    public void should_use_header_as_label_when_labels_is_true() throws ImportException {
        Importer importer = buildImporter("features/csv/mapping-labels.xml");
        List<MyObject> objects = importer.importFrom(MyObject.class, pathFrom("features/csv/file-labels.csv"));

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

    private Importer buildImporter(String mappingFile) {
        ImportFactoryBean factory = new ImportFactoryBean();
        factory.setMappingResources(mappingFile);
        factory.buildImportFactory();

        return factory.buildImporter("myMapping");
    }

    private String pathFrom(String fileName) {
        return getClass().getClassLoader().getResource(fileName).getPath();
    }
}
