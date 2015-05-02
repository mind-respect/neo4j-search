/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package org.triple_brain.module.neo4j_search.result_builder;

import org.triple_brain.module.search.GraphElementSearchResult;

public interface SearchResultBuilder {
    GraphElementSearchResult build();
}
