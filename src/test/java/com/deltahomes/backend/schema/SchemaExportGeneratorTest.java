package com.deltahomes.backend.schema;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.schema.SourceType;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
import org.hibernate.tool.schema.spi.CommandAcceptanceException;
import org.hibernate.tool.schema.spi.ContributableMatcher;
import org.hibernate.tool.schema.spi.ExceptionHandler;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

/**
 * Generates {@code db/migration/V1__baseline.sql} directly from the JPA entity
 * model — no database connection required. Re-run after entity changes:
 *
 * <pre>mvn test -Dtest=SchemaExportGeneratorTest</pre>
 *
 * The output is the Flyway baseline schema (V1). Custom SQL that Hibernate
 * cannot express — generated tsvector columns, GIN/pg_trgm indexes, data
 * cleanup before adding constraints — lives in the hand-written V2+ migrations.
 */
class SchemaExportGeneratorTest {

    private static final String ENTITY_PACKAGE_PATH = "com/deltahomes/backend/entity";

    @Test
    void generateBaselineDdl() throws Exception {
        MetadataSources sources = new MetadataSources(
                new org.hibernate.boot.registry.BootstrapServiceRegistryBuilder().build());

        for (Class<?> clazz : scanEntityClasses()) {
            sources.addAnnotatedClass(clazz);
        }

        Metadata metadata = sources.buildMetadata(
                new StandardServiceRegistryBuilder()
                        .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
                        .build());

        Path outFile = Paths.get("src", "main", "resources", "db", "migration", "V1__baseline.sql");
        Files.createDirectories(outFile.getParent());
        Files.deleteIfExists(outFile);

        SchemaCreatorImpl creator = new SchemaCreatorImpl(
                metadata.getDatabase().getServiceRegistry());

        creator.doCreation(
                metadata,
                options(),
                ContributableMatcher.ALL,
                sourceDescriptor(),
                targetDescriptor(outFile));

        System.out.println("Baseline DDL written to " + outFile.toAbsolutePath());
        org.junit.jupiter.api.Assertions.assertTrue(Files.size(outFile) > 0,
                "Baseline DDL file is empty");
    }

    private static ExecutionOptions options() {
        return new ExecutionOptions() {
            @Override public java.util.Map<String, Object> getConfigurationValues() {
                return java.util.Map.of();
            }

            @Override public boolean shouldManageNamespaces() { return false; }

            @Override public ExceptionHandler getExceptionHandler() {
                return (CommandAcceptanceException exception) -> {
                    throw new IllegalStateException(exception.getMessage(), exception);
                };
            }

            @Override public org.hibernate.tool.schema.spi.SchemaFilter getSchemaFilter() {
                return org.hibernate.tool.schema.spi.SchemaFilter.ALL;
            }
        };
    }

    private static org.hibernate.tool.schema.spi.SourceDescriptor sourceDescriptor() {
        return new org.hibernate.tool.schema.spi.SourceDescriptor() {
            @Override public SourceType getSourceType() { return SourceType.METADATA; }

            @Override public org.hibernate.tool.schema.spi.ScriptSourceInput getScriptSourceInput() {
                return null;
            }
        };
    }

    private static org.hibernate.tool.schema.spi.TargetDescriptor targetDescriptor(Path outFile) {
        return new org.hibernate.tool.schema.spi.TargetDescriptor() {
            @Override public EnumSet<TargetType> getTargetTypes() {
                return EnumSet.of(TargetType.SCRIPT);
            }

            @Override public org.hibernate.tool.schema.spi.ScriptTargetOutput getScriptTargetOutput() {
                return new ScriptTargetOutputToFile(new File(outFile.toString()), "UTF-8");
            }
        };
    }

    /** Scans compiled classes under target/classes for @Entity types. */
    private static List<Class<?>> scanEntityClasses() throws IOException {
        Path classesDir = Paths.get("target", "classes").toAbsolutePath();
        if (!Files.isDirectory(classesDir)) {
            throw new IllegalStateException("Run `mvn compile` before generating the baseline: "
                    + classesDir + " not found");
        }
        Path packageDir = classesDir.resolve(ENTITY_PACKAGE_PATH);
        try (Stream<Path> walk = Files.walk(packageDir)) {
            return walk
                    .filter(p -> p.toString().endsWith(".class") && !p.toString().contains("$"))
                    .map(p -> toClassName(classesDir, p))
                    .map(SchemaExportGeneratorTest::load)
                    .filter(c -> c.isAnnotationPresent(Entity.class))
                    .toList();
        }
    }

    private static String toClassName(Path classesDir, Path classFile) {
        String relative = classesDir.relativize(classFile).toString();
        return relative.substring(0, relative.length() - ".class".length())
                .replace('\\', '.');
    }

    private static Class<?> load(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load " + className, e);
        }
    }
}
