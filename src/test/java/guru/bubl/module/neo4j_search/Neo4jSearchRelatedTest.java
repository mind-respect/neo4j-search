/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search;

import org.junit.Before;
import guru.bubl.module.model.User;
import guru.bubl.module.model.graph.AdaptableGraphComponentTest;
import guru.bubl.module.model.graph.GraphFactory;
import guru.bubl.module.model.graph.schema.SchemaOperator;
import guru.bubl.module.model.graph.vertex.Vertex;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import guru.bubl.module.model.test.scenarios.TestScenarios;
import guru.bubl.module.model.test.scenarios.VerticesCalledABAndC;
import guru.bubl.module.search.GraphIndexer;
import guru.bubl.module.search.GraphSearch;

import javax.inject.Inject;

public class Neo4jSearchRelatedTest extends AdaptableGraphComponentTest{

    @Inject
    GraphFactory graphMaker;


    GraphIndexer graphIndexer;


    GraphSearch graphSearch;

    @Inject
    protected TestScenarios testScenarios;

    protected VertexOperator vertexA;
    protected VertexOperator vertexB;
    protected VertexOperator vertexC;
    protected VertexOperator pineApple;
    protected User user;
    protected User user2;
    protected Vertex vertexOfUser2;


    @Before
    public void beforeSearchRelatedTest() throws Exception{
        graphSearch = AdaptableGraphComponentTest.injector.getInstance(Neo4jGraphSearch.class);
        graphIndexer = AdaptableGraphComponentTest.injector.getInstance(Neo4jGraphIndexer.class);
        user = User.withEmail(
                "test@2example.org"
        ).setUsername("test2");
        user2 = User.withEmail(
                "test@example.org"
        ).setUsername("test");
        deleteAllDocs();
        makeGraphHave3SerialVerticesWithLongLabels();
        vertexOfUser2 = graphMaker.createForUser(user2).defaultVertex();
        pineApple = testScenarios.addPineAppleVertexToVertex(vertexC);
    }

    protected void makeGraphHave3SerialVerticesWithLongLabels() throws Exception {
        VerticesCalledABAndC vertexABAndC = testScenarios.makeGraphHave3SerialVerticesWithLongLabels(
                graphMaker.createForUser(user)
        );
        vertexA = vertexABAndC.vertexA();
        vertexB = vertexABAndC.vertexB();
        vertexC = vertexABAndC.vertexC();
    }

    protected void deleteAllDocs()throws Exception{
    }


    protected void indexGraph(){

    }

    protected void indexVertex(VertexOperator vertex){

    }
    protected SchemaOperator createSchema(User user) {
        return userGraph.schemaOperatorWithUri(
                graphMaker.loadForUser(user).createSchema().uri()
        );
    }
}