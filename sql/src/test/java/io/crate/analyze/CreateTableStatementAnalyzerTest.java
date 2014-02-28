package io.crate.analyze;

import io.crate.sql.parser.SqlParser;
import io.crate.sql.tree.Statement;
import org.junit.Test;

public class CreateTableStatementAnalyzerTest {

    @Test
    public void testConversionToIndexMapping() throws Exception {
        Statement statement = SqlParser.createStatement(
                "create table test (id integer primary key, names array(string), name string) " +
                        "clustered into 3 shards replicas 0");

        CreateTableStatementAnalyzer statementAnalyzer = new CreateTableStatementAnalyzer();

        statementAnalyzer.process(statement, null);
    }
}
