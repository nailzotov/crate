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

import com.google.common.collect.ImmutableMap;
import io.crate.sql.tree.*;

public class CreateTableStatementAnalyzer extends AstVisitor<Void, Void> {

    @Override
    protected Void visitNode(Node node, Void context) {
        throw new RuntimeException(
                String.format("Encountered node %s but expected a CreateTable node", node));
    }

    @Override
    public Void visitCreateTable(CreateTable node, Void context) {
        node.clusteredBy();
        node.name();
        node.replicas();

        ImmutableMap.Builder<String, Object> propertiesBuilder = ImmutableMap.builder();
        for (TableElement tableElement : node.tableElements()) {
            process(tableElement, context);
        }

        return null;
    }

    @Override
    public Void visitColumnDefinition(ColumnDefinition node, Void context) {

        /**
         *  _meta : {
         *      columns: {
         *          "someColumn": {
         *              "array": true
         *          }
         *      }
         * }
         */

        ImmutableMap.builder()
                .put("type", node.type().name())
                .put("store", false);

        // TODO: index constraint
        for (ColumnConstraint columnConstraint : node.constraints()) {
            process(columnConstraint, context);

        }

        switch (node.type().type()) {
            case PRIMITIVE:
            case ARRAY:
                process(node.type(), context);
                break;
            case SET:
                throw new UnsupportedOperationException("the SET type is currently not supported");
        }


        return null;
    }

    @Override
    public Void visitColumnType(ColumnType node, Void context) {
        return super.visitColumnType(node, context);
    }

    @Override
    public Void visitObjectColumnType(ObjectColumnType node, Void context) {
        return super.visitObjectColumnType(node, context);
    }

    @Override
    public Void visitCollectionColumnType(CollectionColumnType node, Void context) {
        return super.visitCollectionColumnType(node, context);
    }

    @Override
    public Void visitIndexColumnConstraint(IndexColumnConstraint node, Void context) {
        return null;
    }

    @Override
    public Void visitPrimaryKeyConstraint(PrimaryKeyConstraint node, Void context) {
        return null;
    }

    @Override
    public Void visitPrimaryKeyColumnConstraint(PrimaryKeyColumnConstraint node, Void context) {
        return null;
    }
}
