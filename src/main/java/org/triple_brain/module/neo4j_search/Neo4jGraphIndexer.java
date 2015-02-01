/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.module.neo4j_search;

import org.triple_brain.module.model.graph.GraphElement;
import org.triple_brain.module.model.graph.edge.Edge;
import org.triple_brain.module.model.graph.schema.SchemaPojo;
import org.triple_brain.module.model.graph.vertex.VertexOperator;
import org.triple_brain.module.search.GraphIndexer;

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
