/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search;

public class GraphIndexerTest extends Neo4jSearchRelatedTest {
/*
    @Test
    public void can_index_vertex() throws Exception {
        SolrDocumentList documentList = queryVertex(vertexA);
        assertThat(documentList.size(), is(0));
        graphIndexer.indexVertex(vertexA);
        graphIndexer.commit();
        documentList = queryVertex(vertexA);
        assertThat(documentList.size(), is(1));
        assertThat(
                labelOfGraphElementSearchResult(documentList.get(0)),
                is("vertex Azure")
        );
    }

    @Test
    public void can_remove_graph_element_from_index() {
        indexGraph();
        GraphSearch graphSearch = SolrGraphSearch.withCoreContainer(coreContainer);
        List<VertexSearchResult> results = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vertex azure",
                user
        );
        assertThat(results.size(), is(1));
        graphIndexer.deleteGraphElement(vertexA);
        graphIndexer.commit();
        results = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vertex azure",
                user
        );
        assertThat(results.size(), is(0));
    }

    @Test
    public void schemas_are_indexed_when_indexing_whole_graph(){
        GraphSearch graphSearch = SolrGraphSearch.withCoreContainer(coreContainer);
        List<VertexSearchResult> results = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema1",
                user
        );
        assertTrue(results.isEmpty());
        SchemaOperator schema = createSchema(user);
        schema.label("schema1");
        graphIndexer.indexWholeGraph();
        graphIndexer.commit();
        results = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "schema1",
                user
        );
        assertThat(results.size(), is(1));
    }

    @Test
    public void vertices_are_indexed_when_indexing_whole_graph(){
        GraphSearch graphSearch = SolrGraphSearch.withCoreContainer(coreContainer);
        List<VertexSearchResult> results = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vertex azure",
                user
        );
        assertTrue(results.isEmpty());
        graphIndexer.indexWholeGraph();
        graphIndexer.commit();
        results = graphSearch.searchSchemasOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vertex azure",
                user
        );
        assertFalse(results.isEmpty());
    }

    @Test
    public void edges_are_indexed_when_indexing_whole_graph(){
        GraphSearch graphSearch = SolrGraphSearch.withCoreContainer(coreContainer);
        List<GraphElementSearchResult> results = graphSearch.searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(
                "between vert",
                user
        );
        assertTrue(results.isEmpty());
        graphIndexer.indexWholeGraph();
        graphIndexer.commit();
        results = graphSearch.searchRelationsPropertiesOrSchemasForAutoCompletionByLabel(
                "between vert",
                user
        );
        assertFalse(results.isEmpty());
    }

    @Test
    @Ignore(
            "todo when lucene4 be integrated in noe4j " +
                    "for now it conflicts with solr " +
                    "when I upgrade solr to version 4"
    )
    public void indexing_graph_element_doesnt_erase_vertex_specific_fields() {
        indexGraph();
        GraphSearch graphSearch = SolrGraphSearch.withCoreContainer(coreContainer);
        List<VertexSearchResult> vertexASearchResults = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                "vertex Azure",
                user
        );
        assertThat(
                vertexASearchResults.size(), is(1)
        );
        //todo uncomment when lucene4 be integrated in noe4j for now it conflicts with solr when I upgrade solr to version 4
//        graphIndexer().updateGraphElementIndex(vertexA, user);
        vertexASearchResults = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                "vertex Azure",
                user
        );
        assertThat(
                vertexASearchResults.size(), is(1)
        );
    }

    private String labelOfGraphElementSearchResult(SolrDocument solrDocument) {
        return (String) solrDocument.getFieldValue("label");
    }

    private SolrDocumentList queryVertex(Vertex vertex) throws Exception {
        return resultsOfSearchQuery(
                new SolrQuery().setQuery(
                        "uri:" + encodeURL(vertex.uri().toString())
                )
        );
    }
    */
}