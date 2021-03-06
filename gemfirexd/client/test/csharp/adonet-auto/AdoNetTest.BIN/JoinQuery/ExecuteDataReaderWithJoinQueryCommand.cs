/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
 
using System;
using System.Data;
using System.Data.Common;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using Pivotal.Data.GemFireXD;
using AdoNetTest.BIN.BusinessObjects;

namespace AdoNetTest.BIN.JoinQuery
{
    /// <summary>
    /// Verifies GFXDDataReader returns all columns specified in the join query command 
    /// </summary>
    class ExecuteDataReaderWithJoinQueryCommand : GFXDTest
    {
        public ExecuteDataReaderWithJoinQueryCommand(ManualResetEvent resetEvent)
            : base(resetEvent)
        {
        }

        public override void Run(object context)
        {
            DbController dbc = new DbController(Connection);

            try
            {
                Category category = dbc.GetRandomCategory();

                Command.CommandText = DbDefault.GetInnerJoinSelectStatement(
                    Relation.PRODUCT_CATEGORY, new long[] { category.CategoryId });

                DataReader = Command.ExecuteReader();

                if (DataReader.FieldCount != (DbDefault.GetTableStructure(TableName.PRODUCT).Columns.Count
                    + DbDefault.GetTableStructure(TableName.CATEGORY).Columns.Count))
                    Fail("Number of data fields returned is incorrect");
            }
            catch (Exception e)
            {
                Fail(e);
            }
            finally
            {
                base.Run(context);
            }
        }
    }
}
