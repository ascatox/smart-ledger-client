/*
 *
 *  Copyright 2016,2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package eu.faredge.smartledger.client.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;
import eu.faredge.smartledger.client.model.RecordDCM;
import eu.faredge.smartledger.client.model.RecordDSM;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.sdk.helper.Utils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;

public class Util {

    public static final String REGEX_MACADDRESS = "^((([0-9A-Fa-f]{2}:){5})|(([0-9A-Fa-f]{2}-){5}))[0-9A-Fa-f]{2}$\n";

    /**
     * Private constructor to prevent instantiation.
     */
    private Util() {
    }

    /**
     * Generate a targz inputstream from source folder.
     *
     * @param src        Source location
     * @param pathPrefix prefix to add to the all files found.
     * @return return inputstream.
     * @throws IOException
     */
    public static InputStream generateTarGzInputStream(File src, String pathPrefix) throws IOException {
        File sourceDirectory = src;

        ByteArrayOutputStream bos = new ByteArrayOutputStream(500000);

        String sourcePath = sourceDirectory.getAbsolutePath();

        TarArchiveOutputStream archiveOutputStream = new TarArchiveOutputStream(new GzipCompressorOutputStream(new
                BufferedOutputStream(bos)));
        archiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        try {
            Collection<File> childrenFiles = org.apache.commons.io.FileUtils.listFiles(sourceDirectory, null, true);

            ArchiveEntry archiveEntry;
            FileInputStream fileInputStream;
            for (File childFile : childrenFiles) {
                String childPath = childFile.getAbsolutePath();
                String relativePath = childPath.substring((sourcePath.length() + 1), childPath.length());

                if (pathPrefix != null) {
                    relativePath = Utils.combinePaths(pathPrefix, relativePath);
                }

                relativePath = FilenameUtils.separatorsToUnix(relativePath);

                archiveEntry = new TarArchiveEntry(childFile, relativePath);
                fileInputStream = new FileInputStream(childFile);
                archiveOutputStream.putArchiveEntry(archiveEntry);

                try {
                    IOUtils.copy(fileInputStream, archiveOutputStream);
                } finally {
                    IOUtils.closeQuietly(fileInputStream);
                    archiveOutputStream.closeArchiveEntry();
                }
            }
        } finally {
            IOUtils.closeQuietly(archiveOutputStream);
        }

        return new ByteArrayInputStream(bos.toByteArray());
    }

    public static File findFileSk(File directory) {

        File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));

        if (null == matches) {
            throw new RuntimeException(format("Matches returned null does %s directory exist?", directory
                    .getAbsoluteFile().getName()));
        }

        if (matches.length != 1) {
            throw new RuntimeException(format("Expected in %s only 1 sk file but found %d", directory.getAbsoluteFile
                    ().getName(), matches.length));
        }

        return matches[0];

    }

    public static void out(String format, Object... args) {

        System.err.flush();
        System.out.flush();

        System.out.println(format(format, args));
        System.err.flush();
        System.out.flush();

    }

    public static void fail(String message) {
        if (message == null) {
            throw new RuntimeException();
        } else {
            throw new RuntimeException(message);
        }
    }

    /**
     * Fabric Certificate authority information
     * Contains information for the Fabric certificate authority
     */
    public static class HFCAInfo {

        private final String caName;
        private final String caChain;

        public HFCAInfo(String caName, String caChain) {
            this.caName = caName;
            this.caChain = caChain;
        }

        /**
         * The CAName for the Fabric Certificate Authority.
         *
         * @return The CA Name.
         */

        public String getCAName() {
            return caName;
        }

        /**
         * The Certificate Authority's Certificate Chain.
         *
         * @return Certificate Chain in X509 PEM format.
         */

        public String getCACertificateChain() {
            return caChain;
        }
    }

    /**
     * Transform payloads in DSM with Array Structure payload[0] = peer's name payload owner
     * payload[1] = Data coming from peer
     *
     * @param payloads
     * @return
     */

    public static List<DSM> extractDSMFromPayloads(List<String[]> payloads) {
        List<DSM> dsms = new ArrayList<>();
        payloads.stream().forEach(val -> {
            String dsmString = val[1];
            Util.out(dsmString);
            if (null != dsmString && !StringUtils.isBlank(dsmString)) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    RecordDSM[] recordDSMs = mapper.readValue(dsmString, RecordDSM[].class);
                    for (RecordDSM recordDSM : recordDSMs) {
                        if (null != recordDSM && null != recordDSM.getRecord())
                            dsms.add(recordDSM.getRecord());
                    }
                } catch (IOException e) {
                    Util.fail(e.getMessage());
                }
            }
        });
        return dsms;
    }

    /**
     * Transform payloads in DCM with Array Structure payload[0] = peer's name payload owner
     * payload[1] = Data coming from peer
     *
     * @param payloads
     * @return
     */
    public static List<DCM> extractDCMFromPayloads(List<String[]> payloads) {
        List<DCM> dcms = new ArrayList<>();
        payloads.stream().forEach(val -> {
            String dcmString = val[1];
            Util.out(dcmString);
            if (null != dcmString && !StringUtils.isBlank(dcmString)) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    RecordDCM[] recordDCMs = mapper.readValue(dcmString, RecordDCM[].class);
                    for (RecordDCM recordDCM : recordDCMs) {
                        if (null != recordDCM && null != recordDCM.getRecord())
                            dcms.add(recordDCM.getRecord());
                    }
                } catch (IOException e) {
                    Util.fail(e.getMessage());
                }
            }
        });
        return dcms;
    }


    public static boolean validateUri(String uri) throws IllegalArgumentException {
        if (StringUtils.isEmpty(uri)) throw new IllegalArgumentException("uri cannot be empty");
        final URL url;
        try {
            url = new URL(uri);
            return true;
        } catch (Exception e1) {
            return false;
        }
    }

    public static boolean validateMacAddress(String macAddress) throws IllegalArgumentException {
        if (StringUtils.isEmpty(macAddress)) throw new IllegalArgumentException("macAddress cannot be empty");
        return macAddress.matches(REGEX_MACADDRESS);
    }
}
