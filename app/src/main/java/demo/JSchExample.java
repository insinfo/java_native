package demo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipOutputStream;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class JSchExample {
    public static void init(String host, String user, String pass, String dirToDownload,String outFile) {
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
            // sftpChannel.setFilenameEncoding(StandardCharsets.ISO_8859_1);//"ISO-8859-1");
            // var fileAndFolderList = sftpChannel.ls("/var/www/html/teste/marcos/poo");

            var startTime = System.currentTimeMillis();
            // downloadDirectory(sftpChannel,dirToDownload,"C:/MyJavaProjects/java_native/download");
            downloadDirectoryAsZip(sftpChannel, dirToDownload, outFile);
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
            FileOutputStream fos = new FileOutputStream(destinationPath);
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
}
