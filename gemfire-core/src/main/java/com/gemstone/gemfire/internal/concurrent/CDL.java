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
package com.gemstone.gemfire.internal.concurrent;

/**
 * Proxy for
 * the JDK 5 version java.util.concurrent.CountDownLatch class
 * @see CFactory
 * @author darrel
 * @deprecated use CountDownLatch
 */
public interface CDL {
  /**
   * Causes the current thread to wait until the latch has counted down to
   * zero, unless the thread is {@linkplain Thread#interrupt interrupted}.
   *
   * <p>If the current count is zero then this method returns immediately.
   *
   * <p>If the current count is greater than zero then the current
   * thread becomes disabled for thread scheduling purposes and lies
   * dormant until one of two things happen:
   * <ul>
   * <li>The count reaches zero due to invocations of the
   * {@link #countDown} method; or
   * <li>Some other thread {@linkplain Thread#interrupt interrupts}
   * the current thread.
   * </ul>
   *
   * <p>If the current thread:
   * <ul>
   * <li>has its interrupted status set on entry to this method; or
   * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
   * </ul>
   * then {@link InterruptedException} is thrown and the current thread's
   * interrupted status is cleared.
   *
   * @throws InterruptedException if the current thread is interrupted
   *         while waiting
   */
  public void await() throws InterruptedException;

  /**
   * Causes the current thread to wait until the latch has counted down to
   * zero, unless the thread is {@linkplain Thread#interrupt interrupted},
   * or the specified waiting time elapses.
   *
   * <p>If the current count is zero then this method returns immediately
   * with the value {@code true}.
   *
   * <p>If the current count is greater than zero then the current
   * thread becomes disabled for thread scheduling purposes and lies
   * dormant until one of three things happen:
   * <ul>
   * <li>The count reaches zero due to invocations of the
   * {@link #countDown} method; or
   * <li>Some other thread {@linkplain Thread#interrupt interrupts}
   * the current thread; or
   * <li>The specified waiting time elapses.
   * </ul>
   *
   * <p>If the count reaches zero then the method returns with the
   * value {@code true}.
   *
   * <p>If the current thread:
   * <ul>
   * <li>has its interrupted status set on entry to this method; or
   * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
   * </ul>
   * then {@link InterruptedException} is thrown and the current thread's
   * interrupted status is cleared.
   *
   * <p>If the specified waiting time elapses then the value {@code false}
   * is returned.  If the time is less than or equal to zero, the method
   * will not wait at all.
   *
   * @param msTimeout the maximum time to wait in milliseconds
   * @return {@code true} if the count reached zero and {@code false}
   *         if the waiting time elapsed before the count reached zero
   * @throws InterruptedException if the current thread is interrupted
   *         while waiting
   */
  public boolean await(long msTimeout) throws InterruptedException;

  /**
   * Decrements the count of the latch, releasing all waiting threads if
   * the count reaches zero.
   *
   * <p>If the current count is greater than zero then it is decremented.
   * If the new count is zero then all waiting threads are re-enabled for
   * thread scheduling purposes.
   *
   * <p>If the current count equals zero then nothing happens.
   */
  public void countDown();

  /**
   * Returns the current count.
   *
   * <p>This method is typically used for debugging and testing purposes.
   *
   * @return the current count
   */
  public long getCount();
}
