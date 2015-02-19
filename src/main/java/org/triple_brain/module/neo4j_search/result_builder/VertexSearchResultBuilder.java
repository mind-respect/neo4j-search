/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search.result_builder;

import org.triple_brain.module.model.graph.GraphElementPojo;
import org.triple_brain.module.model.graph.GraphElementType;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import org.triple_brain.module.search.GraphElementSearchResult;
import org.triple_brain.module.search.VertexSearchResult;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VertexSearchResultBuilder implements SearchResultBuilder {

    private Map<String, Object> row;
    private String prefix;

    public VertexSearchResultBuilder(Map<String, Object> row, String prefix) {
        this.row = row;
        this.prefix = prefix;
    }

    @Override
    public GraphElementSearchResult build() {
        VertexSearchResult searchResult = new VertexSearchResult(
                GraphElementFromExtractorQueryRow.usingRowAndKey(row, prefix).build(),
                GraphElementType.vertex
        );
        searchResult.getProperties().putAll(
                VertexSearchResultBuilder.buildPropertiesFromRow(
                        row
                )
        );
        return searchResult;
    }

    public static Map<URI, GraphElementPojo> buildPropertiesFromRow(Map<String, Object> row) {
        List<List<String>> propertiesList = RelatedFriendlyResourceExtractor.getListOfPropertiesFromRow(
                row
        );
        Map<URI, GraphElementPojo> properties = new HashMap<>();
        for (List<String> propertiesString : propertiesList) {
            RelatedFriendlyResourceExtractor friendlyResourceExtractor = RelatedFriendlyResourceExtractor.fromResourceProperties(
                    propertiesString
            );
            if (friendlyResourceExtractor.hasResource()) {
                addProperty(
                        friendlyResourceExtractor,
                        properties
                );
            }

        }
        return properties;
    }

    private static void addProperty(RelatedFriendlyResourceExtractor friendlyResourceExtractor, Map<URI, GraphElementPojo> properties) {
        GraphElementPojo property = new GraphElementPojo(
                friendlyResourceExtractor.get()
        );
        properties.put(
                property.uri(),
                property
        );
    }
}
