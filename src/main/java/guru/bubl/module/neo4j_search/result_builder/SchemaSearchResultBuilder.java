/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search.result_builder;

import guru.bubl.module.model.graph.GraphElementType;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import guru.bubl.module.search.GraphElementSearchResult;
import guru.bubl.module.search.VertexSearchResult;

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
