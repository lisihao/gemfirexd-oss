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
package com.gemstone.gemfire.codeAnalysis.decode.cp;
import java.io.*;

public class CpNameAndType extends Cp {
    int name_index;     // utf8 unqualified field/method name
    int descriptor_index; // utf8
    CpNameAndType( DataInputStream source ) throws IOException {
        name_index = source.readUnsignedShort();
        descriptor_index = source.readUnsignedShort();
    }
}