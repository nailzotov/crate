package org.cratedb.module.sql.test;

import org.cratedb.sql.*;
import org.elasticsearch.action.search.ReduceSearchPhaseException;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.engine.DocumentAlreadyExistsException;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.transport.RemoteTransportException;
import org.junit.Test;

public class ExceptionHelperTest {

    class TestException extends Throwable {}

    @Test (expected = TestException.class)
    public void testReRaiseCrateExceptionGeneric() throws Throwable {
        Throwable throwable = new TestException();
        throw ExceptionHelper.transformToCrateException(throwable);
    }

    @Test (expected = TestException.class)
    public void testReRaiseCrateExceptionRemoteTransportGeneric() throws Throwable {
        Throwable throwable = new TestException();
        throw ExceptionHelper.transformToCrateException(
                new RemoteTransportException("remote transport failed", throwable)
        );
    }
    @Test (expected = DuplicateKeyException.class)
    public void testReRaiseCrateExceptionDocumentAlreadyExists() throws Throwable {
        Throwable throwable = new DocumentAlreadyExistsException(
                new ShardId("test", 1),
                "default",
                "1"
               );
        throw ExceptionHelper.transformToCrateException(throwable);
    }

    @Test (expected = TableAlreadyExistsException.class)
    public void testReRaiseCrateExceptionIndexAlreadyExists() throws Throwable {
        Throwable throwable = new IndexAlreadyExistsException(new Index("test"));
        throw ExceptionHelper.transformToCrateException(throwable);
    }

    @Test (expected = VersionConflictException.class)
    public void testReRaiseCrateExceptionReduceSearchPhaseVersionConflict() throws Throwable {
        Throwable throwable1 = new VersionConflictException(new Throwable());

        ShardSearchFailure[] searchFailures = {new ShardSearchFailure(throwable1)};

        throw ExceptionHelper.transformToCrateException(
                new ReduceSearchPhaseException("step1", "reduce failed", throwable1,
                        searchFailures)
        );
    }

    @Test (expected = CrateException.class)
    public void testOnShardSearchFailures() throws Exception {
        ShardSearchFailure[] shardSearchFailures = {
                new ShardSearchFailure(new Exception())
        };
        ExceptionHelper.exceptionOnSearchShardFailures(shardSearchFailures);
    }

    @Test (expected = VersionConflictException.class)
    public void testOnShardSearchFailuresVersionConflict() throws Exception {
        VersionConflictEngineException versionConflictEngineException =
                new VersionConflictEngineException(new ShardId("test",1), "default", "1", 1, 1);
        Exception failureException = new RemoteTransportException(
                "failed",
                versionConflictEngineException);
        ShardSearchFailure[] shardSearchFailures = {
                new ShardSearchFailure(failureException)
        };
        ExceptionHelper.exceptionOnSearchShardFailures(shardSearchFailures);
    }

    @Test (expected = TableUnknownException.class)
    public void testReRaiseCrateExceptionIndexMissing() throws Throwable {
        Throwable throwable = new IndexMissingException(new Index("test"));
        throw ExceptionHelper.transformToCrateException(throwable);
    }

}