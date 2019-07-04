/*
 * Copyright 2010-2019 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.database.cloudspanner;

import java.sql.SQLException;
import java.util.Comparator;

/**
 * Comparator for CloudSpannerTables that will sort interleaved tables so that child tables will always come before parent tables.
 * @author loite
 *
 */
public class InterleavedTableComparator implements Comparator<CloudSpannerTable>
{

	/**
	 * This method compares two Cloud Spanner tables to see if o1 is interleaved in o2, so that o1 will be sorted before o2, or vice versa. This method can throw a {@link RuntimeException} containing a {@link SQLException} as its cause.
	 * @see #compare(CloudSpannerTable, CloudSpannerTable)
	 */
	@Override
	public int compare(CloudSpannerTable o1, CloudSpannerTable o2) throws RuntimeException
	{
		try {
			if(o1.isInterleavedIn(o2))
				return -1;
			if(o2.isInterleavedIn(o1))
				return 1;
		}
		catch(SQLException e) {
			throw new RuntimeException(e);
		}
		return 0;
	}

}
