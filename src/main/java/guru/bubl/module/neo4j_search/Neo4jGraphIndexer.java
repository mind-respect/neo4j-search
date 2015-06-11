/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search;

import guru.bubl.module.model.graph.GraphElement;
import guru.bubl.module.model.graph.edge.Edge;
import guru.bubl.module.model.graph.schema.SchemaPojo;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import guru.bubl.module.search.GraphIndexer;

public class Neo4jGraphIndexer implements GraphIndexer{

    @Override
    public void indexWholeGraph() {

    }

    @Override
    public void indexVertex(VertexOperator vertex) {

    }

    @Override
    public void indexRelation(Edge edge) {

    }

    @Override
    public void indexSchema(SchemaPojo schema) {

    }

    @Override
    public void deleteGraphElement(GraphElement graphElement) {

    }

    @Override
    public void commit() {

    }
}
