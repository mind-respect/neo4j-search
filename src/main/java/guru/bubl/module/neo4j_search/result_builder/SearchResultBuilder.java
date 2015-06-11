/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search.result_builder;

import guru.bubl.module.search.GraphElementSearchResult;

public interface SearchResultBuilder {
    GraphElementSearchResult build();
}
