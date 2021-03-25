/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resolver.tokenizer.SqlParser;
import org.flywaydb.core.internal.util.BomFilter;
import org.flywaydb.core.internal.util.IOUtils;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

public class ChecksumCalculator {
    private ChecksumCalculator() {
    }

    /**
     * Calculates the checksum of the resource.
     * The checksum is encoding and line-ending independent.
     *
     * @return The crc-32 checksum of the bytes.
     */
    public static int calculate( @Nonnull LoadableResource loadableResource ) {
        return calculateChecksumForResource( loadableResource );
    }

    /**
     * Calculates the checksum of the resource.
     * The checksum is encoding and line-ending independent.
     *
     * This variant will also create a SQL specific checksum which wont be dependant on the casing of SQL keywords
     * It also won't be affected by different whitespace
     *
     * @return The crc-32 checksum of the bytes.
     */
    public static int calculateForSql( @Nonnull LoadableResource loadableResource ) {
        return calculateChecksumForString( SqlParser.parse( loadableResource ) );
    }

    /**
     * Calculates the checksum of these resources.
     * The checksum is encoding and line-ending independent.
     *
     * @return A Map containing the checksum of each resource
     */
    public static Map<LoadableResource, Integer> calculate( @Nonnull LoadableResource... loadableResources ) {
        if ( loadableResources.length == 0 ) {
            throw new FlywayException( "Checksum compute was invoked with empty array" );
        }

        Map<LoadableResource, Integer> checksumMap = new LinkedHashMap<>();
        for ( LoadableResource loadableResource : loadableResources ) {
            checksumMap.put( loadableResource, calculateChecksumForResource( loadableResource ) );
        }

        return checksumMap;
    }

    private static int calculateChecksumForResource( LoadableResource resource ) {
        final CRC32 crc32 = new CRC32();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader( resource.read(), 4096 );
            String line = bufferedReader.readLine();

            if ( line != null ) {
                line = BomFilter.FilterBomFromString( line );
                do {
                    //noinspection Since15
                    crc32.update( line.getBytes( StandardCharsets.UTF_8 ) );
                } while ( ( line = bufferedReader.readLine() ) != null );
            }
        } catch ( IOException e ) {
            throw new FlywayException(
                "Unable to calculate checksum of " + resource.getFilename() + "\r\n" + e.getMessage(),
                e
            );
        } finally {
            IOUtils.close( bufferedReader );
        }

        return (int) crc32.getValue();
    }

    private static int calculateChecksumForString( String str ) {
        byte[] file = str.getBytes( StandardCharsets.UTF_8 );

        final CRC32 crc32 = new CRC32();
        //noinspection Since15
        crc32.update( file );

        return (int) crc32.getValue();
    }
}