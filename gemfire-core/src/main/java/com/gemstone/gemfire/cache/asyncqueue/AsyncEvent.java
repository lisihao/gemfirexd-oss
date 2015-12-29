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
package com.gemstone.gemfire.cache.asyncqueue;

import com.gemstone.gemfire.cache.wan.EventSequenceID;
import com.gemstone.gemfire.cache.wan.GatewayQueueEvent;

/**
 * Represents <code>Cache</code> events delivered to <code>AsyncEventListener</code>.
 * 
 * @author pdeole
 * @since 7.0
 */
public interface AsyncEvent<K, V> extends GatewayQueueEvent<K, V>{
  /**
   * Returns whether possibleDuplicate is set for this event.
   */
  public boolean getPossibleDuplicate();
  
  /**
   * Returns the wrapper over the DistributedMembershipID, ThreadID, SequenceID
   * which are used to uniquely identify any region operation like create, update etc.
   * This helps in sequencing the events belonging to a unique producer.
   * e.g. The EventID can be used to track events received by <code>AsyncEventListener</code>
   * to avoid processing duplicates.
   */
  public EventSequenceID getEventSequenceID();
}