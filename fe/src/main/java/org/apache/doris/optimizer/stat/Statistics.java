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

package org.apache.doris.optimizer.stat;

import com.google.common.collect.Maps;
import org.apache.doris.optimizer.base.OptColumnRefSet;

import java.util.Map;

public class Statistics {

    private long rowCount;
    private OptColumnRefSet statColumns;
    private Map<Integer, Long> columnsCardinalityMap;

    public Statistics() {
        this.rowCount = 0;
        this.statColumns = new OptColumnRefSet();
        this.columnsCardinalityMap = Maps.newHashMap();
    }

    public void addRow(int id, long cardinality) {
        this.columnsCardinalityMap.put(id, cardinality);
        this.statColumns.include(id);
    }

    public long getRowCount() {
        return rowCount;
    }

    public Long getCardinality(int id) {
        return columnsCardinalityMap.get(id);
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public OptColumnRefSet getStatColumns() {
        return statColumns;
    }
}
