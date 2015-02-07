/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.rest.graphdb.query.QueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.triple_brain.module.model.User;
import org.triple_brain.module.model.graph.FriendlyResourcePojo;
import org.triple_brain.module.model.graph.GraphElementPojo;
import org.triple_brain.module.model.graph.edge.EdgePojo;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Neo4jFriendlyResource;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Neo4jRestApiUtils;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.Neo4jGraphElementFactory;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.edge.Neo4jEdgeOperator;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceFromExtractorQueryRow;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceQueryBuilder;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.EdgeFromExtractorQueryRow;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.Neo4jSubGraphExtractor;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.schema.Neo4jSchemaOperator;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.vertex.Neo4jVertexInSubGraphOperator;
import org.triple_brain.module.neo4j_search.result_builder.RelationSearchResultBuilder;
import org.triple_brain.module.neo4j_search.result_builder.SchemaSearchResultBuilder;
import org.triple_brain.module.neo4j_search.result_builder.SearchResultBuilder;
import org.triple_brain.module.neo4j_search.result_builder.VertexSearchResultBuilder;
import org.triple_brain.module.search.EdgeSearchResult;
import org.triple_brain.module.search.GraphElementSearchResult;
import org.triple_brain.module.search.GraphSearch;
import org.triple_brain.module.search.VertexSearchResult;

import javax.inject.Inject;
import java.net.URI;
import java.util.*;

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
    public List<VertexSearchResult> searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(String searchTerm, User user) {
        return new Getter<VertexSearchResult>().get(
                searchTerm,
                false,
                user.username(),
                Neo4jVertexInSubGraphOperator.NEO4J_LABEL_NAME,
                Neo4jSchemaOperator.NEO4J_LABEL_NAME
        );
    }

    @Override
    public List<VertexSearchResult> searchOnlyForOwnVerticesOrSchemasForAutoCompletionByLabel(String label, User user) {
        return null;
    }

    @Override
    public List<VertexSearchResult> searchOnlyForOwnVerticesForAutoCompletionByLabel(String searchTerm, User user) {
        return new Getter<VertexSearchResult>().get(
                searchTerm,
                true,
                user.username(),
                Neo4jVertexInSubGraphOperator.NEO4J_LABEL_NAME
        );
    }

    @Override
    public List<GraphElementSearchResult> searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(String searchTerm, User user) {
        return new Getter<GraphElementSearchResult>().get(
                searchTerm,
                false,
                user.username(),
                Neo4jSchemaOperator.NEO4J_LABEL_NAME,
                Neo4jSchemaOperator.NEO4J_PROPERTY_LABEL_NAME,
                Neo4jEdgeOperator.NEO4J_LABEL_NAME
        );
    }

    @Override
    public GraphElementSearchResult getByUri(URI uri, User user) {
        return null;
    }

    private class Getter<ResultType extends GraphElementSearchResult> {
        private final String nodePrefix = "node";
        private List<ResultType> searchResults = new ArrayList<>();

        public List<ResultType> get(
                String searchTerm,
                Boolean forPersonal,
                String username,
                String... graphElementTypes
        ) {
            QueryResult<Map<String, Object>> rows = queryEngine.query(
                    buildQuery(
                            searchTerm,
                            forPersonal,
                            username,
                            graphElementTypes
                    ),
                    Neo4jRestApiUtils.map(
                            "owner", username
                    )
            );
            for (Map<String, Object> row : rows) {
                addOrUpdateResult(row);
            }
            return searchResults;
        }

        private void addOrUpdateResult(Map<String, Object> row) {
            printRow(row);
            SearchResultBuilder searchResultBuilder = getFromRow(row);
            if (isForUpdate(row)) {
                GraphElementSearchResult graphElementSearchResult = searchResultBuilder.update(
                        getLastAddedResult()
                );
                searchResults.set(
                        searchResults.size() - 1,
                        (ResultType) graphElementSearchResult
                );
            } else {
                GraphElementSearchResult graphElementSearchResult = searchResultBuilder.build();
                if(hasRelatedNodes(row)){
                    graphElementSearchResult =searchResultBuilder.update(graphElementSearchResult);
                }
                searchResults.add(
                        (ResultType) graphElementSearchResult
                );
            }
        }

        private Boolean hasRelatedNodes(Map<String, Object> row){
            return row.get("related_node.uri") != null;
        }

        private SearchResultBuilder getFromRow(Map<String, Object> row) {
            String resultType = nodeTypeInRow(row);
            switch (resultType) {
                case Neo4jVertexInSubGraphOperator.NEO4J_LABEL_NAME:
                    return new VertexSearchResultBuilder(row, nodePrefix);
                case Neo4jEdgeOperator.NEO4J_LABEL_NAME:
                    return new RelationSearchResultBuilder(row, nodePrefix);
                case Neo4jSchemaOperator.NEO4J_LABEL_NAME:
                    return new SchemaSearchResultBuilder(row, nodePrefix);
            }
            throw new RuntimeException("result type " + resultType + " does not exist");
        }

        private void printRow(Map<String, Object> row) {
            System.out.println("*************printing row*****************");
            for (String key : row.keySet()) {
                System.out.println(key + " " + row.get(key));
            }
        }

        private String nodeTypeInRow(Map<String, Object> row) {
            String resultType = row.get("type").toString();
            return resultType.substring(1, resultType.length() - 1);
        }


        private Boolean isForUpdate(Map<String, Object> row) {
            return !searchResults.isEmpty() && getUriInRow(row).equals(
                    getLastAddedResult().getGraphElement().uri()
            );
        }

        private ResultType getLastAddedResult() {
            return searchResults.get(searchResults.size() - 1);
        }

        private URI getUriInRow(Map<String, Object> row) {
            return FriendlyResourceFromExtractorQueryRow.usingRowAndPrefix(
                    row,
                    "node"
            ).getUri();
        }

        private String buildQuery(
                String searchTerm,
                Boolean forPersonal,
                String username,
                String... graphElementTypes
        ) {
            return "START node=node:node_auto_index('" +
                    Neo4jFriendlyResource.props.label + ":(" + formatSearchTerm(searchTerm) + "*) AND " +
                    (forPersonal ? "owner:" + username : "(is_public:true OR owner:" + username + ")") + " AND " +
                    "( " + Neo4jFriendlyResource.props.type + ":" + StringUtils.join(graphElementTypes, " OR type:") + ") " +
                    "') " +
                    "OPTIONAL MATCH node<-[]->related_node " +
                    "RETURN " +
                    FriendlyResourceQueryBuilder.returnQueryPartUsingPrefix("node") +
                    Neo4jSubGraphExtractor.edgeSpecificPropertiesQueryPartUsingPrefix("node") +
                    "related_node.label, related_node.uri, " +
                    "labels(node) as type";
        }

        private String formatSearchTerm(String searchTerm) {
            return searchTerm.replace(" ", " AND ");
        }

    }
}
