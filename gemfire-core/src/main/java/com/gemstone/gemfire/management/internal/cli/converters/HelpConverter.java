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
package com.gemstone.gemfire.management.internal.cli.converters;

import java.util.List;

import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;

import com.gemstone.gemfire.management.internal.cli.commands.GfshHelpCommands;
import com.gemstone.gemfire.management.internal.cli.i18n.CliStrings;
import com.gemstone.gemfire.management.internal.cli.shell.Gfsh;

/**
 * {@link Converter} for {@link GfshHelpCommands#obtainHelp(String)}
 * 
 * @author Nikhil Jadhav
 *
 * @since 7.0
 */
public class HelpConverter implements Converter<String> {

  @Override
  public String convertFromText(String existingData, Class<?> dataType,
      String optionContext) {
    
    if (optionContext.equals(CliStrings.PARAM_CONTEXT_HELP)) {
      return existingData.replaceAll("\"", "").replaceAll("'", "");
    } else {
      return null;
    }
  }

  @Override
  public boolean getAllPossibleValues(List<Completion> completionCandidates,
      Class<?> dataType, String existingData, String optionContext,
      MethodTarget arg4) {

    List<String> commandNames = Gfsh.getCurrentInstance().obtainHelpCommandNames(existingData);
    
    for (String string : commandNames) {
      completionCandidates.add(new Completion(string));
    }
    if (completionCandidates.size() > 0) {
      return true;
    }
    return false;
  }

  @Override
  public boolean supports(Class<?> arg0, String optionContext) {
    if (String.class.isAssignableFrom(arg0)
        && optionContext.equals(CliStrings.PARAM_CONTEXT_HELP)) {
      return true;
    }
    return false;
  }
}
