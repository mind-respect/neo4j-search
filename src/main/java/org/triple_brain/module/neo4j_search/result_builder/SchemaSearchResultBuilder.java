/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package org.triple_brain.module.neo4j_search.result_builder;

import org.triple_brain.module.model.graph.GraphElementType;
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
    public GraphElementSearchResult build() {
        VertexSearchResult searchResult = new VertexSearchResult(
                GraphElementFromExtractorQueryRow.usingRowAndKey(row, prefix).build(),
                GraphElementType.schema
        );
        searchResult.getProperties().putAll(
                VertexSearchResultBuilder.buildPropertiesFromRow(
                        row
                )
        );
        return searchResult;
    }
}
