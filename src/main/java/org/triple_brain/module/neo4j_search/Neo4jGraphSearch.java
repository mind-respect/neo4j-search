/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.rest.graphdb.query.QueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.triple_brain.module.model.User;
import org.triple_brain.module.model.graph.GraphElementType;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Neo4jFriendlyResource;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Neo4jRestApiUtils;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.Neo4jGraphElementFactory;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.edge.Neo4jEdgeOperator;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceFromExtractorQueryRow;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceQueryBuilder;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.Neo4jSubGraphExtractor;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.schema.Neo4jSchemaOperator;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.vertex.Neo4jVertexInSubGraphOperator;
import org.triple_brain.module.neo4j_search.result_builder.*;
import org.triple_brain.module.search.GraphElementSearchResult;
import org.triple_brain.module.search.GraphElementSearchResultPojo;
import org.triple_brain.module.search.GraphSearch;
import org.triple_brain.module.search.VertexSearchResult;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                GraphElementType.vertex,
                GraphElementType.schema
        );
    }

    @Override
    public List<VertexSearchResult> searchOnlyForOwnVerticesOrSchemasForAutoCompletionByLabel(String searchTerm, User user) {
        return new Getter<VertexSearchResult>().get(
                searchTerm,
                true,
                user.username(),
                GraphElementType.vertex,
                GraphElementType.schema
        );
    }

    @Override
    public List<VertexSearchResult> searchOnlyForOwnVerticesForAutoCompletionByLabel(String searchTerm, User user) {
        return new Getter<VertexSearchResult>().get(
                searchTerm,
                true,
                user.username(),
                GraphElementType.vertex
        );
    }

    @Override
    public List<GraphElementSearchResult> searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(String searchTerm, User user) {
        return new Getter<GraphElementSearchResult>().get(
                searchTerm,
                false,
                user.username(),
                GraphElementType.schema,
                GraphElementType.property,
                GraphElementType.edge
        );
    }

    @Override
    public GraphElementSearchResult getDetails(URI uri, User user) {
        return new Getter<GraphElementSearchResult>().getForUri(
                uri,
                user.username()
        );
    }

    private class Getter<ResultType extends GraphElementSearchResult> {
        private final String nodePrefix = "node";
        private List<ResultType> searchResults = new ArrayList<>();

        public GraphElementSearchResult getForUri(URI uri, String username){
            String query = "START node=node:node_auto_index('uri:" + uri + " AND " +
                    "owner:" + username + "') " +
                    "RETURN " +
                    FriendlyResourceQueryBuilder.returnQueryPartUsingPrefix("node") +
                    Neo4jSubGraphExtractor.edgeSpecificPropertiesQueryPartUsingPrefix("node") +
                    "labels(node) as type";

            QueryResult<Map<String, Object>> rows = queryEngine.query(
                    query,
                    Neo4jRestApiUtils.map(
                            "owner", username
                    )
            );
            Map<String, Object> row = rows.iterator().next();
            return new GraphElementSearchResultPojo(
                    GraphElementFromExtractorQueryRow.usingRowAndKey(
                            row,
                            "node"
                    ).build(),
                    GraphElementType.valueOf(
                            nodeTypeInRow(row)
                    )
            );
        }

        public List<ResultType> get(
                String searchTerm,
                Boolean forPersonal,
                String username,
                GraphElementType ... graphElementTypes
        ) {
            return getUsingQuery(
                    buildQuery(
                            searchTerm,
                            forPersonal,
                            username,
                            graphElementTypes
                    ),
                    username
            );
        }

        private List<ResultType> getUsingQuery(String query, String username){
            QueryResult<Map<String, Object>> rows = queryEngine.query(
                    query,
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
//            printRow(row);
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
            GraphElementType resultType = GraphElementType.valueOf(
                    nodeTypeInRow(row)
            );
            switch (resultType) {
                case vertex:
                    return new VertexSearchResultBuilder(row, nodePrefix);
                case edge:
                    return new RelationSearchResultBuilder(row, nodePrefix);
                case schema:
                    return new SchemaSearchResultBuilder(row, nodePrefix);
                case property:
                    return new PropertySearchResultBuilder(row, nodePrefix);
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
                GraphElementType... graphElementTypes
        ) {
            return "START node=node:node_auto_index('" +
                    Neo4jFriendlyResource.props.label + ":(" + formatSearchTerm(searchTerm) + "*) AND " +
                    (forPersonal ? "owner:" + username : "(is_public:true OR owner:" + username + ")") + " AND " +
                    "( " + Neo4jFriendlyResource.props.type + ":" + StringUtils.join(graphElementTypes, " OR type:") + ") " +
                    "') " +
                    "OPTIONAL MATCH node<-[relation]->related_node " +
                    "RETURN " +
                    "node.uri, node.label, node.creation_date, node.last_modification_date, " +
                    "related_node.label, related_node.uri, " +
                    "type(relation) as relation, " +
                    "labels(node) as type limit 10";
        }
        private String formatSearchTerm(String searchTerm) {
            return QueryParser.escape(searchTerm).replace(
                    "\\", "\\\\"
            ).replace("'","\\'").replace(" ", " AND ");
        }
    }
}
