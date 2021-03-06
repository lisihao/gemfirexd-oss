/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.catalog.SYSALIASESRowFactory

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/*
 * Changes for GemFireXD distributed data platform (some marked by "GemStone changes")
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

package com.pivotal.gemfirexd.internal.impl.sql.catalog;

import java.sql.Types;

import com.pivotal.gemfirexd.internal.catalog.AliasInfo;
import com.pivotal.gemfirexd.internal.catalog.UUID;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.services.uuid.UUIDFactory;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.AliasDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.CatalogRowFactory;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DataDictionary;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.SystemColumn;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.TupleDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecRow;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecutionFactory;
import com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor;
import com.pivotal.gemfirexd.internal.iapi.types.DataValueFactory;
import com.pivotal.gemfirexd.internal.iapi.types.SQLChar;
import com.pivotal.gemfirexd.internal.iapi.types.SQLVarchar;
import com.pivotal.gemfirexd.internal.iapi.types.TypeId;


/**
 * Factory for creating a SYSALIASES row.
 *
 * Here are the directions for adding a new system supplied alias.
 * Misc:
 *  All system supplied aliases are class aliases at this point.
 *	Additional arrays will need to be added if we supply system
 *	aliases of other types.
 *	The preloadAliasIDs array is an array of hard coded UUIDs
 *	for the system supplied aliases.
 *	The preloadAliases array is the array of aliases
 *	for the system supplied aliases.  This array is in alphabetical
 *	order by package and class in Xena.  Each alias is the uppercase
 *	class name of the alias.
 *  The preloadJavaClassNames array is the array of full package.class
 *  names for the system supplied aliases.  This array is in alphabetical
 *	order by package and class in Xena.  
 *	SYSALIASES_NUM_BOOT_ROWS is the number of boot rows in sys.sysaliases
 *  in a new database.
 *
 *
 */

// GemStone changes BEGIN
public class SYSALIASESRowFactory extends CatalogRowFactory {

  public static final String TABLENAME_STRING = "SYSALIASES";

  public static final int SYSALIASES_COLUMN_COUNT = 10;

  public static final int SYSALIASES_ALIASID = 1;

  public static final int SYSALIASES_ALIAS = 2;

  public static final int SYSALIASES_SCHEMAID = 3;

  public static final int SYSALIASES_JAVACLASSNAME = 4;

  public static final int SYSALIASES_ALIASTYPE = 5;

  public static final int SYSALIASES_NAMESPACE = 6;

  public static final int SYSALIASES_SYSTEMALIAS = 7;

  public static final int SYSALIASES_ALIASINFO = 8;

  public static final int SYSALIASES_SPECIFIC_NAME = 9;

  public static final int SYSALIASES_SCHEMANAME = 10;
/*
class SYSALIASESRowFactory extends CatalogRowFactory
{

	private static final int		SYSALIASES_COLUMN_COUNT = 9;
	private static final int		SYSALIASES_ALIASID = 1;
	private static final int		SYSALIASES_ALIAS = 2;
	private static final int		SYSALIASES_SCHEMAID = 3;
	private static final int		SYSALIASES_JAVACLASSNAME = 4;
	private static final int		SYSALIASES_ALIASTYPE = 5;
	private static final int		SYSALIASES_NAMESPACE = 6;
	private static final int		SYSALIASES_SYSTEMALIAS = 7;
	public  static final int		SYSALIASES_ALIASINFO = 8;
	private static final int		SYSALIASES_SPECIFIC_NAME = 9;
*/
// GemStone changes END

 
	protected static final int		SYSALIASES_INDEX1_ID = 0;

	protected static final int		SYSALIASES_INDEX2_ID = 1;

	protected static final int		SYSALIASES_INDEX3_ID = 2;

	// null means all unique.
    private	static	final	boolean[]	uniqueness = null;

	private static int[][] indexColumnPositions =
	{
		{SYSALIASES_SCHEMAID, SYSALIASES_ALIAS, SYSALIASES_NAMESPACE},
		{SYSALIASES_ALIASID},
		{SYSALIASES_SCHEMAID, SYSALIASES_SPECIFIC_NAME},
	};

	private	static	final	String[]	uuids =
	{
		 "c013800d-00d7-ddbd-08ce-000a0a411400"	// catalog UUID
		,"c013800d-00d7-ddbd-75d4-000a0a411400"	// heap UUID
		,"c013800d-00d7-ddbe-b99d-000a0a411400"	// SYSALIASES_INDEX1
		,"c013800d-00d7-ddbe-c4e1-000a0a411400"	// SYSALIASES_INDEX2
		,"c013800d-00d7-ddbe-34ae-000a0a411400"	// SYSALIASES_INDEX3
	};

	/////////////////////////////////////////////////////////////////////////////
	//
	//	CONSTRUCTORS
	//
	/////////////////////////////////////////////////////////////////////////////

    SYSALIASESRowFactory(UUIDFactory uuidf, ExecutionFactory ef, DataValueFactory dvf)
	{
		super(uuidf,ef,dvf);
		initInfo(SYSALIASES_COLUMN_COUNT, "SYSALIASES", indexColumnPositions, uniqueness, uuids);
	}

	/////////////////////////////////////////////////////////////////////////////
	//
	//	METHODS
	//
	/////////////////////////////////////////////////////////////////////////////

  /**
	 * Make a SYSALIASES row
	 *
	 *
	 * @return	Row suitable for inserting into SYSALIASES.
	 *
	 * @exception   StandardException thrown on failure
	 */
	public ExecRow makeRow(TupleDescriptor	td, TupleDescriptor parent)
					throws StandardException 
	{
		DataValueDescriptor		col;
		String					schemaID = null;
		String					javaClassName = null;
		String					sAliasType = null;
		String					aliasID = null;
		String					aliasName = null;
		String					specificName = null;
		char					cAliasType = AliasInfo.ALIAS_TYPE_PROCEDURE_AS_CHAR;
		char					cNameSpace = AliasInfo.ALIAS_NAME_SPACE_PROCEDURE_AS_CHAR;
		boolean					systemAlias = false;
		AliasInfo				aliasInfo = null;

		if (td != null) {

			AliasDescriptor 		ad = (AliasDescriptor)td;
			aliasID	= ad.getUUID().toString();
			aliasName = ad.getDescriptorName();
			schemaID	= ad.getSchemaUUID().toString();
			javaClassName	= ad.getJavaClassName();
			cAliasType = ad.getAliasType();
			cNameSpace = ad.getNameSpace();
			systemAlias = ad.getSystemAlias();
			aliasInfo = ad.getAliasInfo();
			specificName = ad.getSpecificName();

			char[] charArray = new char[1];
			charArray[0] = cAliasType;
			sAliasType = new String(charArray);

			if (SanityManager.DEBUG)
			{
				switch (cAliasType)
				{
					case AliasInfo.ALIAS_TYPE_PROCEDURE_AS_CHAR:
					case AliasInfo.ALIAS_TYPE_FUNCTION_AS_CHAR:
					case AliasInfo.ALIAS_TYPE_SYNONYM_AS_CHAR:
					case AliasInfo.ALIAS_TYPE_UDT_AS_CHAR:
//Gemstone changes Begin
					case AliasInfo.ALIAS_TYPE_RESULT_PROCESSOR_AS_CHAR:  
//Gemstone changes End					  
						break;

					default:
						SanityManager.THROWASSERT(
							"Unexpected value (" + cAliasType +
							") for aliasType");
				}
			}
		}


		/* Insert info into sysaliases */

		/* RESOLVE - It would be nice to require less knowledge about sysaliases
		 * and have this be more table driven.
		 */

		/* Build the row to insert */
		ExecRow row = getExecutionFactory().getValueRow(SYSALIASES_COLUMN_COUNT);

		/* 1st column is ALIASID (UUID - char(36)) */
		row.setColumn(SYSALIASES_ALIASID, new SQLChar(aliasID));

		/* 2nd column is ALIAS (varchar(128))) */
		row.setColumn(SYSALIASES_ALIAS, new SQLVarchar(aliasName));
		//		System.out.println(" added row-- " + aliasName);

		/* 3rd column is SCHEMAID (UUID - char(36)) */
		row.setColumn(SYSALIASES_SCHEMAID, new SQLChar(schemaID));

		/* 4th column is JAVACLASSNAME (longvarchar) */
		row.setColumn(SYSALIASES_JAVACLASSNAME, dvf.getLongvarcharDataValue(javaClassName));

		/* 5th column is ALIASTYPE (char(1)) */
		row.setColumn(SYSALIASES_ALIASTYPE, new SQLChar(sAliasType));

		/* 6th column is NAMESPACE (char(1)) */
		String sNameSpace = new String(new char[] { cNameSpace });

		row.setColumn
			(SYSALIASES_NAMESPACE, new SQLChar(sNameSpace));


		/* 7th column is SYSTEMALIAS (boolean) */
		row.setColumn
			(SYSALIASES_SYSTEMALIAS, dvf.getDataValue(systemAlias));

		/* 8th column is ALIASINFO (com.pivotal.gemfirexd.internal.catalog.AliasInfo) */
		row.setColumn(SYSALIASES_ALIASINFO, 
			dvf.getDataValue(aliasInfo));

		/* 9th column is specific name */
		row.setColumn
			(SYSALIASES_SPECIFIC_NAME, new SQLVarchar(specificName));

// GemStone changes BEGIN
    /* 10th column is schema name */
    String schemaName = null;
    if (td != null) {
      AliasDescriptor ad = (AliasDescriptor)td;
      com.pivotal.gemfirexd.internal.impl.sql.catalog.DataDictionaryImpl dd =
        (com.pivotal.gemfirexd.internal.impl.sql.catalog.DataDictionaryImpl)ad
          .getDataDictionary();
      com.pivotal.gemfirexd.internal.iapi.store.access.TransactionController tc =
        dd.getTransactionExecute();
      schemaName = dd.getSchemaDescriptor(ad.getSchemaUUID(), tc)
          .getSchemaName();
    }
    row.setColumn(SYSALIASES_SCHEMANAME, new SQLVarchar(schemaName));
// GemStone changes END

		return row;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	//	ABSTRACT METHODS TO BE IMPLEMENTED BY CHILDREN OF CatalogRowFactory
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Make a AliasDescriptor out of a SYSALIASES row
	 *
	 * @param row a SYSALIASES row
	 * @param parentTupleDescriptor	Null for this kind of descriptor.
	 * @param dd dataDictionary
	 *
	 * @exception   StandardException thrown on failure
	 */
	public TupleDescriptor buildDescriptor(
		ExecRow					row,
		TupleDescriptor			parentTupleDescriptor,
		DataDictionary 			dd )
					throws StandardException
	{
		if (SanityManager.DEBUG)
		{
			SanityManager.ASSERT(
				row.nColumns() == SYSALIASES_COLUMN_COUNT, 
				"Wrong number of columns for a SYSALIASES row");
		}

		char				cAliasType;
		char				cNameSpace;
		DataValueDescriptor	col;
		String				aliasID;
		UUID				aliasUUID;
		String				aliasName;
		String				javaClassName;
		String				sAliasType;
		String				sNameSpace;
		String				typeStr;
		boolean				systemAlias = false;
		AliasInfo			aliasInfo = null;

		/* 1st column is ALIASID (UUID - char(36)) */
		col = row.getColumn(SYSALIASES_ALIASID);
		aliasID = col.getString();
		aliasUUID = getUUIDFactory().recreateUUID(aliasID);

		/* 2nd column is ALIAS (varchar(128)) */
		col = row.getColumn(SYSALIASES_ALIAS);
		aliasName = col.getString();

		/* 3rd column is SCHEMAID (UUID - char(36)) */
		col = row.getColumn(SYSALIASES_SCHEMAID);
		UUID schemaUUID = col.isNull() ? null : getUUIDFactory().recreateUUID(col.getString());

		/* 4th column is JAVACLASSNAME (longvarchar) */
		col = row.getColumn(SYSALIASES_JAVACLASSNAME);
		javaClassName = col.getString();

		/* 5th column is ALIASTYPE (char(1)) */
		col = row.getColumn(SYSALIASES_ALIASTYPE);
		sAliasType = col.getString();
		if (SanityManager.DEBUG)
		{
			SanityManager.ASSERT(sAliasType.length() == 1, 
				"Fifth column (aliastype) type incorrect");
			switch (sAliasType.charAt(0))
			{
				case AliasInfo.ALIAS_TYPE_PROCEDURE_AS_CHAR: 
				case AliasInfo.ALIAS_TYPE_FUNCTION_AS_CHAR: 
				case AliasInfo.ALIAS_TYPE_SYNONYM_AS_CHAR: 
				case AliasInfo.ALIAS_TYPE_UDT_AS_CHAR: 
//Gemstone changes Begin
				case AliasInfo.ALIAS_TYPE_RESULT_PROCESSOR_AS_CHAR:  
//Gemstone changes End				  
					break;

				default: 
					SanityManager.THROWASSERT("Invalid type value '"
							+sAliasType+ "' for  alias");
			}
		}

		cAliasType = sAliasType.charAt(0);

		/* 6th column is NAMESPACE (char(1)) */
		col = row.getColumn(SYSALIASES_NAMESPACE);
		sNameSpace = col.getString();
		if (SanityManager.DEBUG)
		{
			SanityManager.ASSERT(sNameSpace.length() == 1, 
				"Sixth column (namespace) type incorrect");
			switch (sNameSpace.charAt(0))
			{
				case AliasInfo.ALIAS_NAME_SPACE_PROCEDURE_AS_CHAR: 
				case AliasInfo.ALIAS_NAME_SPACE_FUNCTION_AS_CHAR: 
				case AliasInfo.ALIAS_TYPE_SYNONYM_AS_CHAR:
				case AliasInfo.ALIAS_TYPE_UDT_AS_CHAR: 
//Gemfire changes Begin
				case AliasInfo.ALIAS_NAME_SPACE_RESULT_PROCESSOR_AS_CHAR: 
//Gemfire changes End				  
					break;

				default: 
					SanityManager.THROWASSERT("Invalid type value '"
							+sNameSpace+ "' for  alias");
			}
		}

		cNameSpace = sNameSpace.charAt(0);


		/* 7th column is SYSTEMALIAS (boolean) */
		col = row.getColumn(SYSALIASES_SYSTEMALIAS);
		systemAlias = col.getBoolean();

		/* 8th column is ALIASINFO (com.pivotal.gemfirexd.internal.catalog.AliasInfo) */
		col = row.getColumn(SYSALIASES_ALIASINFO);
		aliasInfo = (AliasInfo) col.getObject();

		/* 9th column is specific name */
		col = row.getColumn(SYSALIASES_SPECIFIC_NAME);
		String specificName = col.getString();


		/* now build and return the descriptor */
		return new AliasDescriptor(dd, aliasUUID, aliasName,
										schemaUUID, javaClassName, cAliasType,
										cNameSpace, systemAlias,
										aliasInfo, specificName);
	}

    /**
     * Builds a list of columns suitable for creating this Catalog.
     * DERBY-1734 fixed an issue where older code created the
     * BOOLEAN column SYSTEMALIAS with maximum length 0 instead of 1.
     * DERBY-1742 was opened to track if upgrade changes are needed.
     *
     *
     * @return array of SystemColumn suitable for making this catalog.
     */
    public SystemColumn[]   buildColumnList()
        throws StandardException
    {
      return new SystemColumn[] {
        
        SystemColumnImpl.getUUIDColumn("ALIASID", false),
        SystemColumnImpl.getIdentifierColumn("ALIAS", false),
        SystemColumnImpl.getUUIDColumn("SCHEMAID", true),
        SystemColumnImpl.getColumn("JAVACLASSNAME",
                java.sql.Types.LONGVARCHAR, false, TypeId.LONGVARCHAR_MAXWIDTH),
        SystemColumnImpl.getIndicatorColumn("ALIASTYPE"),
        SystemColumnImpl.getIndicatorColumn("NAMESPACE"),
        SystemColumnImpl.getColumn("SYSTEMALIAS",
                Types.BOOLEAN, false),
        SystemColumnImpl.getJavaColumn("ALIASINFO",
                "com.pivotal.gemfirexd.internal.catalog.AliasInfo", true),
// GemStone changes BEGIN
        SystemColumnImpl.getIdentifierColumn("SPECIFICNAME", false),
        SystemColumnImpl.getIdentifierColumn("ALIASSCHEMANAME", false)
        /* SystemColumnImpl.getIdentifierColumn("SPECIFICNAME", false) */
// GemStone changes END
        };
    }
}
