/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search.result_builder;

import org.triple_brain.module.model.graph.edge.EdgePojo;
import org.triple_brain.module.model.graph.vertex.VertexInSubGraphPojo;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Relationships;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceFromExtractorQueryRow;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import org.triple_brain.module.search.EdgeSearchResult;
import org.triple_brain.module.search.GraphElementSearchResult;

import java.util.Map;

public class RelationSearchResultBuilder implements SearchResultBuilder {

    private Map<String, Object> row;
    private String prefix;

    public RelationSearchResultBuilder(Map<String, Object> row, String prefix) {
        this.row = row;
        this.prefix = prefix;
    }

    @Override
    public GraphElementSearchResult update(GraphElementSearchResult graphElementSearchResult) {
        EdgeSearchResult searchResult = (EdgeSearchResult) graphElementSearchResult;
        VertexInSubGraphPojo vertex = new VertexInSubGraphPojo(
                FriendlyResourceFromExtractorQueryRow.usingRowAndNodeKey(
                        row,
                        "related_node"
                ).build()
        );
        EdgePojo edge = searchResult.getEdge();
        if(isDestinationVertex()){
            edge.setDestinationVertex(vertex);
        }else{
            edge.setSourceVertex(vertex);
        }
        return searchResult;
    }

    @Override
    public GraphElementSearchResult build() {
        return new EdgeSearchResult(
                new EdgePojo(
                        GraphElementFromExtractorQueryRow.usingRowAndKey(
                                row,
                                prefix
                        ).build()
                )
        );
    }

    private Boolean isDestinationVertex(){
        return Relationships.DESTINATION_VERTEX.name().equals(
                getRelationName()
        );
    }

    private String getRelationName(){
        return row.get("relation").toString();
    }
}
