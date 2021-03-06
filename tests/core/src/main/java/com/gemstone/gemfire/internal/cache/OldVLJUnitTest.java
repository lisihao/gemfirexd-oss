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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import com.gemstone.gemfire.internal.InternalDataSerializer;

public class OldVLJUnitTest extends TestCase {
  private ByteArrayOutputStream baos;
  private DataOutputStream dos;

  private DataOutput createDOS() {
    this.baos = new ByteArrayOutputStream(32);
    this.dos = new DataOutputStream(baos);
    return dos;
  }

  private DataInput createDIS() throws IOException {
    this.dos.close();
    ByteArrayInputStream bais = new ByteArrayInputStream(this.baos
        .toByteArray());
    return new DataInputStream(bais);
  }

  public void testMinByte() throws IOException {
    InternalDataSerializer.writeVLOld(1, createDOS());
    assertEquals(1, InternalDataSerializer.readVLOld(createDIS()));
  }

  public void testMaxByte() throws IOException {
    InternalDataSerializer.writeVLOld(125, createDOS());
    assertEquals(125, InternalDataSerializer.readVLOld(createDIS()));
  }

  public void testMinShort() throws IOException {
    InternalDataSerializer.writeVLOld(126, createDOS());
    assertEquals(126, InternalDataSerializer.readVLOld(createDIS()));
  }

  public void testMaxShort() throws IOException {
    InternalDataSerializer.writeVLOld(0x7fff, createDOS());
    assertEquals(0x7fff, InternalDataSerializer.readVLOld(createDIS()));
  }

  public void testMinInt() throws IOException {
    InternalDataSerializer.writeVLOld(0x7fff + 1, createDOS());
    assertEquals(0x7fff + 1, InternalDataSerializer.readVLOld(createDIS()));
  }

  public void testMaxInt() throws IOException {
    InternalDataSerializer.writeVLOld(0x7fffffff, createDOS());
    assertEquals(0x7fffffff, InternalDataSerializer.readVLOld(createDIS()));
  }

  public void testMinLong() throws IOException {
    InternalDataSerializer.writeVLOld(0x7fffffffL + 1, createDOS());
    assertEquals(0x7fffffffL + 1, InternalDataSerializer.readVLOld(createDIS()));
  }

  public void testMaxLong() throws IOException {
    InternalDataSerializer.writeVLOld(Long.MAX_VALUE, createDOS());
    assertEquals(Long.MAX_VALUE, InternalDataSerializer.readVLOld(createDIS()));
  }

}
