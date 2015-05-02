/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package org.triple_brain.module.neo4j_search.result_builder;

import org.triple_brain.module.model.graph.edge.EdgePojo;
import org.triple_brain.module.model.graph.vertex.VertexInSubGraphPojo;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Relationships;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import org.triple_brain.module.search.EdgeSearchResult;
import org.triple_brain.module.search.GraphElementSearchResult;

import java.util.List;
import java.util.Map;

public class RelationSearchResultBuilder implements SearchResultBuilder {

    private Map<String, Object> row;
    private String prefix;

    public RelationSearchResultBuilder(Map<String, Object> row, String prefix) {
        this.row = row;
        this.prefix = prefix;
    }



    @Override
    public GraphElementSearchResult build() {
        EdgeSearchResult searchResult = new EdgeSearchResult(
                new EdgePojo(
                        GraphElementFromExtractorQueryRow.usingRowAndKey(
                                row,
                                prefix
                        ).build()
                )
        );
        List<List<String>> propertiesList = RelatedFriendlyResourceExtractor.getListOfPropertiesFromRow(
                row
        );
        for(List<String> properties : propertiesList){
            RelatedFriendlyResourceExtractor friendlyResourceExtractor = RelatedFriendlyResourceExtractor.fromResourceProperties(
                    properties
            );
            VertexInSubGraphPojo vertex = new VertexInSubGraphPojo(
                    friendlyResourceExtractor.get()
            );
            EdgePojo edge = searchResult.getEdge();
            if(isDestinationVertex(friendlyResourceExtractor.getRelationship())){
                edge.setDestinationVertex(vertex);
            }else{
                edge.setSourceVertex(vertex);
            }
        }
        return searchResult;
    }

    private Boolean isDestinationVertex(Relationships relationShip){
        return Relationships.DESTINATION_VERTEX == relationShip;
    }
}
