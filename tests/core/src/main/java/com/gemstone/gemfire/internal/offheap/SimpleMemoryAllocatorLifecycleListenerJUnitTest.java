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
package com.gemstone.gemfire.internal.offheap;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import com.gemstone.gemfire.internal.offheap.SimpleMemoryAllocatorImpl.LifecycleListener;

/**
 * Tests SimpleMemoryAllocatorImpl.LifecycleListener
 * 
 * @author Kirk Lund
 */
public class SimpleMemoryAllocatorLifecycleListenerJUnitTest extends TestCase {
  
  private final List<LifecycleListenerCallback> afterCreateCallbacks = new ArrayList<LifecycleListenerCallback>();
  private final List<LifecycleListenerCallback> afterReuseCallbacks = new ArrayList<LifecycleListenerCallback>();
  private final List<LifecycleListenerCallback> beforeCloseCallbacks = new ArrayList<LifecycleListenerCallback>();
  private final TestLifecycleListener listener = new TestLifecycleListener(
      this.afterCreateCallbacks, this.afterReuseCallbacks, this.beforeCloseCallbacks);
  
  @After
  public void tearDown() throws Exception {
    SimpleMemoryAllocatorImpl.removeLifecycleListener(this.listener);
    this.afterCreateCallbacks.clear();
    this.afterReuseCallbacks.clear();
    this.beforeCloseCallbacks.clear();
    SimpleMemoryAllocatorImpl.freeOffHeapMemory();
  }

  public void testAddRemoveListener() {
    SimpleMemoryAllocatorImpl.addLifecycleListener(this.listener);
    SimpleMemoryAllocatorImpl.removeLifecycleListener(this.listener);

    UnsafeMemoryChunk slab = new UnsafeMemoryChunk(1024); // 1k
    SimpleMemoryAllocatorImpl ma = SimpleMemoryAllocatorImpl.create(new NullOutOfOffHeapMemoryListener(), new NullOffHeapMemoryStats(), new UnsafeMemoryChunk[]{slab});

    Assert.assertEquals(0, this.afterCreateCallbacks.size());
    Assert.assertEquals(0, this.afterReuseCallbacks.size());
    Assert.assertEquals(0, this.beforeCloseCallbacks.size());
    
    ma.close();

    Assert.assertEquals(0, this.afterCreateCallbacks.size());
    Assert.assertEquals(0, this.afterReuseCallbacks.size());
    Assert.assertEquals(0, this.beforeCloseCallbacks.size());
  }
  
  public void testCallbacksAreCalledAfterCreate() {
    SimpleMemoryAllocatorImpl.addLifecycleListener(this.listener);
    
    UnsafeMemoryChunk slab = new UnsafeMemoryChunk(1024); // 1k
    SimpleMemoryAllocatorImpl ma = SimpleMemoryAllocatorImpl.create(new NullOutOfOffHeapMemoryListener(), new NullOffHeapMemoryStats(), new UnsafeMemoryChunk[]{slab});

    Assert.assertEquals(1, this.afterCreateCallbacks.size());
    Assert.assertEquals(0, this.afterReuseCallbacks.size());
    Assert.assertEquals(0, this.beforeCloseCallbacks.size());
    
    ma.close();
    
    Assert.assertEquals(1, this.afterCreateCallbacks.size());
    Assert.assertEquals(0, this.afterReuseCallbacks.size());
    Assert.assertEquals(1, this.beforeCloseCallbacks.size());
  }
  
  static final class LifecycleListenerCallback {
    private final SimpleMemoryAllocatorImpl allocator;
    private final long timeStamp;
    private final Throwable creationTime;
    LifecycleListenerCallback(SimpleMemoryAllocatorImpl allocator) {
      this.allocator = allocator;
      this.timeStamp = System.currentTimeMillis();
      this.creationTime = new Exception();
    }
    SimpleMemoryAllocatorImpl getAllocator() {
      return this.allocator;
    }
    Throwable getStackTrace() {
      return this.creationTime;
    }
    long getCreationTime() {
      return this.timeStamp;
    }
    @Override
    public String toString() {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.print(new StringBuilder().
          append(super.toString()).
          append(" created at ").
          append(this.timeStamp).
          append(" by ").toString());
      this.creationTime.printStackTrace(pw);
      return sw.toString();
    }
  }
  
  static class TestLifecycleListener implements LifecycleListener {
    private final List<LifecycleListenerCallback> afterCreateCallbacks;
    private final List<LifecycleListenerCallback> afterReuseCallbacks;
    private final List<LifecycleListenerCallback> beforeCloseCallbacks;
    TestLifecycleListener(List<LifecycleListenerCallback> afterCreateCallbacks,
        List<LifecycleListenerCallback> afterReuseCallbacks,
        List<LifecycleListenerCallback> beforeCloseCallbacks) {
      this.afterCreateCallbacks = afterCreateCallbacks;
      this.afterReuseCallbacks = afterReuseCallbacks;
      this.beforeCloseCallbacks = beforeCloseCallbacks;
    }
    @Override
    public void afterCreate(SimpleMemoryAllocatorImpl allocator) {
      this.afterCreateCallbacks.add(new LifecycleListenerCallback(allocator));
    }
    @Override
    public void afterReuse(SimpleMemoryAllocatorImpl allocator) {
      this.afterReuseCallbacks.add(new LifecycleListenerCallback(allocator));
    }
    @Override
    public void beforeClose(SimpleMemoryAllocatorImpl allocator) {
      this.beforeCloseCallbacks.add(new LifecycleListenerCallback(allocator));
    }
  }
}
