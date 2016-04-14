/*
 Licensed to Diennea S.r.l. under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. Diennea S.r.l. licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */
package herddb.core;

import herddb.client.impl.ScanResultList;
import herddb.model.GetResult;
import herddb.model.Predicate;
import herddb.model.commands.InsertStatement;
import herddb.model.Record;
import herddb.model.StatementExecutionException;
import herddb.model.commands.DeleteStatement;
import herddb.model.commands.GetStatement;
import herddb.model.commands.ScanStatement;
import herddb.utils.Bytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 * Tests on table creation
 *
 * @author enrico.olivelli
 */
public class ScanTest extends BaseTestcase {

    @Test
    public void test() throws Exception {

        for (int i = 0; i < 100; i++) {
            Record record = new Record(Bytes.from_string("key_" + i), Bytes.from_int(i));
            InsertStatement st = new InsertStatement(tableSpace, tableName, record);
            assertEquals(1, manager.executeUpdate(st).getUpdateCount());
        }

        {
            ScanStatement scan = new ScanStatement(tableSpace, tableName, new Predicate() {
                @Override
                public boolean evaluate(Record record) throws StatementExecutionException {
                    int value = record.value.to_int();
                    return value >= 50;
                }
            });
            ScanResultList result = new ScanResultList();
            assertEquals(50, manager.scan(scan, result));
            assertEquals(50, result.results.size());
        }

        for (int i = 0; i < 20; i++) {
            DeleteStatement st = new DeleteStatement(tableSpace, tableName, Bytes.from_string("key_" + i), null);
            assertEquals(1, manager.executeUpdate(st).getUpdateCount());
        }

        {
            ScanStatement scan = new ScanStatement(tableSpace, tableName, new Predicate() {
                @Override
                public boolean evaluate(Record record) throws StatementExecutionException {
                    int value = record.value.to_int();
                    return value < 50;
                }
            });
            ScanResultList result = new ScanResultList();
            assertEquals(30, manager.scan(scan, result));
            assertEquals(30, result.results.size());
        }
        
        {
            ScanStatement scan = new ScanStatement(tableSpace, tableName, null);
            ScanResultList result = new ScanResultList();
            assertEquals(80, manager.scan(scan, result));
            assertEquals(80, result.results.size());
        }
    }
}