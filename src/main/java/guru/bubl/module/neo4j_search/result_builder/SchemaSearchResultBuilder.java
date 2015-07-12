/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search.result_builder;

import guru.bubl.module.common_utils.NoExRun;
import guru.bubl.module.model.graph.GraphElementType;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import guru.bubl.module.search.GraphElementSearchResult;
import guru.bubl.module.search.VertexSearchResult;

import java.sql.ResultSet;
import java.util.Map;

public class SchemaSearchResultBuilder implements SearchResultBuilder{

    private ResultSet row;
    private String prefix;

    public SchemaSearchResultBuilder(ResultSet row, String prefix){
        this.row = row;
        this.prefix = prefix;
    }

    @Override
    public GraphElementSearchResult build() {
        return NoExRun.wrap(()->{
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
        }).get();
    }
}
