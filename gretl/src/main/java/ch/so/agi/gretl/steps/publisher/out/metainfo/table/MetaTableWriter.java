package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.so.agi.gretl.steps.publisher.PartWorkspace;
import ch.so.agi.gretl.steps.publisher.operation.Operation;

/**
 * Writes one publication payload into the metadata tables.
 *
 * <p>This class coordinates the persistence of the publication root row and
 * its dependent part and artifact-type rows. It owns the write flow but does
 * not contain retention logic.</p>
 */
public class MetaTableWriter implements Operation {
    private final MetaTableWriterParameters parameters;
    private final Repository repository;
    private final Mapper mapper;

    public MetaTableWriter(MetaTableWriterParameters parameters) {
        this(parameters, new Repository(), new Mapper());
    }

    public MetaTableWriter(MetaTableWriterParameters parameters, Repository repository, Mapper mapper) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public void execute() throws SQLException {
        write(parameters);
    }

    @Override
    public String getHumanReadableName() { return "Table metadata writer"; }

    @Override
    public String getSuccessLogDetail() {
        return "recorded publication metadata for " + parameters.getPublicationIdent();
    }

    void write(MetaTableWriterParameters operationParameters) throws SQLException {
        Objects.requireNonNull(operationParameters, "operationParameters");
        Mapper.PublicationTree publicationTree = mapper.map(operationParameters.getPublicationIdent(),
                operationParameters.getPublicationDate(), partIdentifiers(operationParameters),
                discoverPublishedFormats(operationParameters.getCacheRoot()));
        repository.write(operationParameters.getConnection(), operationParameters.getMetadataSchema(), publicationTree);
    }

    private static List<String> partIdentifiers(MetaTableWriterParameters parameters) {
        if (parameters.getRawRoot() == null) return parameters.getPartIdentifiers();
        try (java.util.stream.Stream<Path> stream = java.nio.file.Files.list(parameters.getRawRoot())) {
            List<String> parts = stream.filter(java.nio.file.Files::isDirectory)
                    .map(path -> PartWorkspace.decode(path.getFileName().toString())).sorted().collect(Collectors.toList());
            if (parts.isEmpty()) throw new IllegalArgumentException("raw workspace has no parts: " + parameters.getRawRoot());
            return parts;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("failed to inspect raw workspace " + parameters.getRawRoot(), e);
        }
    }

    private static List<String> discoverPublishedFormats(Path cacheRoot) {
        try (java.util.stream.Stream<Path> stream = java.nio.file.Files.list(cacheRoot)) {
            List<String> formats = new java.util.ArrayList<String>();
            stream.filter(java.nio.file.Files::isRegularFile).map(path -> path.getFileName().toString().toLowerCase())
                    .filter(name -> name.endsWith(".zip")).forEach(name -> addFormat(formats, name.substring(0, name.length() - 4)));
            if (formats.isEmpty()) throw new IllegalArgumentException("no packed publication artifacts found in " + cacheRoot);
            return formats;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("failed to inspect cacheRoot " + cacheRoot, e);
        }
    }

    private static void addFormat(List<String> formats, String base) {
        String format = "geobau_dxf".equals(base) ? "dxf_geobau"
                : ("gpkg".equals(base) || "shp".equals(base) || "dxf".equals(base) ? base
                : base.substring(base.lastIndexOf('.') + 1));
        if (("xtf".equals(format) || "itf".equals(format) || "gpkg".equals(format) || "shp".equals(format)
                || "dxf".equals(format) || "dxf_geobau".equals(format)) && !formats.contains(format)) formats.add(format);
    }
}
