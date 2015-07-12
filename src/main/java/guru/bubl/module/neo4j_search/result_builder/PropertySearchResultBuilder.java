/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_search.result_builder;

import guru.bubl.module.common_utils.NoExRun;
import guru.bubl.module.model.graph.schema.SchemaPojo;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import guru.bubl.module.search.GraphElementSearchResult;
import guru.bubl.module.search.PropertySearchResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class PropertySearchResultBuilder implements SearchResultBuilder {

    private ResultSet row;
    private String prefix;

    public PropertySearchResultBuilder(ResultSet row, String prefix) {
        this.row = row;
        this.prefix = prefix;
    }

    @Override
    public GraphElementSearchResult build() {
        return NoExRun.wrap(() ->
                PropertySearchResult.forPropertyAndSchema(
                        GraphElementFromExtractorQueryRow.usingRowAndKey(row, prefix).build(),
                        new SchemaPojo(
                                RelatedGraphElementExtractor.fromResourceProperties(
                                        RelatedGraphElementExtractor.getListOfPropertiesFromRow(row).get(0)
                                ).get()
                        )
                )).get();
    }
}
