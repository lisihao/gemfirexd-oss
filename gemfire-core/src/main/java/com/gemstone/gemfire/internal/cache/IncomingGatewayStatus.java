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

package com.gemstone.gemfire.internal.cache;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Class <code>IncomingGatewayStatus</code> provides status about an incoming
 * <code>Gateway</code> client from the server's perspective. This class is
 * used by the monitoring tool.
 *
 * @author Barry Oglesby
 *
 * @since 4.3
 */
public class IncomingGatewayStatus implements Serializable {
  private static final long serialVersionUID = -4579815367602658353L;

  protected String _memberId;
  protected int _socketPort;
  protected InetAddress _socketAddress;

  public IncomingGatewayStatus(String memberId, InetAddress socketAddress, int socketPort) {
    this._memberId = memberId;
    this._socketAddress = socketAddress;
    this._socketPort = socketPort;
  }

  public String getMemberId() {
    return this._memberId;
  }

  protected void setMemberId(String memberId) {
    this._memberId = memberId;
  }

  public int getSocketPort() {
    return this._socketPort;
  }

  protected void setSocketPort(int socketPort) {
    this._socketPort = socketPort;
  }

  public InetAddress getSocketAddress() {
    return this._socketAddress;
  }

  protected void setSocketAddress(InetAddress socketAddress) {
    this._socketAddress = socketAddress;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer
      .append("IncomingGatewayStatus[")
      .append("memberId=")
      .append(this._memberId)
      .append("; socketAddress=")
      .append(this._socketAddress)
      .append("; socketPort=")
      .append(this._socketPort)
      .append("]");
    return buffer.toString();
  }
}
