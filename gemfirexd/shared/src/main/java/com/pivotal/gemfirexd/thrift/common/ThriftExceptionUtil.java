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

package com.pivotal.gemfirexd.thrift.common;

import java.rmi.ServerException;
import java.sql.ClientInfoStatus;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TTransportException;

import com.pivotal.gemfirexd.internal.shared.common.error.ClientExceptionUtil;
import com.pivotal.gemfirexd.internal.shared.common.reference.SQLState;
import com.pivotal.gemfirexd.thrift.GFXDException;
import com.pivotal.gemfirexd.thrift.GFXDExceptionData;

/**
 * This class provides utility routines for exceptions from Thrift layer.
 * 
 * @author swale
 * @since gfxd 1.1
 */
public abstract class ThriftExceptionUtil extends ClientExceptionUtil {

  protected ThriftExceptionUtil() {
    // no instance allowed
  }

  public static SQLException newSQLException(GFXDException gfxde) {
    GFXDExceptionData payload = gfxde.getExceptionData();
    SQLException sqle = newSQLException(payload, gfxde.getCause(),
        gfxde.getServerInfo());
    // since GFXDException is always a wrapper, no need to record the stack
    sqle.setStackTrace(gfxde.getStackTrace());
    // build next exceptions
    List<GFXDExceptionData> nextList = gfxde.getNextExceptions();
    SQLException current = sqle, next;
    if (nextList != null) {
      for (GFXDExceptionData nextData : nextList) {
        // check for server stack indicator
        if (SQLState.GFXD_SERVER_STACK_INDICATOR.equals(
            nextData.getSqlState())) {
          Throwable cause = sqle;
          while (cause.getCause() != null) {
            cause = cause.getCause();
          }
          cause.initCause(new ServerException(nextData.getReason()));
        }
        else {
          next = newSQLException(nextData, null, null);
          current.setNextException(next);
          current = next;
        }
      }
    }
    return sqle;
  }

  public static SQLException newSQLException(GFXDExceptionData payload,
      Throwable cause, String serverInfo) {
    String message = payload.getReason();
    if (serverInfo != null) {
      message = "(" + serverInfo + ") " + message;
    }
    return factory.getSQLException(message, payload.getSqlState(),
        payload.getSeverity(), null, cause);
  }

  public static SQLClientInfoException newSQLClientInfoException(
      String sqlState, Map<String, ClientInfoStatus> failedProperties,
      Throwable t, Object... args) {
    return new SQLClientInfoException(getMessageUtil().getCompleteMessage(
        sqlState, args), getSQLStateFromIdentifier(sqlState),
        getSeverityFromIdentifier(sqlState), failedProperties, t);
  }

  public static SQLWarning newSQLWarning(GFXDExceptionData payload,
      Throwable cause) {
    return new SQLWarning(payload.getReason(), payload.getSqlState(),
        payload.getSeverity(), cause);
  }

  public static GFXDException newGFXDException(String sqlState, Throwable t,
      String serverInfo, Object... args) {
    GFXDExceptionData payload = new GFXDExceptionData(getMessageUtil()
        .getCompleteMessage(sqlState, args),
        getSQLStateFromIdentifier(sqlState),
        getSeverityFromIdentifier(sqlState));
    GFXDException gfxde = new GFXDException(payload, serverInfo);
    if (t != null) {
      if (t instanceof GFXDException) {
        GFXDException next = (GFXDException)t;
        gfxde.addToNextExceptions(next.getExceptionData());
        if (next.getNextExceptions() != null) {
          gfxde.getNextExceptions().addAll(next.getNextExceptions());
        }
      }
      else {
        gfxde.initCause(t);
      }
    }
    return gfxde;
  }

  public static String getExceptionString(Throwable t) {
    String typeMessage = null;
    if (t instanceof TTransportException) {
      TTransportException tte = (TTransportException)t;
      switch (tte.getType()) {
        case TTransportException.ALREADY_OPEN:
          typeMessage = "SOCKET ALREADY OPEN";
          break;
        case TTransportException.END_OF_FILE:
          typeMessage = "SOCKET END OF TRANSMISSION";
          break;
        case TTransportException.NOT_OPEN:
          typeMessage = "SOCKET NOT OPEN";
          break;
        case TTransportException.TIMED_OUT:
          typeMessage = "SOCKET TIMED OUT";
          break;
        default:
          typeMessage = "UNKNOWN SOCKET ERROR";
          break;
      }
    }
    else if (t instanceof TProtocolException) {
      TProtocolException tpe = (TProtocolException)t;
      switch (tpe.getType()) {
        case TProtocolException.BAD_VERSION:
          typeMessage = "BAD PROTOCOL VERSION";
          break;
        case TProtocolException.INVALID_DATA:
          typeMessage = "INVALID DATA IN PROTOCOL";
          break;
        case TProtocolException.NEGATIVE_SIZE:
          typeMessage = "NEGATIVE SIZE IN PROTOCOL";
          break;
        case TProtocolException.NOT_IMPLEMENTED:
          typeMessage = "NOT IMPLEMENTED";
          break;
        case TProtocolException.SIZE_LIMIT:
          typeMessage = "PROTOCOL SIZE LIMIT HIT";
          break;
        default:
          typeMessage = "UNKNOWN PROTOCOL ERROR";
          break;
      }
    }
    else if (t instanceof TApplicationException) {
      TApplicationException tae = (TApplicationException)t;
      switch (tae.getType()) {
        case TApplicationException.BAD_SEQUENCE_ID:
          typeMessage = "BAD SEQUENCE ID";
          break;
        case TApplicationException.INTERNAL_ERROR:
          typeMessage = "INTERNAL ERROR";
          break;
        case TApplicationException.INVALID_MESSAGE_TYPE:
          typeMessage = "INVALID MESSAGE TYPE";
          break;
        case TApplicationException.MISSING_RESULT:
          typeMessage = "MISSING RESULT";
          break;
        case TApplicationException.PROTOCOL_ERROR:
          typeMessage = "PROTOCOL ERROR";
          break;
        case TApplicationException.UNKNOWN_METHOD:
          typeMessage = "UNKNOWN METHOD";
          break;
        case TApplicationException.WRONG_METHOD_NAME:
          typeMessage = "WRONG METHOD NAME";
          break;
        default:
          typeMessage = "UNKNOWN APPLICATION ERROR";
          break;
      }
    }
    String message = t.getLocalizedMessage();
    if (message == null || message.length() == 0) {
      message = t.getClass().getName();
    }
    if (typeMessage != null) {
      return message + " [" + typeMessage + ']';
    }
    else {
      return message;
    }
  }
}
