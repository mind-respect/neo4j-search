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
import org.triple_brain.module.neo4j_graph_manipulator.graph.Neo4jFriendlyResource;
import org.triple_brain.module.neo4j_graph_manipulator.graph.Neo4jRestApiUtils;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.Neo4jGraphElementFactory;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.edge.Neo4jEdgeOperator;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceQueryBuilder;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.schema.Neo4jSchemaOperator;
import org.triple_brain.module.neo4j_graph_manipulator.graph.graph.vertex.Neo4jVertexInSubGraphOperator;
import org.triple_brain.module.search.GraphElementSearchResult;
import org.triple_brain.module.search.GraphSearch;
import org.triple_brain.module.search.VertexSearchResult;

import javax.inject.Inject;
import javax.xml.transform.Result;
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

    private class Getter<ResultType> {

        public List<ResultType> get(
                String searchTerm,
                Boolean forPersonal,
                String username,
                String... graphElementTypes
        ) {
            QueryResult<Map<String, Object>> results = queryEngine.query(
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
            List<ResultType> searchResults = new ArrayList<>();
            for (Map<String, Object> row : results) {
                ResultType searchResult = (ResultType) new VertexSearchResult(
                        GraphElementFromExtractorQueryRow.usingRowAndKey(row, "node").build()
                );
                searchResults.add(
                        searchResult
                );
            }
            return searchResults;
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
                    "RETURN " +
                    FriendlyResourceQueryBuilder.returnQueryPartUsingPrefix("node") +
                    "labels(node) as type";
        }
        private String formatSearchTerm(String searchTerm) {
            return searchTerm.replace(" ", " AND ");
        }
    }
}
