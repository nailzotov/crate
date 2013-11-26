package org.cratedb.module.sql.benchmark;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import org.apache.lucene.index.AtomicReaderContext;
import org.cratedb.action.GroupByFieldLookup;
import org.cratedb.action.groupby.SQLGroupingCollector;
import org.cratedb.action.sql.ParsedStatement;
import org.cratedb.service.SQLParseService;
import org.cratedb.sql.GroupByOnArrayUnsupportedException;
import org.cratedb.stubs.HitchhikerMocks;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

public class SQLGroupingCollectorBenchmark extends AbstractBenchmark {

    private static int FAKE_DOCS_LENGTH = 300000;

    private ParsedStatement stmt;
    private DummyLookup dummyLookup;
    private Object[] fakeDocs;
    private SecureRandom random = new SecureRandom();

    @Before
    public void prepare() throws Exception {
        SQLParseService parseService = new SQLParseService(HitchhikerMocks.nodeExecutionContext());
        stmt = parseService.parse("select count(*), min(age) from characters group by race order by count(*) limit 4");

        fakeDocs = new Object[FAKE_DOCS_LENGTH];
        for (int i = 0; i < fakeDocs.length; i++) {
            fakeDocs[i] = new BigInteger(130, random).toString(32);
        }
        dummyLookup = new DummyLookup(fakeDocs);
    }

    @BenchmarkOptions(benchmarkRounds = 20)
    @Test
    public void testGroupingCollector() throws Exception {
        SQLGroupingCollector collector = new SQLGroupingCollector(
            stmt,
            dummyLookup,
            HitchhikerMocks.aggFunctionMap,
            new String[] {"r1", "r2", "r3", "r4" }
        );

        for (int i = 0; i < fakeDocs.length; i++) {
            collector.collect(i);
        }
    }

    private class DummyLookup implements GroupByFieldLookup {

        private final Object[] fakeDocs;
        private int docId;

        public DummyLookup(Object[] fakeDocs) {
            this.fakeDocs = fakeDocs;
        }

        @Override
        public void setNextDocId(int doc) {
            this.docId = doc;
        }

        @Override
        public void setNextReader(AtomicReaderContext context) {
        }

        @Override
        public Object lookupField(String columnName) throws IOException, GroupByOnArrayUnsupportedException {
            return fakeDocs[docId];
        }
    }
}
