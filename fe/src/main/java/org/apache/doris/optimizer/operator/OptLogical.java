// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.optimizer.operator;

import org.apache.doris.optimizer.OptExpression;
import org.apache.doris.optimizer.OptExpressionWapper;
import org.apache.doris.optimizer.OptUtils;
import org.apache.doris.optimizer.base.OptColumnRefSet;
import org.apache.doris.optimizer.base.OptItemProperty;
import org.apache.doris.optimizer.base.OptLogicalProperty;
import org.apache.doris.optimizer.base.OptMaxcard;
import org.apache.doris.optimizer.base.OptProperty;
import org.apache.doris.optimizer.stat.Statistics;
import org.apache.doris.optimizer.stat.StatisticsContext;

import java.util.BitSet;

public abstract class OptLogical extends OptOperator {

    protected OptLogical(OptOperatorType type) {
        super(type);
    }

    public abstract BitSet getCandidateRulesForExplore();

    public abstract BitSet getCandidateRulesForImplement();

    public abstract Statistics deriveStat(OptExpressionWapper wapper, StatisticsContext context);

    @Override
    public boolean isLogical() { return true; }
    public boolean isSelectOp() { return false; }

    @Override
    public OptProperty createProperty() {
        return new OptLogicalProperty();
    }

    protected OptColumnRefSet getOutputColumnPassThrough(OptExpressionHandle exprHandle) {
        return exprHandle.getChildLogicalProperty(0).getOutputColumns();
    }
    // TODO(zc): returning null to make compiler happy
    public OptColumnRefSet getOutputColumns(OptExpressionHandle exprHandle) {
        return null;
    }

    public OptColumnRefSet getOuterColumns(OptExpressionHandle exprHandle,
                                           OptColumnRefSet additionalUsedColumns) {
        OptColumnRefSet outerColumns = new OptColumnRefSet();
        OptColumnRefSet outputColumns = new OptColumnRefSet();
        OptColumnRefSet usedColumns = new OptColumnRefSet();
        for (int i = 0; i < exprHandle.arity(); ++i) {
            if (exprHandle.isItemChild(i)) {
                // Item Expression only use column
                OptItemProperty property = exprHandle.getChildItemProperty(i);
                usedColumns.include(property.getUsedColumns());
            } else {
                // union output and outer columns from children
                OptLogicalProperty property = exprHandle.getChildLogicalProperty(i);
                outputColumns.include(property.getOutputColumns());

                outerColumns.include(property.getOuterColumns());
            }
        }

        if (additionalUsedColumns != null) {
            usedColumns.include(additionalUsedColumns);
        }
        outerColumns.include(usedColumns);
        outerColumns.exclude(outerColumns);

        return outerColumns;
    }

    // return outer columns
    public OptColumnRefSet getOuterColumns(OptExpressionHandle exprHandle) {
        return getOuterColumns(exprHandle, null);
    }

    protected OptMaxcard getDefaultMaxcard(OptExpressionHandle exprHandle) {
        OptMaxcard maxcard = exprHandle.getChildLogicalProperty(0).getMaxcard();
        for (int i = 1; i < exprHandle.arity() - 1; ++i) {
            if (exprHandle.isItemChild(i)) {
                continue;
            }
            maxcard.multiply(exprHandle.getChildLogicalProperty(i).getMaxcard());
        }
        return maxcard;
    }

    // Derive max card given scalar child and constraint property. If a
    // contradiction is detected then return maxcard of zero, otherwise
    // use the given default maxcard
    protected OptMaxcard getMaxcard(OptExpressionHandle exprHandle, int itemIdx, OptMaxcard defaultCard) {
        // in case of a false condition (when the operator is not Full / Left Outer Join) or a contradiction,
        // maxcard should be zero
        OptExpression itemExpr = exprHandle.getItemChild(itemIdx);
        if (itemExpr != null &&
                ((OptUtils.isItemConstFalse(itemExpr) &&
                        !(exprHandle.getOp() instanceof OptLogicalFullOuterJoin) &&
                        !(exprHandle.getOp() instanceof OptLogicalLeftOuterJoin)))) {
            return new OptMaxcard(0);
        }
        return defaultCard;
    }

    public OptMaxcard getMaxcard(OptExpressionHandle exprHandle) {
        return new OptMaxcard();
    }

    public OptExpression pushThrough(OptExpression expr, OptExpression conj) {
        return null;
    }
}
