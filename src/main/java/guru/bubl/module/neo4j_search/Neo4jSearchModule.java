/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search;

import com.google.inject.AbstractModule;
import guru.bubl.module.search.GraphIndexer;
import guru.bubl.module.search.GraphSearch;

public class Neo4jSearchModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GraphIndexer.class).to(Neo4jGraphIndexer.class);
        bind(GraphSearch.class).to(Neo4jGraphSearch.class);
    }
}
