/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.maven;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.codehaus.plexus.util.IOUtil;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.info.MigrationInfoImpl;
import com.googlecode.flyway.core.resolver.ResolvedMigration;

/**
 * Maven goal to aggregate a number of sql files for delivery.
 * 
 * @goal merge
 */
public class MergeMojo extends AbstractMigrationLoadingMojo {

	private static final Splitter VERSIONS_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

	/**
	 * Output directory of generated files.
	 * 
	 * @parameter default-value="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Name template of generated files.
	 * 
	 * @parameter default-value="merge.sql"
	 */
	private String outputName;

	/**
	 * The initial version from which to merge.
	 * <p>
	 * Also configurable with Maven or System Property: ${flyway.start}
	 * </p>
	 * 
	 * @parameter expression="${flyway.start}"
	 */
	private String start;

	/**
	 * The final version to which to merge. Defaults to latest version.
	 * <p>
	 * Also configurable with Maven or System Property: ${flyway.end}
	 * </p>
	 * 
	 * @parameter expression="${flyway.end}"
	 */
	private String end;

	/**
	 * A set of version to merge instead of an interval.
	 * <p>
	 * Also configurable with Maven or System Property: ${flyway.versions}
	 * </p>
	 * 
	 * @parameter expression="${flyway.versions}"
	 */
	private String versions;

	@Override
	protected void doExecuteWithMigrationConfig(Flyway flyway) throws Exception {
		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new IllegalStateException("Output directory could not be created");
		}
		MigrationVersionContainer versionContainer = buildVersionContainer();

		File output = new File(outputDirectory, outputName);
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), flyway.getEncoding()));
			MigrationInfo[] allInfos = flyway.info().all();
			for (MigrationInfo info : allInfos) {
				if (versionContainer.contains(info.getVersion())) {
					log.debug("Migration [" + info.getDescription() + "] is in interval");
					if (info.getType() != MigrationType.SQL) {
						throw new UnsupportedOperationException("Migration [" + info.getDescription()
								+ "] is not sql, only sql migrations are supported for aggregation");
					}
					writer.append("-- ").append(info.getScript());
					writer.append('\n');

					ResolvedMigration resolvedMigration = ((MigrationInfoImpl) info).getResolvedMigration();
					Reader reader = null;
					try {
						reader = new InputStreamReader(new FileInputStream(resolvedMigration.getPhysicalLocation()), flyway.getEncoding());
						IOUtil.copy(reader, writer);
					} finally {
						IOUtil.close(reader);
					}
					writer.append('\n');
				}
			}
		} finally {
			IOUtil.close(writer);
		}
	}

	protected MigrationVersionContainer buildVersionContainer() {
		MigrationVersionContainer container;
		if (start == null) {
			Preconditions.checkArgument(end == null, "End cannot be specified if start isn't, please use only versions property instead");
			Preconditions.checkArgument(versions != null, "Versions must be specified if start/end aren't");

			ImmutableSet.Builder<MigrationVersion> setBuilder = ImmutableSet.builder();
			for (String version : VERSIONS_SPLITTER.split(versions)) {
				setBuilder.add(new MigrationVersion(version));
			}
			final ImmutableSet<MigrationVersion> set = setBuilder.build();
			container = new MigrationVersionContainer() {
				public boolean contains(MigrationVersion version) {
					return set.contains(version);
				}
			};
		} else {
			Preconditions.checkArgument(versions == null, "Versions cannot be specified if start/end are");

			MigrationVersion startVersion = new MigrationVersion(start);
			MigrationVersion endVersion = end == null ? MigrationVersion.LATEST : new MigrationVersion(end);

			final Range<MigrationVersion> range = Ranges.closed(startVersion, endVersion);
			container = new MigrationVersionContainer() {
				public boolean contains(MigrationVersion version) {
					return range.contains(version);
				}
			};
		}
		return container;
	}

	private interface MigrationVersionContainer {
		boolean contains(MigrationVersion version);
	}
}
