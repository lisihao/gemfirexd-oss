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
package parReg.execute;

import hydra.blackboard.Blackboard;

public class ExecuteExceptionBB extends Blackboard {

  // Blackboard creation variables
  static String BB_NAME = "ExecuteException_Blackboard";

  static String BB_TYPE = "RMI";

  // sharedCounters:
  public static int signalCacheClose;

  public static int cacheCloseCompleted;

  public static int signalCacheCreate;

  public static int cacheCreateCompleted;

  public static int vmCrashComplete;

  public static int crashedVMUp;

  public static int keyDestroyCompleted;

  public static ExecuteExceptionBB bbInstance = null;

  public static int exceptionNodes;

  public static int sendResultsNodes;

  /**
   * Get the BB
   */
  public static ExecuteExceptionBB getBB() {
    if (bbInstance == null) {
      synchronized (ExecuteExceptionBB.class) {
        if (bbInstance == null)
          bbInstance = new ExecuteExceptionBB(BB_NAME, BB_TYPE);
      }
    }
    return bbInstance;
  }

  /**
   * Zero-arg constructor for remote method invocations.
   */
  public ExecuteExceptionBB() {
  }

  /**
   * Creates a sample blackboard using the specified name and transport type.
   */
  public ExecuteExceptionBB(String name, String type) {
    super(name, type, ExecuteExceptionBB.class);
  }

}
