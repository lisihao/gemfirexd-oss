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
 * A SerialDistributionMessage is processed in the order
 * it is generated from a single thread's point of view.
 */
public abstract class SerialDistributionMessage extends DistributionMessage {

  // not "final" because it's overridden in ViewMessage
  @Override
  public int getProcessorType() {
    return DistributionManager.SERIAL_EXECUTOR;
  }


}

