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
package com.gemstone.gemfire.cache30;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.util.*;
import com.gemstone.gemfire.distributed.*;
import com.gemstone.gemfire.distributed.internal.*;
import com.gemstone.gemfire.internal.cache.TXManagerImpl;

import java.util.*;
import dunit.*;
/**
 * Test the getCallbackArgument in light of bug 34075.
 *
 * @author darrel
 * @since 5.0
 */
public class CallbackArgDUnitTest extends CacheTestCase {

//  private transient Region r;
//  private transient DistributedMember otherId;
  protected transient volatile int invokeCount;
  protected static String callbackArg;
  
  public CallbackArgDUnitTest(String name) {
    super(name);
  }

  private VM getOtherVm() {
    Host host = Host.getHost(0);
    return host.getVM(0);
  }
    
  private void initOtherId() {
    VM vm = getOtherVm();
    vm.invoke(new CacheSerializableRunnable("Connect") {
        @Override
        public void run2() throws CacheException {
          getCache();
        }
      });
    vm.invoke(CallbackArgDUnitTest.class, "getVMDistributedMember");
  }
  private void doCommitOtherVm() {
    VM vm = getOtherVm();
    vm.invoke(new CacheSerializableRunnable("create root") {
        @Override
        public void run2() throws CacheException {
          AttributesFactory af = new AttributesFactory();
          CacheListener cl1 = new CacheListenerAdapter() {
              @Override
              public void afterCreate(EntryEvent e) {
                assertEquals(callbackArg, e.getCallbackArgument());
              }
            };
          af.addCacheListener(cl1);
          af.setScope(Scope.DISTRIBUTED_ACK);
          Region r1 = createRootRegion("r1", af.create());
          Region r2 = r1.createSubregion("r2", af.create());
          Region r3 = r2.createSubregion("r3", af.create());
          TXManagerImpl ctm = (TXManagerImpl)getCache()
              .getCacheTransactionManager();
          TransactionListener tl1 = new TransactionListenerAdapter() {
              @Override
              public void afterCommit(TransactionEvent e) {
                assertEquals(6, e.getEvents().size());
                Iterator it = e.getEvents().iterator();
                while (it.hasNext()) {
                  EntryEvent ee = (EntryEvent)it.next();
                  assertEquals(callbackArg, ee.getCallbackArgument());
                  assertEquals(true, ee.isCallbackArgumentAvailable());
                }
              }
            };
          ctm.addListener(tl1);
          ctm.begin();
          r2.put("b", "value1", callbackArg);
          r3.put("c", "value2", callbackArg);
          r1.put("a", "value3", callbackArg);
          r1.put("a2", "value4", callbackArg);
          r3.put("c2", "value5", callbackArg);
          r2.put("b2", "value6", callbackArg);
          ctm.commit();
          TXManagerImpl.waitForPendingCommitForTest();
        }
      });
  }

  public static DistributedMember getVMDistributedMember() {
    return InternalDistributedSystem.getAnyInstance().getDistributedMember();
  }
  
  //////////////////////  Test Methods  //////////////////////

  List expectedKeys;
  int clCount = 0;

  Object getCurrentExpectedKey() {
    Object result = this.expectedKeys.get(this.clCount);
    this.clCount += 1;
    return result;
  }
  /**
   * make sure callback arg is NOT_AVAILABLE in all the places it should be
   */
  public void testForNA_CA() throws CacheException {
    doTest();
  }
  public void testForCA() throws Exception {
    callbackArg = "cbArg";
    getOtherVm().invoke(new SerializableCallable() {
      public Object call() throws Exception {
        callbackArg = "cbArg";
        return null;
      }
    });
    doTest();
  }
  private void doTest() throws CacheException {
    initOtherId();
    AttributesFactory af = new AttributesFactory();
    af.setDataPolicy(DataPolicy.REPLICATE);
    af.setScope(Scope.DISTRIBUTED_ACK);
    CacheListener cl1 = new CacheListenerAdapter() {
        @Override
        public void afterCreate(EntryEvent e) {
          assertEquals(getCurrentExpectedKey(), e.getKey());
          assertEquals(callbackArg, e.getCallbackArgument());
          assertEquals(true, e.isCallbackArgumentAvailable());
        }
      };
    af.addCacheListener(cl1);
    Region r1 = createRootRegion("r1", af.create());
    Region r2 = r1.createSubregion("r2", af.create());
    r2.createSubregion("r3", af.create());

    TransactionListener tl1 = new TransactionListenerAdapter() {
        @Override
        public void afterCommit(TransactionEvent e) {
          assertEquals(6, e.getEvents().size());
          ArrayList keys = new ArrayList();
          Iterator it = e.getEvents().iterator();
          while (it.hasNext()) {
            EntryEvent ee = (EntryEvent)it.next();
            keys.add(ee.getKey());
            assertEquals(callbackArg, ee.getCallbackArgument());
            assertEquals(true, ee.isCallbackArgumentAvailable());
          }
          assertEquals(CallbackArgDUnitTest.this.expectedKeys, keys);
          CallbackArgDUnitTest.this.invokeCount = 1;
        }
      };
    CacheTransactionManager ctm =  getCache().getCacheTransactionManager();
    ctm.addListener(tl1);

    this.invokeCount = 0;
    this.clCount = 0;
    this.expectedKeys = Arrays.asList(new String[]{"b", "c", "a", "a2", "c2", "b2"});
    doCommitOtherVm();
    assertEquals(1, this.invokeCount);
    assertEquals(6, this.clCount);
  }
}
