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

package com.gemstone.gemfire.internal.shared;

import java.net.Socket;


/**
 * Extended TCP socket options to set socket-specific KEEPALIVE settings etc.
 * Passed to {@link NativeCalls} API to set these options on the Java
 * {@link Socket} using native OS specific calls.
 * 
 * @author swale
 * @since 7.5
 */
public enum TCPSocketOptions {

  /**
   * TCP keepalive time between two transmissions on socket in idle condition
   * (in seconds)
   */
  OPT_KEEPIDLE,
  /**
   * TCP keepalive duration between successive transmissions on socket if no
   * reply to packet sent after idle timeout (in seconds)
   */
  OPT_KEEPINTVL,
  /**
   * number of retransmissions to be sent before declaring the other end to be
   * dead
   */
  OPT_KEEPCNT
}