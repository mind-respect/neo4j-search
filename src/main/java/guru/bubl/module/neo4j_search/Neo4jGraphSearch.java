/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search;

import guru.bubl.module.common_utils.NoExRun;
import guru.bubl.module.model.User;
import guru.bubl.module.model.graph.GraphElementPojo;
import guru.bubl.module.model.graph.GraphElementType;
import guru.bubl.module.model.graph.IdentificationPojo;
import guru.bubl.module.neo4j_graph_manipulator.graph.Neo4jFriendlyResource;
import guru.bubl.module.neo4j_graph_manipulator.graph.Relationships;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.Neo4jGraphElementFactory;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceQueryBuilder;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.IdentificationQueryBuilder;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import guru.bubl.module.neo4j_search.result_builder.*;
import guru.bubl.module.search.GraphElementSearchResult;
import guru.bubl.module.search.GraphElementSearchResultPojo;
import guru.bubl.module.search.GraphSearch;
import guru.bubl.module.search.VertexSearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.ReadableIndex;

import javax.inject.Inject;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jGraphSearch implements GraphSearch {

    @Inject
    GraphDatabaseService graphDatabaseService;

    @Inject
    Neo4jGraphElementFactory graphElementFactory;

    @Inject
    ReadableIndex<Node> nodeIndex;

    @Inject
    Connection connection;


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
        return new Getter().getForUri(
                uri,
                user.username()
        );
    }

    @Override
    public List<VertexSearchResult> searchPublicVerticesOnly(String searchTerm) {
        return new Getter<VertexSearchResult>().get(
                searchTerm,
                false,
                "",
                GraphElementType.vertex,
                GraphElementType.schema
        );
    }

    @Override
    public GraphElementSearchResult getDetailsAnonymously(URI uri) {
        return new Getter().getForUri(
                uri,
                ""
        );
    }

    private class Getter<ResultType extends GraphElementSearchResult> {
        private final String nodePrefix = "node";
        private List<ResultType> searchResults = new ArrayList<>();

        public GraphElementSearchResult getForUri(URI uri, String username) {
            String query = String.format(
                    "START node=node:node_auto_index('uri:%s AND (is_public:true %s)') " +
                            "OPTIONAL MATCH (node)-[%s:%s]->(%s) " +
                            "RETURN %s%s%snode.type as type",
                    uri,
                    StringUtils.isEmpty(username) ? "" : " OR owner:" + username,
                    IdentificationQueryBuilder.IDENTIFICATION_RELATION_QUERY_KEY,
                    Relationships.IDENTIFIED_TO,
                    IdentificationQueryBuilder.IDENTIFICATION_QUERY_KEY,
                    FriendlyResourceQueryBuilder.returnQueryPartUsingPrefix("node"),
                    FriendlyResourceQueryBuilder.imageReturnQueryPart("node"),
                    IdentificationQueryBuilder.identificationReturnQueryPart()
            );

            return NoExRun.wrap(()->{
                ResultSet rs = connection.createStatement().executeQuery(query);
                if(!rs.next()){
                    return null;
                }
                return new GraphElementSearchResultPojo(
                        setupGraphElementForDetailedResult(
                                GraphElementFromExtractorQueryRow.usingRowAndKey(
                                        rs,
                                        "node"
                                ).build()
                        ),
                        GraphElementType.valueOf(
                                nodeTypeInRow(rs)
                        )
                );
            }).get();
        }


        private GraphElementPojo setupGraphElementForDetailedResult(GraphElementPojo graphElement) {
            if (graphElement.gotImages()) {
                graphElement.removeAllIdentifications();
                return graphElement;
            }
            for (IdentificationPojo identification : graphElement.getIdentifications().values()) {
                if (identification.gotImages()) {
                    graphElement.addImage(
                            identification.images().iterator().next()
                    );
                    graphElement.removeAllIdentifications();
                    return graphElement;
                }
            }
            graphElement.removeAllIdentifications();
            return graphElement;
        }

        public List<ResultType> get(
                String searchTerm,
                Boolean forPersonal,
                String username,
                GraphElementType... graphElementTypes
        ) {
            return getUsingQuery(
                    buildQuery(
                            searchTerm,
                            forPersonal,
                            username,
                            graphElementTypes
                    )
            );
        }

        private List<ResultType> getUsingQuery(String query) {
            return NoExRun.wrap(()->{
                ResultSet rs = connection.createStatement().executeQuery(query);
                while(rs.next()){
                    addResult(rs);
                }
                return searchResults;
            }).get();
        }

        private void addResult(ResultSet row) throws SQLException{
//            printRow(row);
            SearchResultBuilder searchResultBuilder = getFromRow(row);
            GraphElementSearchResult graphElementSearchResult = searchResultBuilder.build();
            searchResults.add(
                    (ResultType) graphElementSearchResult
            );
        }

        private SearchResultBuilder getFromRow(ResultSet row) throws SQLException{
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
                if (key.equals("related_nodes")) {
                    List collection = (List) row.get(key);
                    System.out.println(collection);

                } else {
                    System.out.println(key + " " + row.get(key));
                }
            }
        }

        private String nodeTypeInRow(ResultSet row) throws SQLException{
            return row.getString("type");
        }

        private String buildQuery(
                String searchTerm,
                Boolean forPersonal,
                String username,
                GraphElementType... graphElementTypes
        ) {
            return "START node=node:node_auto_index('" +
                    Neo4jFriendlyResource.props.label + ":(" + formatSearchTerm(searchTerm) + "*) AND " +
                    (forPersonal ? "owner:" + username : "(is_public:true " +
                            (StringUtils.isEmpty(username) ? "" : " OR owner:" + username ) + ")") + " AND " +
                    "( " + Neo4jFriendlyResource.props.type + ":" + StringUtils.join(graphElementTypes, " OR type:") + ") " +
                    "') " +
                    "OPTIONAL MATCH node<-[relation]->related_node " +
                    "RETURN " +
                    "node.uri, node.label, node.creation_date, node.last_modification_date, " +
                    "COLLECT([related_node.label, related_node.uri, type(relation)])[0..5] as related_nodes, " +
                    "node.type as type limit 10";
        }

        private String formatSearchTerm(String searchTerm) {
            return QueryParser.escape(searchTerm).replace(
                    "\\", "\\\\"
            ).replace("'", "\\'").replace(" ", " AND ");
        }
    }
}
