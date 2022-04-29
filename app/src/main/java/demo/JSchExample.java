package demo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipOutputStream;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class JSchExample {
    public static void init(String host, String user, String pass, String dirToDownload, String outFile) {
        try {
            JSch sshClient = new JSch();
            JSch.setConfig("server_host_key", JSch.getConfig("server_host_key") + ",ssh-rsa");
            JSch.setConfig("PubkeyAcceptedAlgorithms", JSch.getConfig("PubkeyAcceptedAlgorithms") + ",ssh-rsa");

            Session session = sshClient.getSession(user, host, 22);
            session.setPassword(pass);

            var config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            // sshClient.setKnownHosts("~/.ssh/known_hosts");
            session.connect();

            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            sftpChannel.setFilenameEncoding(StandardCharsets.ISO_8859_1);// "ISO-8859-1");
            // var fileAndFolderList = sftpChannel.ls("/var/www/html/teste/marcos/poo");

            var startTime = System.currentTimeMillis();
            // downloadDirectory(sftpChannel,dirToDownload,"C:/MyJavaProjects/java_native/download");
            downloadDirectoryAsTar(sftpChannel, dirToDownload, outFile);
            var stopTime = System.currentTimeMillis();
            System.out.println((stopTime - startTime) + "ms");

            sftpChannel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("JSchExample@init: " + e);
        }

    }

    public static final String PATHSEPARATOR = "/";

    public static void listDirectory(
            ChannelSftp channelSftp, String path, List<String> list) throws SftpException {
        java.util.Vector<LsEntry> files = channelSftp.ls(path);
        for (LsEntry entry : files) {
            if (!entry.getAttrs().isDir()) {
                list.add(path + "/" + entry.getFilename());
            } else {
                if (!entry.getFilename().equals(".") &&
                        !entry.getFilename().equals("..")) {
                    listDirectory(channelSftp, path + "/" + entry.getFilename(), list);
                }
            }
        }
    }

    public static void downloadDirectory(ChannelSftp sftpClient, String sourcePath, String destinationPath) {
        try {
            var fileAndFolderList = sftpClient.ls(sourcePath); // Let list of folder

            // Iterate through list of folder content
            for (LsEntry item : fileAndFolderList) {
                var dp = destinationPath + PATHSEPARATOR + item.getFilename();
                var sp = sourcePath + PATHSEPARATOR + item.getFilename();

                // Check if it is a file (not a directory).
                if (!item.getAttrs().isDir()) {
                    new File(dp);
                    // Download file from source (source filename, destination filename).
                    sftpClient.get(sp, dp);
                    // System.out.println(String.format("File: %s",sp));

                } else if (!(".".equals(item.getFilename()) || "..".equals(item.getFilename()))) {
                    // Empty folder copy.
                    new File(dp).mkdirs();
                    // Enter found folder on server to read
                    downloadDirectory(sftpClient, sp, dp);
                    // its contents and create locally.
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println(String.format("Error: %s | path: %s", e, sourcePath));
        }
    }

    public static void downloadDirectoryAsZip(ChannelSftp sftpClient, String sourcePath, String destinationPath) {
        try {
            var outputFile = destinationPath.endsWith(".zip") ? destinationPath : destinationPath + ".zip";
            FileOutputStream fos = new FileOutputStream(outputFile);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            downloadDirectoryAsZipRec(zipOut, sftpClient, sourcePath, destinationPath);
            zipOut.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(String.format("downloadDirectoryAsZip: %s | path: %s", e, sourcePath));
        }
    }

    public static void downloadDirectoryAsZipRec(ZipOutputStream zipOut, ChannelSftp sftpClient, String sourcePath,
            String destinationPath) {
        try {
            var fileAndFolderList = sftpClient.ls(sourcePath);

            for (var item : fileAndFolderList) {
                var dp = destinationPath + PATHSEPARATOR + item.getFilename();
                var sp = sourcePath + PATHSEPARATOR + item.getFilename();

                if (!item.getAttrs().isDir()) {

                    // using temp file
                    // File f = File.createTempFile("MyAppName-", ".tmp");
                    // f.deleteOnExit();
                    var f = new ByteArrayOutputStream();
                    sftpClient.get(sp, f);
                    // FileInputStream fis = new FileInputStream(f);
                    var fis = new ByteArrayInputStream(f.toByteArray());

                    java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(sp);
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024 * 128 * 2];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                    fis.close();
                    // outstr.close();
                    f.close();
                    zipOut.closeEntry();
                    // System.out.println(sp);

                } else if (!(".".equals(item.getFilename()) || "..".equals(item.getFilename()))) {
                    downloadDirectoryAsZipRec(zipOut, sftpClient, sp, dp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(String.format("downloadDirectoryAsZipRec: %s | path: %s ", e, sourcePath));
        }
    }

    public static void downloadDirectoryAsTarGzip(ChannelSftp sftpClient, String sourcePath, String destinationPath) {

        try {
            var outputFile = destinationPath.endsWith(".tar.gz") ? destinationPath : destinationPath + ".tar.gz";
            try (FileOutputStream fos = new FileOutputStream(outputFile);
                    GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
                    TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gos, "ISO-8859-1");) {
                // to fix
                // https://stackoverflow.com/questions/32528799/when-i-tar-a-file-its-throw-exception-as-is-too-long-100-bytes-tararchiveo
                tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                // files larger than 8GiB
                // https://commons.apache.org/proper/commons-compress/tar.html#Long_File_Names
                tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
                tarOut.setAddPaxHeadersForNonAsciiNames(true);
                downloadDirectoryAsTarGzipRec(tarOut, sftpClient, sourcePath, destinationPath);
            }

        } catch (Exception e) {
            System.out.println(String.format("downloadDirectoryAsTarGzip: %s | path: %s", e, sourcePath));
            e.printStackTrace();
        }
    }

    public static void downloadDirectoryAsTarGzipRec(TarArchiveOutputStream tarOut, ChannelSftp sftpClient,
            String sourcePath,
            String destinationPath) throws SftpException {

        var fileAndFolderList = sftpClient.ls(sourcePath);

        for (var item : fileAndFolderList) {
            var filename = "";
            try {
                filename = item.getFilename();
                var dp = destinationPath + PATHSEPARATOR + filename;
                var sp = sourcePath + PATHSEPARATOR + filename;
                var fileAttr = item.getAttrs();
                if (!fileAttr.isDir()) {

                    // using temp file
                    // File tempFile = File.createTempFile("MyAppName-", ".tmp");
                    // f.deleteOnExit();
                    // var tempFile = new ByteArrayOutputStream();
                    try (InputStream tempFile = sftpClient.get(sp);
                            BufferedInputStream bis = new BufferedInputStream(tempFile);) {
                        // FileInputStream fis = new FileInputStream(tempFile);
                        // var ar = tempFile.toByteArray();
                        // var fis = new ByteArrayInputStream(ar);
                        // InputStreamReader isr = new InputStreamReader(is);
                        var zipEntry = new TarArchiveEntry(sp);
                        zipEntry.setSize(fileAttr.getSize());
                        zipEntry.setGroupId(fileAttr.getGId());
                        zipEntry.setUserId(fileAttr.getUId());
                        // var now = ZonedDateTime.now();
                        // var formatter = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss zzz yyyy",
                        // java.util.Locale.ENGLISH);
                        // var modTime = formatter.parse(fileAttr.getMtimeString());
                        var mDate = new Date(((long) fileAttr.getMTime()) * 1000L);
                        // var s = (mDate.toString());//formatter.format(modTime));
                        // System.out.println(fileAttr.getMtimeString());
                        // System.out.println(item.getLongname() + " | " +s);
                        zipEntry.setModTime(mDate);
                        zipEntry.setMode(fileAttr.getPermissions());
                        zipEntry.setGroupName(Utils.getGroupFromLongname(item.getLongname()));
                        zipEntry.setUserName(Utils.getUserFromLongname(item.getLongname()));
                        // System.out.println(Utils.getGroupFromLongname(item.getLongname()));
                        // zipEntry.setGroupName(item.getLongname());
                        tarOut.putArchiveEntry(zipEntry);

                        /*
                         * int COPY_BUF_SIZE = 8024;
                         * byte[] bytes = new byte[1024 * 128 * 2];
                         * int length;
                         * while ((length = tempFile.read(bytes)) >= 0) {
                         * tarOut.write(bytes, 0, length);
                         * }
                         */

                        // Write file content to archive
                        IOUtils.copy(bis, tarOut);

                        tarOut.closeArchiveEntry();
                    }

                } else if (!(".".equals(item.getFilename()) || "..".equals(item.getFilename()))) {
                    downloadDirectoryAsTarGzipRec(tarOut, sftpClient, sp, dp);
                }
            } catch (Exception e) {
                System.out.println(String.format("downloadDirectoryAsTarGzipRec: %s | path: %s ", e, sourcePath));
                e.printStackTrace();
            }
        }

    }

    public static void downloadDirectoryAsTar(ChannelSftp sftpClient, String sourcePath, String destinationPath) {

        try {
            var outputFile = destinationPath.endsWith(".tar") ? destinationPath : destinationPath + ".tar";
            try (FileOutputStream fos = new FileOutputStream(outputFile);
                    TarArchiveOutputStream tarOut = new TarArchiveOutputStream(fos, "ISO-8859-1");) {
                // to fix
                // https://stackoverflow.com/questions/32528799/when-i-tar-a-file-its-throw-exception-as-is-too-long-100-bytes-tararchiveo
                tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                // files larger than 8GiB
                // https://commons.apache.org/proper/commons-compress/tar.html#Long_File_Names
                tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
                tarOut.setAddPaxHeadersForNonAsciiNames(true);
                downloadDirectoryAsTarRec(tarOut, sftpClient, sourcePath, destinationPath);
            }

        } catch (Exception e) {
            System.out.println(String.format("downloadDirectoryAsTar: %s | path: %s", e, sourcePath));
            e.printStackTrace();
        }
    }

    public static void downloadDirectoryAsTarRec(TarArchiveOutputStream tarOut, ChannelSftp sftpClient,
            String sourcePath,
            String destinationPath) throws SftpException {

        var fileAndFolderList = sftpClient.ls(sourcePath);
        for (var item : fileAndFolderList) {
            var filename = "";
            try {
                filename = item.getFilename();
                var dp = destinationPath + PATHSEPARATOR + filename;
                var sp = sourcePath + PATHSEPARATOR + filename;
                var fileAttr = item.getAttrs();
                if (!fileAttr.isDir()) {
                    // using temp file
                    // File tempFile = File.createTempFile("MyAppName-", ".tmp");
                    // f.deleteOnExit();
                    // var tempFile = new ByteArrayOutputStream();
                    try (InputStream tempFile = sftpClient.get(sp);
                            BufferedInputStream bis = new BufferedInputStream(tempFile);) {
                        // FileInputStream fis = new FileInputStream(tempFile);
                        // var ar = tempFile.toByteArray();
                        // var fis = new ByteArrayInputStream(ar);
                        // InputStreamReader isr = new InputStreamReader(is);
                        var zipEntry = new TarArchiveEntry(sp);
                        zipEntry.setSize(fileAttr.getSize());
                        zipEntry.setGroupId(fileAttr.getGId());
                        zipEntry.setUserId(fileAttr.getUId());
                        // var now = ZonedDateTime.now();
                        // var formatter = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss zzz yyyy",
                        // java.util.Locale.ENGLISH);
                        // var modTime = formatter.parse(fileAttr.getMtimeString());
                        var mDate = new Date(((long) fileAttr.getMTime()) * 1000L);
                        // var s = (mDate.toString());//formatter.format(modTime));
                        // System.out.println(fileAttr.getMtimeString());
                        // System.out.println(item.getLongname() + " | " +s);
                        zipEntry.setModTime(mDate);
                        zipEntry.setMode(fileAttr.getPermissions());
                        zipEntry.setGroupName(Utils.getGroupFromLongname(item.getLongname()));
                        zipEntry.setUserName(Utils.getUserFromLongname(item.getLongname()));
                        // System.out.println(Utils.getGroupFromLongname(item.getLongname()));
                        // zipEntry.setGroupName(item.getLongname());
                        tarOut.putArchiveEntry(zipEntry);
                        /*
                         * int COPY_BUF_SIZE = 8024;
                         * byte[] bytes = new byte[1024 * 128 * 2];
                         * int length;
                         * while ((length = tempFile.read(bytes)) >= 0) {
                         * tarOut.write(bytes, 0, length);
                         * }
                         */
                        // Write file content to archive
                        IOUtils.copy(bis, tarOut);
                        tarOut.closeArchiveEntry();
                    }

                } else if (!(".".equals(item.getFilename()) || "..".equals(item.getFilename()))) {
                    var zipEntry = new TarArchiveEntry(sp + "/");
                    // zipEntry.setModTime(Date.from(java.time.ZonedDateTime.now().toInstant()));
                    var mDate = new Date(((long) fileAttr.getMTime()) * 1000L);
                    zipEntry.setModTime(mDate);
                    zipEntry.setMode(fileAttr.getPermissions());
                    zipEntry.setGroupName(Utils.getGroupFromLongname(item.getLongname()));
                    zipEntry.setUserName(Utils.getUserFromLongname(item.getLongname()));
                    tarOut.putArchiveEntry(zipEntry);
                    downloadDirectoryAsTarRec(tarOut, sftpClient, sp, dp);
                }
            } catch (Exception e) {
                System.out.println(String.format("downloadDirectoryAsTarRec: %s | path: %s ", e, sourcePath));
                e.printStackTrace();
            }
        }

    }

}
