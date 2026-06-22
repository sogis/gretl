package ch.so.agi.gretl.steps.publisher.in.db.tocache;

import java.util.List;

interface SelectionMetadataReader {
    List<String> getDatasets() throws Exception;

    List<String> getModels() throws Exception;

    List<String> getTopics() throws Exception;

    List<String> getBaskets() throws Exception;
}
