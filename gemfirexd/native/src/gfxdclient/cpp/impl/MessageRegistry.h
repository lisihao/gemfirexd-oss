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

/**
 *  Created on: 7 Jun 2014
 *      Author: swale
 */

#ifndef MESSAGEREGISTRY_H_
#define MESSAGEREGISTRY_H_

#include "ThreadSafeMap.h"
#include "common/MessageBase.h"

namespace com
{
  namespace pivotal
  {
    namespace gemfirexd
    {
      namespace impl
      {

        /**
         * Singleton class to register all messages and lookup as required.
         */
        class MessageRegistry
        {
        private:
          ThreadSafeMap<const std::string, MessageBase*> m_allMessages;

          MessageRegistry();

          static MessageRegistry s_instance;

        public:

          inline static MessageRegistry& instance() throw ()
          {
            return s_instance;
          }

          void addMessage(MessageBase& msg);
          void removeMessage(const MessageBase& msg);
          MessageBase* lookup(const std::string& messageId) const;
        };

      } /* namespace impl */
    } /* namespace gemfirexd */
  } /* namespace pivotal */
} /* namespace com */

#endif /* MESSAGEREGISTRY_H_ */
