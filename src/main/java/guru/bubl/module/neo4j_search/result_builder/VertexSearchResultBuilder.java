/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search.result_builder;

import guru.bubl.module.common_utils.NoExRun;
import guru.bubl.module.model.graph.GraphElementPojo;
import guru.bubl.module.model.graph.GraphElementType;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import guru.bubl.module.search.GraphElementSearchResult;
import guru.bubl.module.search.VertexSearchResult;

import javax.xml.transform.Result;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VertexSearchResultBuilder implements SearchResultBuilder {

    private ResultSet row;
    private String prefix;

    public VertexSearchResultBuilder(ResultSet row, String prefix) {
        this.row = row;
        this.prefix = prefix;
    }

    @Override
    public GraphElementSearchResult build() {
        return NoExRun.wrap(() -> {
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
        }).get();
    }

    public static Map<URI, GraphElementPojo> buildPropertiesFromRow(ResultSet row) throws SQLException {
        List<List<String>> propertiesList = RelatedGraphElementExtractor.getListOfPropertiesFromRow(
                row
        );
        Map<URI, GraphElementPojo> properties = new HashMap<>();
        for (List<String> propertiesString : propertiesList) {
            RelatedGraphElementExtractor friendlyResourceExtractor = RelatedGraphElementExtractor.fromResourceProperties(
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

    private static void addProperty(RelatedGraphElementExtractor graphElementExtractor, Map<URI, GraphElementPojo> properties) {
        GraphElementPojo property = graphElementExtractor.get();
        properties.put(
                property.uri(),
                property
        );
    }
}
