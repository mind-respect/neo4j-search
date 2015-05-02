/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package org.triple_brain.module.neo4j_search;

import com.google.inject.AbstractModule;
import org.triple_brain.module.search.GraphIndexer;
import org.triple_brain.module.search.GraphSearch;

public class Neo4jSearchModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GraphIndexer.class).to(Neo4jGraphIndexer.class);
        bind(GraphSearch.class).to(Neo4jGraphSearch.class);
    }
}
