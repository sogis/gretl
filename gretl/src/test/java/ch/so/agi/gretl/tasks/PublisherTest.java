package ch.so.agi.gretl.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;

class PublisherTest {
    @Test
    void acceptsCanonicalDbAndOutputDslValues() {
        Project project = ProjectBuilder.builder().build();
        Publisher task = project.getTasks().create("publish", Publisher.class);

        task.setDbDatabase(List.of("jdbc:postgresql://source/db", "source_user", "source_pass"));
        task.setDbSchema("live");
        task.setDbIliIdent_Type("dataset");
        task.setDbIliIdent_Values(List.of("2401", "2402"));
        task.setDbMergeToSingleXtf(false);
        task.setOutFolderPath(List.of("/publication-root"));
        task.setOutDataIdent("ch.so.agi.demo");
        task.setOutWriteMetadata(false);
        task.setOutFormats(List.of("xtf", "itf", OutputFormat.SHP, "gpkg"));

        assertEquals("jdbc:postgresql://source/db", task.getDbDatabase().getDbUri());
        assertEquals(List.of("2401", "2402"), task.getDbIliIdent_Values().get());
        assertEquals("/publication-root", task.getOutFolderPath().getUrl());
        assertEquals(List.of(OutputFormat.XTF, OutputFormat.ITF, OutputFormat.SHP, OutputFormat.GPKG),
                task.getOutFormats().get());
        assertEquals(Boolean.FALSE, task.getOutWriteMetadata());
    }

    @Test
    void acceptsAFileAsLocalOutputFolder() {
        Project project = ProjectBuilder.builder().build();
        Publisher task = project.getTasks().create("publish", Publisher.class);

        task.setOutFolderPath(new java.io.File("build/local-publication"));

        assertEquals(project.file("build/local-publication").toPath().toAbsolutePath().normalize().toString(),
                task.getOutFolderPath().getUrl());
    }

    @Test
    void keepsLegacyTaskAsAnExplicitSeparateType() {
        Project project = ProjectBuilder.builder().build();

        Object oldTask = project.getTasks().create("publishOld", PublisherOld.class);

        assertInstanceOf(PublisherOld.class, oldTask);
    }
}
