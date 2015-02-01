/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.rest.graphdb.query.QueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.triple_brain.module.model.User;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Neo4jFriendlyResource;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Neo4jRestApiUtils;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Relationships;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.Neo4jGraphElementFactory;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceQueryBuilder;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import org.triple_brain.module.search.GraphElementSearchResult;
import org.triple_brain.module.search.GraphSearch;
import org.triple_brain.module.search.VertexSearchResult;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.triple_brain.module.neo4j_graph_manipulator.graph.Neo4jRestApiUtils.map;

public class Neo4jGraphSearch implements GraphSearch {

    @Inject
    QueryEngine queryEngine;

    @Inject
    GraphDatabaseService graphDatabaseService;

    @Inject
    Neo4jGraphElementFactory graphElementFactory;

    @Inject
    ReadableIndex<Node> nodeIndex;

    @Override
    public List<VertexSearchResult> searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(String label, User user) {
        return null;
    }

    @Override
    public List<VertexSearchResult> searchOnlyForOwnVerticesOrSchemasForAutoCompletionByLabel(String label, User user) {
        return null;
    }

    @Override
    public List<VertexSearchResult> searchOnlyForOwnVerticesForAutoCompletionByLabel(String searchTerm, User user) {
        String query = "START node=node:node_auto_index('" +
                Neo4jFriendlyResource.props.label + ":(" + formatSearchTerm(searchTerm) + "*) AND " +
                "owner:" + user.username() + "') " +
                "MATCH (node:vertex) " +
                "OPTIONAL MATCH (node)<-[:r]->(relation_node) " +
                "RETURN " +
                FriendlyResourceQueryBuilder.returnQueryPartUsingPrefix("node") +
                "labels(node) as type";
        QueryResult<Map<String, Object>> results = queryEngine.query(
                query,
                Neo4jRestApiUtils.map(
                        "owner", user.username()
                )
        );
        List<VertexSearchResult> searchResults = new ArrayList<>();
        for (Map<String, Object> row : results) {
            VertexSearchResult searchResult = new VertexSearchResult(
                    GraphElementFromExtractorQueryRow.usingRowAndKey(row, "node").build()
            );
            searchResults.add(
                    searchResult
            );
        }
        return searchResults;
    }

    private String formatSearchTerm(String searchTerm) {
        return searchTerm.replace(" ", " AND ");
    }

    @Override
    public List<GraphElementSearchResult> searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(String label, User user) {
        return null;
    }

    @Override
    public GraphElementSearchResult getByUri(URI uri, User user) {
        return null;
    }
}
