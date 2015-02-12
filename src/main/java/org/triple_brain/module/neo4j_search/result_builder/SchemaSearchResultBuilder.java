/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search.result_builder;

import org.triple_brain.module.model.graph.FriendlyResourcePojo;
import org.triple_brain.module.model.graph.GraphElementPojo;
import org.triple_brain.module.model.graph.GraphElementType;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceFromExtractorQueryRow;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import org.triple_brain.module.search.GraphElementSearchResult;
import org.triple_brain.module.search.VertexSearchResult;

import java.util.Map;

public class SchemaSearchResultBuilder implements SearchResultBuilder{

    private Map<String, Object> row;
    private String prefix;

    public SchemaSearchResultBuilder(Map<String, Object> row, String prefix){
        this.row = row;
        this.prefix = prefix;
    }

    @Override
    public GraphElementSearchResult update(GraphElementSearchResult graphElementSearchResult) {
        VertexSearchResult searchResult = (VertexSearchResult) graphElementSearchResult;
        GraphElementPojo property = buildProperty(row);
        searchResult.getProperties().put(
                property.uri(),
                property
        );
        return searchResult;
    }

    @Override
    public GraphElementSearchResult build() {
        return new VertexSearchResult(
                GraphElementFromExtractorQueryRow.usingRowAndKey(row, prefix).build(),
                GraphElementType.schema
        );
    }

    private GraphElementPojo buildProperty(Map<String, Object> row){
        FriendlyResourceFromExtractorQueryRow extractor = FriendlyResourceFromExtractorQueryRow.usingRowAndNodeKey(
                row,
                "related_node"
        );
        return new GraphElementPojo(
                new FriendlyResourcePojo(
                        extractor.getUri(),
                        extractor.getLabel()
                )
        );
    }
}
