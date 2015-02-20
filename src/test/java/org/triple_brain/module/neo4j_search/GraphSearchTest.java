/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.triple_brain.module.model.Image;
import org.triple_brain.module.model.graph.GraphElement;
import org.triple_brain.module.model.graph.GraphElementOperator;
import org.triple_brain.module.model.graph.GraphElementPojo;
import org.triple_brain.module.model.graph.edge.Edge;
import org.triple_brain.module.model.graph.schema.SchemaOperator;
import org.triple_brain.module.model.graph.vertex.Vertex;
import org.triple_brain.module.search.EdgeSearchResult;
import org.triple_brain.module.search.GraphElementSearchResult;
import org.triple_brain.module.search.PropertySearchResult;
import org.triple_brain.module.search.VertexSearchResult;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
public class GraphSearchTest extends Neo4jSearchRelatedTest {

    @Test
    public void can_search_vertices_for_auto_completion() throws Exception {
        indexGraph();
        indexVertex(pineApple);
        List<VertexSearchResult> vertices;
        vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel("vert", user);
        assertThat(vertices.size(), is(3));
        vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel("vertex Cad", user);
        assertThat(vertices.size(), is(1));
        GraphElement firstVertex = vertices.get(0).getGraphElementSearchResult().getGraphElement();
        assertThat(firstVertex.label(), is("vertex Cadeau"));
        vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel("pine A", user);
        assertThat(vertices.size(), is(1));
    }

    @Test
    public void cant_search_in_vertices_of_another_user() throws Exception {
        indexGraph();
        indexVertex(pineApple);
        List<VertexSearchResult> vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                "vert",
                user
        );
        assertTrue(vertices.size() > 0);
        vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                "vert",
                user2
        );
        assertFalse(vertices.size() > 0);
    }

    @Test
    public void vertex_comment_is_not_included_in_search_result() throws Exception {
        vertexA.comment("A description");
        indexGraph();
        List<VertexSearchResult> searchResults = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                vertexA.label(),
                user
        );
        GraphElement vertex = searchResults.get(0).getGraphElementSearchResult().getGraphElement();
        assertThat(
                vertex.comment(),
                is("")
        );
    }

    @Test
    public void can_search_for_other_users_public_vertices() {
        indexGraph();
        List<VertexSearchResult> vertices = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vert",
                user2
        );
        assertTrue(vertices.isEmpty());
        vertexA.makePublic();
        indexVertex(vertexA);
        vertices = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vert",
                user2
        );
        assertFalse(vertices.isEmpty());
    }


    @Test
    public void searching_for_own_vertices_only_does_not_return_vertices_of_other_users() {
        vertexA.makePublic();
        indexGraph();
        List<VertexSearchResult> vertices = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vert",
                user2
        );
        assertTrue(vertices.size() > 0);
        vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                "vert",
                user2
        );
        assertFalse(vertices.size() > 0);
    }

    @Test
    public void searching_for_own_vertices_does_not_return_schemas() {
        SchemaOperator schema = createSchema(user);
        schema.label("schema1");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        graphIndexer.commit();
        List<VertexSearchResult> searchResult = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema",
                user
        );
        assertFalse(searchResult.isEmpty());
        searchResult = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                "schema",
                user
        );
        assertTrue(searchResult.isEmpty());
    }

    @Test
    public void search_is_case_insensitive() {
        indexGraph();
        List<VertexSearchResult> vertices = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vert",
                user
        );
        assertTrue(vertices.size() > 0);
        vertices = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "Vert",
                user
        );
        assertTrue(vertices.size() > 0);
    }

    @Test
    public void case_is_preserved_when_getting_label() {
        vertexA.label("Vertex Azure");
        indexGraph();
        List<VertexSearchResult> vertices = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vertex azure",
                user
        );
        GraphElement vertex = vertices.get(0).getGraphElementSearchResult().getGraphElement();
        assertThat(
                vertex.label(),
                is("Vertex Azure")
        );
    }

    @Test
    public void search_goes_beyond_two_first_words() {
        vertexA.label(
                "bonjour monsieur proute"
        );
        vertexB.label(
                "bonjour monsieur pratte"
        );
        vertexC.label(
                "bonjour monsieur avion"
        );
        indexGraph();

        List<VertexSearchResult> vertices = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "bonjour monsieur pr",
                user
        );
        assertThat(vertices.size(), is(2));
    }

    @Test
    public void can_search_relations() {
        indexGraph();
        List<GraphElementSearchResult> results = graphSearch.searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(
                "between vert",
                user
        );
        assertThat(results.size(), is(2));
    }

    @Test
    public void relation_source_and_destination_vertex_label_and_uri_are_included_in_result() {
        indexGraph();
        List<GraphElementSearchResult> relations = graphSearch.searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(
                "between vertex A and B",
                user
        );
        Edge edge = ((EdgeSearchResult) relations.get(0)).getEdge();
        Vertex sourceVertex = edge.sourceVertex();
        Vertex destinationVertex = edge.destinationVertex();
        assertThat(
                sourceVertex.uri(),
                is(vertexA.uri())
        );
        assertThat(
                sourceVertex.label(),
                is("vertex Azure")
        );
        assertThat(
                destinationVertex.uri(),
                is(vertexB.uri())
        );
        assertThat(
                destinationVertex.label(),
                is("vertex Bareau")
        );

        edge.sourceVertex().uri().equals(vertexA.uri());
        assertFalse(
                null == edge.sourceVertex().uri()
        );
        assertFalse(
                null == edge.destinationVertex().uri()
        );
    }

    @Test
    public void schemas_are_included_in_relations_search() {
        List<GraphElementSearchResult> results = graphSearch.searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(
                "schema1",
                user
        );
        assertTrue(results.isEmpty());
        SchemaOperator schema = createSchema(user);
        schema.label("schema1");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        graphIndexer.commit();
        results = graphSearch.searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(
                "schema1",
                user
        );
        assertFalse(results.isEmpty());
    }

    @Test
    public void can_search_schema() {
        SchemaOperator schema = createSchema(user);
        schema.label("schema1");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        graphIndexer.commit();
        List<VertexSearchResult> results = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema",
                user
        );
        assertThat(results.size(), is(1));
        GraphElementPojo schemaAsSearchResult = results.iterator().next().getGraphElement();
        assertThat(
                schemaAsSearchResult.uri(),
                is(
                        schema.uri()
                )
        );
    }

    @Test
    public void schema_properties_can_be_retrieved() throws Exception {
        SchemaOperator schema = createSchema(userGraph.user());
        schema.label("schema1");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        graphIndexer.commit();
        List<VertexSearchResult> searchResults = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema",
                userGraph.user()
        );
        VertexSearchResult result = searchResults.get(0);
        Map<URI, GraphElementPojo> properties = result.getProperties();
        assertTrue(
                properties.isEmpty()
        );
        GraphElementOperator property1 = schema.addProperty();
        property1.label("prop1");
        GraphElementOperator property2 = schema.addProperty();
        property2.label("prop2");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        graphIndexer.commit();
        searchResults = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema",
                userGraph.user()
        );
        result = searchResults.get(0);
        properties = result.getProperties();
        assertThat(
                properties.size(),
                is(2)
        );
        assertTrue(
                properties.containsKey(property1.uri())
        );
        assertTrue(
                properties.containsKey(property2.uri())
        );
    }

    @Test
    public void can_search_schema_property() {
        SchemaOperator schema = createSchema(userGraph.user());
        schema.label("schema1");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        graphIndexer.commit();
        assertTrue(
                graphSearch.searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(
                        "prop",
                        userGraph.user()
                ).isEmpty()
        );
        GraphElementOperator property1 = schema.addProperty();
        property1.label("prop1");
        GraphElementOperator property2 = schema.addProperty();
        property2.label("prop2");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        graphIndexer.commit();
        assertThat(
                graphSearch.searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(
                        "prop1",
                        userGraph.user()
                ).size(),
                is(1)
        );
    }

    @Test
    public void schema_label_and_uri_are_included_in_property_search_result(){
        SchemaOperator schema = createSchema(userGraph.user());
        schema.label("schema1");
        GraphElementOperator property1 = schema.addProperty();
        property1.label("prop1");
        PropertySearchResult searchResult = (PropertySearchResult) graphSearch.searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(
                "prop1",
                userGraph.user()
        ).get(0);
        assertThat(
                searchResult.getSchema().label(),
                is("schema1")
        );
        assertThat(
                searchResult.getSchema().uri(),
                is(schema.uri())
        );
    }

    @Test
    public void can_search_not_owned_schema() {
        SchemaOperator schema = createSchema(user);
        schema.label("schema1");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        graphIndexer.commit();
        List<VertexSearchResult> searchResults = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema",
                user2
        );
        assertFalse(searchResults.isEmpty());
    }

    @Test
    public void can_search_not_owned_schema_property() {
        SchemaOperator schema = createSchema(user);
        GraphElementOperator property1 = schema.addProperty();
        property1.label("prop");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        graphIndexer.commit();
        List<GraphElementSearchResult> searchResults = graphSearch.searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(
                "prop",
                user2
        );
        assertFalse(searchResults.isEmpty());
    }

    @Test
    public void can_search_for_only_owned_schemas() {
        SchemaOperator schema = createSchema(user);
        schema.label("schema1");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        SchemaOperator schema2 = createSchema(user2);
        schema2.label("schema2");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema2.uri()
                )
        );
        graphIndexer.commit();
        List<VertexSearchResult> searchResults = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema",
                user
        );
        assertThat(
                searchResults.size(),
                is(2)
        );

        searchResults = graphSearch.searchOnlyForOwnVerticesOrSchemasForAutoCompletionByLabel(
                "schema",
                user
        );
        assertThat(
                searchResults.size(),
                is(1)
        );
    }


    @Test
    public void schema_search_results_dont_have_comment() {
        SchemaOperator schema = createSchema(user);
        schema.label("schema1");
        schema.comment("test comment");
        graphIndexer.indexSchema(
                userGraph.schemaPojoWithUri(
                        schema.uri()
                )
        );
        graphIndexer.commit();
        VertexSearchResult result = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema",
                user
        ).get(0);
        assertThat(
                result.getGraphElement().comment(),
                is("")
        );
    }


    @Test
    public void search_queries_can_have_special_characters() {
        vertexA.label("a\\(test*");
        indexGraph();
        List<VertexSearchResult> vertices = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "a\\(test*",
                user
        );
        GraphElement vertex = vertices.get(0).getGraphElementSearchResult().getGraphElement();
        assertThat(
                vertex.label(),
                is("a\\(test*")
        );
    }

    @Test
    public void search_queries_can_have_single_quotes() {
        vertexA.label("a'test");
        List<VertexSearchResult> vertices = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "a'test",
                user
        );
        GraphElement vertex = vertices.get(0).getGraphElementSearchResult().getGraphElement();
        assertThat(
                vertex.label(),
                is("a'test")
        );
    }


    @Test
    public void does_no_have_identifications() {
        indexGraph();
        GraphElement vertex = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                vertexA.label(),
                user
        ).get(0).getGraphElementSearchResult().getGraphElement();
        assertThat(
                vertex.getIdentifications().size(),
                is(0)
        );
        vertexA.addGenericIdentification(
                modelTestScenarios.computerScientistType()
        );
        indexGraph();
        vertex = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                vertexA.label(),
                user
        ).get(0).getGraphElementSearchResult().getGraphElement();
        assertThat(
                vertex.getIdentifications().size(),
                is(0)
        );
    }

    @Test
    public void can_get_more_details_for_element_with_uri() {
        indexGraph();
        GraphElementSearchResult searchResult = graphSearch.getDetails(
                vertexA.uri(),
                user
        );
        GraphElement vertex = searchResult.getGraphElementSearchResult().getGraphElement();
        assertThat(
                vertex.label(),
                is(vertexA.label())
        );
    }

    @Test
    public void can_get_property_details() {
        SchemaOperator schema = createSchema(userGraph.user());
        GraphElementOperator property1 = schema.addProperty();
        property1.label("prop1");
        GraphElementSearchResult  searchResult = graphSearch.getDetails(
                property1.uri(),
                userGraph.user()
        );
        assertThat(
                searchResult.getGraphElement().label(),
                is("prop1")
        );
    }

    @Test
    public void more_details_contains_comment() {
        vertexA.comment("A description");
        indexGraph();
        GraphElementSearchResult searchResult= graphSearch.getDetails(
                vertexA.uri(),
                user
        );
        assertThat(
                searchResult.getGraphElement().comment(),
                is("A description")
        );
    }

    @Test
    public void more_details_contains_image(){
        GraphElementSearchResult searchResult = graphSearch.getDetails(
                vertexA.uri(),
                user
        );
        assertThat(
                searchResult.getGraphElement().images().size(),
                is(0)
        );
        Image image1 = Image.withBase64ForSmallAndUriForBigger(
                UUID.randomUUID().toString(),
                URI.create("/large_1")
        );
        vertexA.addImages(Sets.newHashSet(
                image1
        ));
        searchResult = graphSearch.getDetails(
                vertexA.uri(),
                user
        );
        assertThat(
                searchResult.getGraphElement().images().size(),
                is(1)
        );
    }

    @Test
    public void more_details_contains_identifications(){
        GraphElementSearchResult searchResult = graphSearch.getDetails(
                vertexA.uri(),
                user
        );
        assertThat(
                searchResult.getGraphElement().getIdentifications().size(),
                is(0)
        );
        vertexA.addGenericIdentification(
                modelTestScenarios.computerScientistType()
        );
        searchResult = graphSearch.getDetails(
                vertexA.uri(),
                user
        );
        assertThat(
                searchResult.getGraphElement().getIdentifications().size(),
                is(1)
        );
    }

    @Test
    public void cannot_get_by_uri_if_not_owner_element_not_public() {
        try{
            graphSearch.getDetails(
                    vertexA.uri(),
                    user2
            );
            fail();
        }catch(Exception e){

        }
    }

    @Test
    public void vertices_have_their_edges_label_and_uri_in_result(){
        VertexSearchResult searchResult = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                vertexA.label(),
                user
        ).iterator().next();
        assertThat(
                searchResult.getProperties().size(),
                is(1)
        );
        Edge edge = vertexA.getEdgeThatLinksToDestinationVertex(vertexB);
        assertThat(
                searchResult.getProperties().get(edge.uri()).label(),
                is("between vertex A and vertex B")
        );
    }

    @Test
    public void there_is_a_limit_5_related_elements(){
        SchemaOperator schema = createSchema(userGraph.user());
        schema.label("schema1");
        for(int i = 1; i <= 12; i++){
            schema.addProperty();
        }
        VertexSearchResult searchResult = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema",
                user
        ).iterator().next();
        assertThat(
                searchResult.getProperties().size(),
                is(5)
        );
    }

    @Test
    public void results_limit_is_on_number_of_results_not_number_of_related_elements(){
        SchemaOperator schema = createSchema(userGraph.user());
        schema.label("schema1");
        for(int i = 1; i <= 12; i++){
            schema.addProperty();
        }
        SchemaOperator schema2 = createSchema(userGraph.user());
        schema2.label("schema2");
        for(int i = 1; i <= 12; i++){
            schema2.addProperty();
        }
        List<VertexSearchResult> searchResult = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema",
                user
        );
        assertThat(
                searchResult.size(),
                is(2)
        );
    }

    @Test
    public void has_number_of_references_to_an_identification() {

    }


}