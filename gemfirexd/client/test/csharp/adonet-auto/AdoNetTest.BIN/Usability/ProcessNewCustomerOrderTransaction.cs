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
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using Pivotal.Data.GemFireXD;
using AdoNetTest.BIN.BusinessObjects;
using AdoNetTest.BIN.BusinessRules;
using AdoNetTest.BIN.DataObjects;

namespace AdoNetTest.BIN.Usability
{
    /// <summary>
    /// Execute a new order transaction. Expects no exception
    /// </summary>
    class ProcessNewCustomerOrderTransaction : GFXDTest
    {
        public ProcessNewCustomerOrderTransaction(ManualResetEvent resetEvent)
            : base(resetEvent)
        {
        }

        public override void Run(object context)
        {
            DbController dbc = new DbController(Connection);
            IList<BusinessObject> bObjects = new List<BusinessObject>();

            try
            {
                Connection.AutoCommit = false;
                Connection.BeginGFXDTransaction();

                Order order = (Order)ObjectFactory.Create(ObjectType.Order);
                order.Customer = dbc.GetRandomCustomer();
                dbc.AddOrder(order);
                
                IList<Product> products = dbc.GetRandomProducts(5);
                foreach (Product product in products)
                {
                    OrderDetail ordDetail = new OrderDetail();
                    ordDetail.OrderId = order.OrderId;
                    ordDetail.ProductId = product.ProductId;
                    ordDetail.UnitPrice = product.RetailPrice;
                    ordDetail.Quantity = DbHelper.GetRandomNumber(1);
                    ordDetail.Discount = 0;

                    dbc.AddOrderDetail(ordDetail);
                    bObjects.Add(ordDetail);

                    product.UnitsInStock -= ordDetail.Quantity;
                    dbc.UpdateProduct(product);

                    order.SubTotal += (ordDetail.UnitPrice * ordDetail.Quantity);
                }

                dbc.UpdateOrder(order);
                bObjects.Add(order);

                Connection.Commit();

                if (!dbc.ValidateTransaction(bObjects))
                    Fail(dbc.GetValidationErrors(bObjects));
            }
            catch (Exception e)
            {
                Connection.Rollback();
                Fail(e);
            }
            finally
            {                
                base.Run(context);
            }
        }
    }
}
