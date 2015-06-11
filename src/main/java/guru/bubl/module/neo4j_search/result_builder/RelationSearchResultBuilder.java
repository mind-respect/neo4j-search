/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search.result_builder;

import guru.bubl.module.model.graph.edge.EdgePojo;
import guru.bubl.module.model.graph.vertex.VertexInSubGraphPojo;
import guru.bubl.module.neo4j_graph_manipulator.graph.Relationships;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import guru.bubl.module.search.EdgeSearchResult;
import guru.bubl.module.search.GraphElementSearchResult;

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
        List<List<String>> propertiesList = RelatedGraphElementExtractor.getListOfPropertiesFromRow(
                row
        );
        for(List<String> properties : propertiesList){
            RelatedGraphElementExtractor friendlyResourceExtractor = RelatedGraphElementExtractor.fromResourceProperties(
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
