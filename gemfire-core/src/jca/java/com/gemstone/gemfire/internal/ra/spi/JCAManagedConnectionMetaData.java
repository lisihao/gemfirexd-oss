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
package com.gemstone.gemfire.internal.ra.spi;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;
/**
 * 
 * @author asif
 *
 */
public class JCAManagedConnectionMetaData implements ManagedConnectionMetaData
{
  private final String prodName;

  private final String version;

  private final String user;

  public JCAManagedConnectionMetaData(String prodName, String version,
      String user) {
    this.prodName = prodName;
    this.version = version;
    this.user = user;
  }

  
  public String getEISProductName() throws ResourceException
  {
    return this.prodName;
  }

  
  public String getEISProductVersion() throws ResourceException
  {

    return this.version;
  }

  
  public int getMaxConnections() throws ResourceException
  {

    return 0;
  }

  
  public String getUserName() throws ResourceException
  {
    return this.user;
  }

}
