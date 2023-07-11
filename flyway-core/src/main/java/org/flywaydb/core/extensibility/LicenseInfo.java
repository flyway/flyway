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

import lombok.CustomLog;
import lombok.Getter;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.StringUtils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;

import static java.time.temporal.ChronoUnit.DAYS;

@CustomLog
public class LicenseInfo {
    @Getter
    private final LicenseType licenseType;
    @Getter
    private final String licensedTo;
    @Getter
    private final Date validUntil;
    @Getter
    private final boolean trial;

    @Getter
    private boolean expired = false;

    private static final String DECRYPTION_KEY = "30820122300d06092a864886f70d01010105000382010f003082010a02820101009" +
            "fbebb4eb38e68c758ff04f2ca68f6c37660eb5b821aabbfac7c221c54debec144edacc9f95005f8070f0ee20fc54c8e913ae859e" +
            "2a96864ebe7d153a092e12379cfae26ba615f778db674452d0466c4259faadce1772b3e4b71f4a63c8c6856e093e9e279c9f0834" +
            "b986606e223c691cfddba18e5cfc54d0f50f1c44eeafefaad2e805edd67946d69902a0fe1d119e7814a5add199ba95ffa9e23182" +
            "40040219a96ba1f3a55a96359d2c590b3050f77f5b9950d04f3e77aa891578e0800fb16e4d122bcdd3f422cb846f982a5177b803" +
            "d3fbfd45b4c61d6182ca429b85c77bed678fd345bcbae01c51aef2ecf0bd1c3f85aeebe60380933501cf0b98eca97670203010001";

    private LicenseInfo(LicenseType licenseType, Date validUntil, String licensedTo, boolean trial) {
        this.licenseType = licenseType;
        this.validUntil = validUntil;
        this.licensedTo = licensedTo;
        this.trial = trial;
    }

    public static LicenseInfo create(String licenseKey) {
        validateLicenseKey(licenseKey);
        LicenseInfo licenseInfo = extractLicenseInfo(licenseKey);
        LicenseType licenseType = licenseInfo.getLicenseType();

        if (licenseInfo.getValidUntil().before(new Date())) {
            licenseInfo.expired = true;
        }
        VersionPrinter.EDITION = licenseType.getEdition();
        return licenseInfo;
    }

    public static LicenseInfo extractLicenseInfo(String licenseKey) {
        byte[] decrypted = rsaDecrypt(fromHex(licenseKey.substring(4)), rsaPublicKey(fromHex(DECRYPTION_KEY)));
        int year = 2000 + decrypted[1];
        int month = decrypted[2];
        int day = decrypted[3];
        LicenseType licenseType = LicenseType.fromCode(decrypted[0]);
        Date validUntil = DateUtils.toDate(year, month, day);
        String licensedTo = new String(Arrays.copyOfRange(decrypted, 4, decrypted.length), StandardCharsets.UTF_8);
        return new LicenseInfo(licenseType, validUntil, licensedTo, hasTrialBit(licenseType.getCode()) || licenseType.getCode() == 0);
    }

    private static boolean hasTrialBit(byte input) {
        return ((input & 0xff) >> 7) == 1;
    }

    public void print() {
        LOG.info("Licensed to " + getLicensedTo() + " until " + DateUtils.toDateString(getValidUntil()));
        if (Edition.PRO.equals(licenseType.getEdition())) {
            LOG.info("Your Flyway license is upgraded to Flyway Teams.");
        }
        if (isTrial()) {
            LOG.warn("You are using a limited Flyway trial license, valid until " + getValidUntil() + ". " +
                             " In " + getRemainingDaysString() + " you must either upgrade to a full " + VersionPrinter.EDITION + " license or downgrade to " + Edition.COMMUNITY + ".");
        }
        LOG.info("");
    }

    private static void validateLicenseKey(String licenseKey) {
        if (!StringUtils.hasLength(licenseKey)) {
            throw new FlywayMissingLicenseKeyException();
        }
        if (licenseKey.matches("[0-9]{3}-[0-9]{3}-[0-9]{6}-[A-Z0-9]{4}")) {
            throw new FlywayRedgateLicenseKeyException();
        }
        if (!licenseKey.matches("FL01[A-F0-9]{512}")) {
            throw new FlywayInvalidLicenseKeyException();
        }
    }

    public long getRemainingDays() {
        return DAYS.between(new Date().toInstant(), validUntil.toInstant());
    }

    public String getRemainingDaysString() {
        long daysRemaining = getRemainingDays();
        return daysRemaining + (daysRemaining == 1 ? " day" : " days");
    }

    private static PublicKey rsaPublicKey(byte[] publicKey) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
        } catch (GeneralSecurityException e) {
            throw new FlywayException("Unable to load the license decryption key", e);
        }
    }

    private static byte[] rsaDecrypt(byte[] encrypted, Key key) {
        try {
            Cipher rsa = Cipher.getInstance("RSA/ECB/Pkcs1Padding");
            rsa.init(Cipher.DECRYPT_MODE, key);
            return rsa.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            throw new FlywayInvalidLicenseKeyException("Unable to decrypt license", e);
        }
    }

    /**
     * Converts a string of hexadecimal characters into a byte array.
     */
    public static byte[] fromHex(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return binary;
    }
}