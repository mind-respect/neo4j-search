/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search.result_builder;

import guru.bubl.module.model.graph.FriendlyResourcePojo;
import guru.bubl.module.model.graph.GraphElementPojo;
import guru.bubl.module.neo4j_graph_manipulator.graph.Relationships;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class RelatedGraphElementExtractor {

    List<String> properties;

    public static RelatedGraphElementExtractor fromResourceProperties(List<String> properties) {
        return new RelatedGraphElementExtractor(
                properties
        );
    }

    public static List<List<String>> getListOfPropertiesFromRow(ResultSet row)throws SQLException{
        return (List) row.getObject("related_nodes");
    }

    protected RelatedGraphElementExtractor(List<String> properties) {
        this.properties = properties;
    }

    public GraphElementPojo get() {
        String label = properties.get(0);
        if (label == null) {
            label = "";
        }
        return new GraphElementPojo(new FriendlyResourcePojo(
                URI.create(
                        properties.get(1)
                ),
                label
        ));
    }

    public Boolean hasResource(){
        return properties.get(1) != null;
    }

    public Relationships getRelationship() {
        return Relationships.valueOf(
                getRelationName()
        );
    }

    private String getRelationName() {
        return properties.get(2);
    }

}
