package com.equinox.imports.feature;

import com.equinox.imports.Importer;
import com.equinox.imports.exception.ImportException;
import com.equinox.imports.fixture.MyObject;
import com.equinox.imports.fixture.PathFinder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static com.equinox.imports.fixture.ImporterBuilder.anImporter;
import static org.assertj.core.api.Assertions.assertThat;

public class ImportDTOFromCSVFileTest {

    @Test
    public void should_import_dto_from_csv_file() throws ImportException {
        Importer importer = anImporter().withMappingFile("features/csv/mapping.xml").build();
        List<MyObject> objects = importer.importFrom(MyObject.class, new PathFinder("features/csv/file.csv").getPath());

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

    @Test
    public void should_skip_lines() throws ImportException {
        Importer importer = anImporter().withMappingFile("features/csv/mapping-skip-lines.xml").build();
        List<MyObject> objects = importer.importFrom(MyObject.class, new PathFinder("features/csv/file-skip-lines.csv").getPath());

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

    @Test
    public void should_read_between_two_tags_when_start_tag_and_end_tag_specified() throws ImportException {
        Importer importer = anImporter().withMappingFile("features/csv/mapping-tags.xml").build();
        List<MyObject> objects = importer.importFrom(MyObject.class, new PathFinder("features/csv/file-tags.csv").getPath());

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

    @Test
    @Ignore("There is a bug with this behavior: https://github.com/rnowif/import/issues/8")
    public void should_use_header_as_label_when_labels_is_true() throws ImportException {
        Importer importer = anImporter().withMappingFile("features/csv/mapping-labels.xml").build();
        List<MyObject> objects = importer.importFrom(MyObject.class, new PathFinder("features/csv/file-labels.csv").getPath());

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

}
