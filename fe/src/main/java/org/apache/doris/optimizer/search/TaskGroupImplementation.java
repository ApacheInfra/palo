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

package org.apache.doris.optimizer.search;

import org.apache.doris.optimizer.MultiExpression;
import org.apache.doris.optimizer.OptGroup;

/**
 * For creating physical implementations of all expressions in the group.
 *
 * +--------------------------------+  MultiExpression is not implemented.
 * |                                |------------------>+
 * |       ImplementingState        |                   | Child StateMachine
 * |                                |<------------------+
 * +--------------------------------+
 *                |
 *                |  MultiExpression are all implemented.
 *                V
 * +--------------------------------+
 * |                                |
 * |       CompletingState          |
 * |                                |
 * +--------------------------------+
 *                |
 *                |
 *                V
 *        Parent StateMachine
 */

public class TaskGroupImplementation extends Task {

    private final OptGroup group;
    private int lastMexprIndex;

    private TaskGroupImplementation(OptGroup group, Task parent) {
        super(parent);
        this.group = group;
        this.nextState = new ImplementingState();
        this.lastMexprIndex = 0;
    }

    public static void schedule(SearchContext sContext, OptGroup group, Task parent) {
        sContext.schedule(new TaskGroupImplementation(group, parent));
    }

    private class ImplementingState extends TaskState {

        @Override
        public void handle(SearchContext sContext) {
            group.setStatus(OptGroup.GState.Implementing);
            boolean hasNew = false;
            for (; lastMexprIndex < group.getMultiExpressions().size(); lastMexprIndex++) {
                final MultiExpression mExpr = group.getMultiExpressions().get(lastMexprIndex);
                if (!mExpr.isImplemented()) {
                    TaskMultiExpressionImplementation.schedule(sContext, mExpr, TaskGroupImplementation.this);
                    hasNew = true;
                }
            }

            if (hasNew) {
                return;
            }

            nextState = new CompletingState();
        }
    }

    private class CompletingState extends TaskState {

        @Override
        public void handle(SearchContext sContext) {
            group.setStatus(OptGroup.GState.Implemented);
            deriveProperty();
            setFinished();
        }

        public void deriveProperty() {
        }
    }
}
