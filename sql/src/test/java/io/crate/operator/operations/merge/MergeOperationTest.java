/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.operator.operations.merge;

import io.crate.metadata.*;
import io.crate.operator.aggregation.AggregationFunction;
import io.crate.operator.aggregation.impl.AggregationImplModule;
import io.crate.operator.aggregation.impl.MinimumAggregation;
import io.crate.operator.operations.ImplementationSymbolVisitor;
import io.crate.operator.projectors.TopN;
import io.crate.planner.RowGranularity;
import io.crate.planner.node.MergeNode;
import io.crate.planner.projection.GroupProjection;
import io.crate.planner.projection.Projection;
import io.crate.planner.projection.TopNProjection;
import io.crate.planner.symbol.Aggregation;
import io.crate.planner.symbol.InputColumn;
import io.crate.planner.symbol.Symbol;
import org.cratedb.DataType;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MergeOperationTest {

    private GroupProjection groupProjection;
    private AggregationFunction<MinimumAggregation.MinimumAggState<Double>> minAggFunction;
    private ImplementationSymbolVisitor symbolVisitor;

    @Before
    @SuppressWarnings("unchecked")
    public void prepare() {
        Injector injector = new ModulesBuilder().add(new AggregationImplModule()).createInjector();
        Functions functions = injector.getInstance(Functions.class);
        ReferenceResolver referenceResolver = new GlobalReferenceResolver(Collections.<ReferenceIdent, ReferenceImplementation>emptyMap());
        symbolVisitor = new ImplementationSymbolVisitor(referenceResolver, functions, RowGranularity.NODE);

        FunctionIdent minAggIdent = new FunctionIdent(MinimumAggregation.NAME, Arrays.asList(DataType.DOUBLE));
        FunctionInfo minAggInfo = new FunctionInfo(minAggIdent, DataType.DOUBLE);
        minAggFunction = (AggregationFunction<MinimumAggregation.MinimumAggState<Double>>) functions.get(minAggIdent);

        groupProjection = new GroupProjection();
        groupProjection.keys(Arrays.<Symbol>asList(new InputColumn(0)));
        groupProjection.values(Arrays.asList(
                new Aggregation(minAggInfo, Arrays.<Symbol>asList(new InputColumn(1)), Aggregation.Step.PARTIAL, Aggregation.Step.FINAL)
        ));
    }

    @Test
    public void testMergeSingleResult() throws Exception {
        TopNProjection topNProjection = new TopNProjection(3, TopN.NO_OFFSET, Arrays.<Symbol>asList(new InputColumn(0)), new boolean[]{false});
        topNProjection.outputs(Arrays.<Symbol>asList(new InputColumn(0), new InputColumn(1)));

        MergeNode mergeNode = new MergeNode("merge", 2); // no need for inputTypes here
        mergeNode.projections(Arrays.asList(
                groupProjection,
                topNProjection
        ));

        MergeOperation mergeOperation = new MergeOperation(symbolVisitor, mergeNode);

        Object[][] rows = new Object[20][];
        for (int i=0; i<rows.length; i++) {
            MinimumAggregation.MinimumAggState<Double> aggState = minAggFunction.newState();
            aggState.setValue(i+0.5d);
            rows[i] = new Object[]{ i%4, aggState};
        }
        assertTrue(mergeOperation.addRows(rows));

        Object[][] mergeResult = mergeOperation.result();
        assertThat(mergeResult.length, is(3));
        assertThat((Integer)mergeResult[0][0], is(0));
        assertThat((Double)mergeResult[0][1], is(0.5d));

        assertThat((Integer)mergeResult[1][0], is(1));
        assertThat((Double)mergeResult[1][1], is(1.5d));

        assertThat((Integer)mergeResult[2][0], is(2));
        assertThat((Double)mergeResult[2][1], is(2.5d));
    }

    @Test
    public void testMergeMultipleResults() throws Exception {
        MergeNode mergeNode = new MergeNode("merge", 2); // no need for inputTypes here
        mergeNode.projections(Arrays.<Projection>asList(
                groupProjection
        ));
        MergeOperation mergeOperation = new MergeOperation(symbolVisitor, mergeNode);
        Object[][] rows = new Object[1][];
        MinimumAggregation.MinimumAggState<Double> aggState = minAggFunction.newState();
        aggState.setValue(100.0d);
        rows[0] = new Object[]{0, aggState};
        assertTrue(mergeOperation.addRows(rows));

        Object[][] otherRows = new Object[1][];
        MinimumAggregation.MinimumAggState<Double> otherAggState = minAggFunction.newState();
        otherAggState.setValue(2.5d);
        otherRows[0] = new Object[]{0, otherAggState};
        assertTrue(mergeOperation.addRows(otherRows));

        Object[][] mergeResult = mergeOperation.result();
        assertThat(mergeResult.length, is(1));
        assertThat((Integer)mergeResult[0][0], is(0));
        assertThat((Double)mergeResult[0][1], is(2.5d));
    }

}