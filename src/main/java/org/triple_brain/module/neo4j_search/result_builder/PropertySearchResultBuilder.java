/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search.result_builder;

import org.triple_brain.module.model.graph.schema.SchemaPojo;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceFromExtractorQueryRow;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import org.triple_brain.module.search.GraphElementSearchResult;
import org.triple_brain.module.search.PropertySearchResult;

import java.util.Map;

public class PropertySearchResultBuilder implements SearchResultBuilder {

    private Map<String, Object> row;
    private String prefix;

    public PropertySearchResultBuilder(Map<String, Object> row, String prefix){
        this.row = row;
        this.prefix = prefix;
    }

    @Override
    public GraphElementSearchResult update(GraphElementSearchResult graphElementSearchResult) {
        return graphElementSearchResult;
    }

    @Override
    public GraphElementSearchResult build() {
        return PropertySearchResult.forPropertyAndSchema(
                GraphElementFromExtractorQueryRow.usingRowAndKey(row, prefix).build(),
                new SchemaPojo(
                        FriendlyResourceFromExtractorQueryRow.usingRowAndNodeKey(row, "related_node").build()
                )
        );
    }
}
