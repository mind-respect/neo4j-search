/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search;

import org.junit.Before;
import org.triple_brain.module.model.User;
import org.triple_brain.module.model.graph.AdaptableGraphComponentTest;
import org.triple_brain.module.model.graph.GraphFactory;
import org.triple_brain.module.model.graph.schema.SchemaOperator;
import org.triple_brain.module.model.graph.vertex.Vertex;
import org.triple_brain.module.model.graph.vertex.VertexOperator;
import org.triple_brain.module.model.test.scenarios.TestScenarios;
import org.triple_brain.module.model.test.scenarios.VerticesCalledABAndC;
import org.triple_brain.module.search.GraphIndexer;
import org.triple_brain.module.search.GraphSearch;

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
        );
        user2 = User.withEmail(
                "test@example.org"
        );
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