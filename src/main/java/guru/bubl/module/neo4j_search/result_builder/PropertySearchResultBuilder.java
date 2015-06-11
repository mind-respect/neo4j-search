/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search.result_builder;

import guru.bubl.module.model.graph.schema.SchemaPojo;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import guru.bubl.module.search.GraphElementSearchResult;
import guru.bubl.module.search.PropertySearchResult;

import java.util.Map;

public class PropertySearchResultBuilder implements SearchResultBuilder {

    private Map<String, Object> row;
    private String prefix;

    public PropertySearchResultBuilder(Map<String, Object> row, String prefix){
        this.row = row;
        this.prefix = prefix;
    }

    @Override
    public GraphElementSearchResult build() {
        return PropertySearchResult.forPropertyAndSchema(
                GraphElementFromExtractorQueryRow.usingRowAndKey(row, prefix).build(),
                new SchemaPojo(
                        RelatedGraphElementExtractor.fromResourceProperties(
                                RelatedGraphElementExtractor.getListOfPropertiesFromRow(row).get(0)
                        ).get()
                )
        );
    }
}
