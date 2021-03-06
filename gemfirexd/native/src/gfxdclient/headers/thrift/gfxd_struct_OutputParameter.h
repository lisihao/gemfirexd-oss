/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */

/*
 * Changes for GemFireXD distributed data platform.
 *
 * Portions Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
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

#ifndef GFXD_STRUCT_OUTPUTPARAMETER_H
#define GFXD_STRUCT_OUTPUTPARAMETER_H


#include "gfxd_types.h"

#include "gfxd_struct_FieldDescriptor.h"
#include "gfxd_struct_Decimal.h"
#include "gfxd_struct_Timestamp.h"
#include "gfxd_struct_FieldValue.h"
#include "gfxd_struct_PDXNode.h"
#include "gfxd_struct_PDXObject.h"
#include "gfxd_struct_PDXSchemaNode.h"
#include "gfxd_struct_PDXSchema.h"
#include "gfxd_struct_JSONField.h"
#include "gfxd_struct_JSONNode.h"
#include "gfxd_struct_JSONObject.h"
#include "gfxd_struct_BlobChunk.h"
#include "gfxd_struct_ClobChunk.h"
#include "gfxd_struct_ServiceMetaData.h"
#include "gfxd_struct_ServiceMetaDataArgs.h"
#include "gfxd_struct_OpenConnectionArgs.h"
#include "gfxd_struct_ConnectionProperties.h"
#include "gfxd_struct_HostAddress.h"
#include "gfxd_struct_GFXDExceptionData.h"
#include "gfxd_struct_StatementAttrs.h"
#include "gfxd_struct_DateTime.h"
#include "gfxd_struct_ColumnValue.h"
#include "gfxd_struct_ColumnDescriptor.h"
#include "gfxd_struct_Row.h"

namespace com { namespace pivotal { namespace gemfirexd { namespace thrift {

typedef struct _OutputParameter__isset {
  _OutputParameter__isset() : scale(false), typeName(false) {}
  bool scale;
  bool typeName;
} _OutputParameter__isset;

class OutputParameter {
 public:

  static const char* ascii_fingerprint; // = "69ED04DC035A4CF78CE4B470902F18B3";
  static const uint8_t binary_fingerprint[16]; // = {0x69,0xED,0x04,0xDC,0x03,0x5A,0x4C,0xF7,0x8C,0xE4,0xB4,0x70,0x90,0x2F,0x18,0xB3};

  OutputParameter() : type(GFXDType::OTHER), scale(0), typeName() {
  }

  virtual ~OutputParameter() throw() {}

  GFXDType::type type;
  int32_t scale;
  std::string typeName;

  _OutputParameter__isset __isset;

  void __set_type(const GFXDType::type val) {
    type = val;
  }

  void __set_scale(const int32_t val) {
    scale = val;
    __isset.scale = true;
  }

  void __set_typeName(const std::string& val) {
    typeName = val;
    __isset.typeName = true;
  }

  bool operator == (const OutputParameter & rhs) const
  {
    if (!(type == rhs.type))
      return false;
    if (__isset.scale != rhs.__isset.scale)
      return false;
    else if (__isset.scale && !(scale == rhs.scale))
      return false;
    if (__isset.typeName != rhs.__isset.typeName)
      return false;
    else if (__isset.typeName && !(typeName == rhs.typeName))
      return false;
    return true;
  }
  bool operator != (const OutputParameter &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const OutputParameter & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

void swap(OutputParameter &a, OutputParameter &b);

}}}} // namespace

#endif
