/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search.result_builder;

import org.triple_brain.module.model.graph.edge.EdgePojo;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.EdgeFromExtractorQueryRow;
import org.triple_brain.module.search.EdgeSearchResult;
import org.triple_brain.module.search.GraphElementSearchResult;

import java.util.Map;

public class RelationSearchResultBuilder implements SearchResultBuilder {

    private Map<String, Object> row;
    private String prefix;

    public RelationSearchResultBuilder(Map<String, Object> row, String prefix){
        this.row = row;
        this.prefix = prefix;
    }

    @Override
    public GraphElementSearchResult update(GraphElementSearchResult graphElementSearchResult) {
        return graphElementSearchResult;
    }

    @Override
    public GraphElementSearchResult build() {
        return new EdgeSearchResult(
                (EdgePojo) EdgeFromExtractorQueryRow.usingRowAndKey(row, prefix).build()
        );
    }
}
