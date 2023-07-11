/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.core.extensibility;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.internal.license.Edition;

import java.math.BigInteger;

@RequiredArgsConstructor
@Getter
public enum LicenseType {
    TRIAL((byte) 0x00, Edition.ENTERPRISE, "trial"),

    PRO10((byte) 0x01, Edition.PRO, "10 schemas"),
    PRO20((byte) 0x02, Edition.PRO, "20 schemas"),
    PRO30((byte) 0x03, Edition.PRO, "30 schemas"),
    PRO40((byte) 0x04, Edition.PRO, "40 schemas"),
    PRO50((byte) 0x05, Edition.PRO, "50 schemas"),
    PRO60((byte) 0x06, Edition.PRO, "60 schemas"),
    PRO70((byte) 0x07, Edition.PRO, "70 schemas"),
    PRO80((byte) 0x08, Edition.PRO, "80 schemas"),
    PRO90((byte) 0x09, Edition.PRO, "90 schemas"),
    PRO100((byte) 0x0A, Edition.PRO, "100 schemas"),
    PROSITE((byte) 0x0F, Edition.PRO, "unlimited schemas"),

    ENTERPRISE10((byte) 0x11, Edition.ENTERPRISE, "10 schemas"),
    ENTERPRISE20((byte) 0x12, Edition.ENTERPRISE, "20 schemas"),
    ENTERPRISE30((byte) 0x13, Edition.ENTERPRISE, "30 schemas"),
    ENTERPRISE40((byte) 0x14, Edition.ENTERPRISE, "40 schemas"),
    ENTERPRISE50((byte) 0x15, Edition.ENTERPRISE, "50 schemas"),
    ENTERPRISE60((byte) 0x16, Edition.ENTERPRISE, "60 schemas"),
    ENTERPRISE70((byte) 0x17, Edition.ENTERPRISE, "70 schemas"),
    ENTERPRISE80((byte) 0x18, Edition.ENTERPRISE, "80 schemas"),
    ENTERPRISE90((byte) 0x19, Edition.ENTERPRISE, "90 schemas"),
    ENTERPRISE100((byte) 0x1A, Edition.ENTERPRISE, "100 schemas"),
    ENTERPRISESITE((byte) 0x1F, Edition.ENTERPRISE, "unlimited schemas"),

    PRO10_REDISTRIBUTABLE((byte) 0x21, Edition.PRO, "10 schemas, redistributable"),
    PRO20_REDISTRIBUTABLE((byte) 0x22, Edition.PRO, "20 schemas, redistributable"),
    PRO30_REDISTRIBUTABLE((byte) 0x23, Edition.PRO, "30 schemas, redistributable"),
    PRO40_REDISTRIBUTABLE((byte) 0x24, Edition.PRO, "40 schemas, redistributable"),
    PRO50_REDISTRIBUTABLE((byte) 0x25, Edition.PRO, "50 schemas, redistributable"),
    PRO60_REDISTRIBUTABLE((byte) 0x26, Edition.PRO, "60 schemas, redistributable"),
    PRO70_REDISTRIBUTABLE((byte) 0x27, Edition.PRO, "70 schemas, redistributable"),
    PRO80_REDISTRIBUTABLE((byte) 0x28, Edition.PRO, "80 schemas, redistributable"),
    PRO90_REDISTRIBUTABLE((byte) 0x29, Edition.PRO, "90 schemas, redistributable"),
    PRO100_REDISTRIBUTABLE((byte) 0x2A, Edition.PRO, "100 schemas, redistributable"),
    PROSITE_REDISTRIBUTABLE((byte) 0x2F, Edition.PRO, "unlimited schemas, redistributable"),

    ENTERPRISE10_REDISTRIBUTABLE((byte) 0x31, Edition.ENTERPRISE, "10 schemas, redistributable"),
    ENTERPRISE20_REDISTRIBUTABLE((byte) 0x32, Edition.ENTERPRISE, "20 schemas, redistributable"),
    ENTERPRISE30_REDISTRIBUTABLE((byte) 0x33, Edition.ENTERPRISE, "30 schemas, redistributable"),
    ENTERPRISE40_REDISTRIBUTABLE((byte) 0x34, Edition.ENTERPRISE, "40 schemas, redistributable"),
    ENTERPRISE50_REDISTRIBUTABLE((byte) 0x35, Edition.ENTERPRISE, "50 schemas, redistributable"),
    ENTERPRISE60_REDISTRIBUTABLE((byte) 0x36, Edition.ENTERPRISE, "60 schemas, redistributable"),
    ENTERPRISE70_REDISTRIBUTABLE((byte) 0x37, Edition.ENTERPRISE, "70 schemas, redistributable"),
    ENTERPRISE80_REDISTRIBUTABLE((byte) 0x38, Edition.ENTERPRISE, "80 schemas, redistributable"),
    ENTERPRISE90_REDISTRIBUTABLE((byte) 0x39, Edition.ENTERPRISE, "90 schemas, redistributable"),
    ENTERPRISE100_REDISTRIBUTABLE((byte) 0x3A, Edition.ENTERPRISE, "100 schemas, redistributable"),
    ENTERPRISESITE_REDISTRIBUTABLE((byte) 0x3F, Edition.ENTERPRISE, "unlimited schemas, redistributable"),

    TIER3_UNLIMITED((byte) 0x4F, Edition.TIER3, "unlimited"),

    TEAMS_TRIAL((byte) (0x1F | 0x80), Edition.ENTERPRISE, "trial"),
    TIER3_TRIAL((byte) (0x4F | 0x80), Edition.TIER3, "trial");

    private final byte code;
    private final Edition edition;
    private final String details;

    public static LicenseType fromCode(byte code) {
        for (LicenseType licenseType : values()) {
            if (licenseType.code == code) {
                return licenseType;
            }
        }
        throw new IllegalArgumentException("Unknown license code: 0x" + toHex(code));
    }

    private static String toHex(byte... array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    @Override
    public String toString() {
        return edition + " (" + details + ")";
    }
}