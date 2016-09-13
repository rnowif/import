package com.equinox.imports.feature;

import com.equinox.imports.Importer;
import com.equinox.imports.exception.ImportException;
import com.equinox.imports.fixture.MyObject;
import com.equinox.imports.fixture.PathFinder;
import org.junit.Test;

import java.util.List;

import static com.equinox.imports.fixture.ImporterBuilder.anImporter;
import static org.assertj.core.api.Assertions.assertThat;

public class ImportDTOFromXLSFileTest {

    @Test
    public void should_import_dto_from_xls_file() throws ImportException {
        Importer importer = anImporter().withMappingFile("features/xls/mapping.xml").build();
        List<MyObject> objects = importer.importFrom(MyObject.class, new PathFinder("features/xls/file.xls").getPath());

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

    @Test
    public void should_skip_lines() throws ImportException {
        Importer importer = anImporter().withMappingFile("features/xls/mapping-skip-lines.xml").build();
        List<MyObject> objects = importer.importFrom(MyObject.class, new PathFinder("features/xls/file-skip-lines.xls").getPath());

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

    @Test
    public void should_use_header_as_label_when_labels_is_true() throws ImportException {
        Importer importer = anImporter().withMappingFile("features/xls/mapping-labels.xml").build();
        List<MyObject> objects = importer.importFrom(MyObject.class, new PathFinder("features/xls/file-labels.xls").getPath());

        assertThat(objects).hasSize(1);
        assertThat(objects).contains(new MyObject("foo", 42));
    }

}
