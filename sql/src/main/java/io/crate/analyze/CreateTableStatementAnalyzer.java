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
package io.crate.analyze;

import io.crate.metadata.TableIdent;
import io.crate.sql.tree.*;

import java.util.HashMap;
import java.util.Map;

public class CreateTableStatementAnalyzer extends AstVisitor<Void, CreateTableAnalysis> {

    @Override
    protected Void visitNode(Node node, CreateTableAnalysis context) {
        throw new RuntimeException(
                String.format("Encountered node %s but expected a CreateTable node", node));
    }

    @Override
    public Void visitCreateTable(CreateTable node, CreateTableAnalysis context) {

        context.table(TableIdent.of(node.name()));
        context.indexSettingsBuilder().put("number_of_replicas", node.replicas().or(1));

        if (node.clusteredBy().isPresent()) {
            ClusteredBy clusteredBy = node.clusteredBy().get();
            context.indexSettingsBuilder().put("number_of_shards", clusteredBy.numberOfShards().or(5));
        }

        for (TableElement tableElement : node.tableElements()) {
            process(tableElement, context);
        }

        return null;
    }

    @Override
    public Void visitColumnDefinition(ColumnDefinition node, CreateTableAnalysis context) {
        Map<String, Object> metaColumnDefinition = new HashMap<>();
        Map<String, Object> columnDefinition = new HashMap<>();
        context.addColumnDefinition(
                node.ident(),
                columnDefinition,
                metaColumnDefinition
        );
        columnDefinition.put("store", false);

        if (node.constraints().isEmpty()) { // set default if no constraints
            columnDefinition.put("index", "not_analyzed");
        }

        for (ColumnConstraint columnConstraint : node.constraints()) {
            process(columnConstraint, context);

        }

        process(node.type(), context);

        return null;
    }

    @Override
    public Void visitColumnType(ColumnType node, CreateTableAnalysis context) {
        context.currentMetaColumnDefinition().put("collection_type", null);
        //context.currentColumnDefinition().put("doc_values", true); // TODO: only if not analyzed
        context.currentColumnDefinition().put("type", node.type().name());

        return null;
    }

    @Override
    public Void visitObjectColumnType(ObjectColumnType node, CreateTableAnalysis context) {
        return null;
    }

    @Override
    public Void visitCollectionColumnType(CollectionColumnType node, CreateTableAnalysis context) {
        if (node.type() == ColumnType.Type.SET) {
            throw new UnsupportedOperationException("the SET dataType is currently not supported");
        }

        context.currentMetaColumnDefinition().put("collection_type", "array");
        context.currentColumnDefinition().put("doc_values", false);


        return null;
    }

    @Override
    public Void visitIndexColumnConstraint(IndexColumnConstraint node, CreateTableAnalysis context) {
        return null;
    }

    @Override
    public Void visitPrimaryKeyConstraint(PrimaryKeyConstraint node, CreateTableAnalysis context) {
        return null;
    }

    @Override
    public Void visitPrimaryKeyColumnConstraint(PrimaryKeyColumnConstraint node, CreateTableAnalysis context) {
        return null;
    }
}
