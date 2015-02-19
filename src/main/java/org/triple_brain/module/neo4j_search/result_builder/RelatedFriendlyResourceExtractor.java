/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search.result_builder;

import org.triple_brain.module.model.graph.FriendlyResourcePojo;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Relationships;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class RelatedFriendlyResourceExtractor {

    List<String> properties;

    public static RelatedFriendlyResourceExtractor fromResourceProperties(List<String> properties) {
        return new RelatedFriendlyResourceExtractor(
                properties
        );
    }

    public static List<List<String>> getListOfPropertiesFromRow(Map<String, Object> row){
        return (List) row.get("related_nodes");
    }

    protected RelatedFriendlyResourceExtractor(List<String> properties) {
        this.properties = properties;
    }

    public FriendlyResourcePojo get() {
        String label = properties.get(0);
        if (label == null) {
            label = "";
        }
        return new FriendlyResourcePojo(
                URI.create(
                        properties.get(1)
                ),
                label
        );
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
