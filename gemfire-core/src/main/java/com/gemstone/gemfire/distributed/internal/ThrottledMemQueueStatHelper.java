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

package com.gemstone.gemfire.distributed.internal;

/**
 * Used to implement statistics on a throttled queue.
 * The implementation will call these methods at to proper time.
 *
 * @author Bruce Schuchardt
 *
 * @since 5.0
 */
public interface ThrottledMemQueueStatHelper extends QueueStatHelper {

  /**
   * Called each time a thread was delayed by the throttle.
   */
  public void incThrottleCount();
  /**
   * Called after a throttled operation has completed.
   * @param nanos the amount of time, in nanoseconds, the throttle caused us to wait.
   */
  public void throttleTime(long nanos);

  /** Increments the amount of memory consumed by queue contents.
   * @param amount number of bytes added to the queue
   */
  public void addMem(int amount);

  /** Decrements the amount of memory consumed by queue contents.
   * @param amount number of bytes removed from the queue
   */
  public void removeMem(int amount);
}
