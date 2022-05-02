package demo;

/*import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;

import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//import org.apache.log4j.BasicConfigurator;*/

public class SshjExample {
   /* public static void init(String host, String user, String pass, String dirToDownload,String outFile) {
        try {
            // ssh
            // enable log sshj
            // BasicConfigurator.configure();
            SSHClient sshClient = new SSHClient();
            // sshClient.setRemoteCharset(Charset.forName("ISO-8859-1"));
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(host, 22);
            sshClient.authPassword(user, pass);// new String(Base64.getDecoder().decode("MDhkZXNlY3QwNQ=="))
            SFTPClient sftpClient = sshClient.newSFTPClient();

            // sftpClient.get("/var/www/teste/texto.txt", new FileSystemFile("texto.txt"));
            // sftpClient.ls("/var/www/teste").forEach(i ->
            // System.out.println(i.getName()));

            var startTime = System.currentTimeMillis();
            // downloadDirectory(sftpClient, dirToDownload,
            // "C:/MyJavaProjects/java_native/download");
            downloadDirectoryAsZip(sftpClient, dirToDownload, outFile);
            var stopTime = System.currentTimeMillis();
            System.out.println((stopTime - startTime) + "ms");

            sftpClient.close();
            sshClient.close();

        } catch (Exception e) {
            System.out.println("SshjExample@init: " + e);
        }
    }

    public static final String PATHSEPARATOR = "/";

    public static void syncDirectory(SFTPClient sftpClient, String sourcePath, String destinationPath) {
        try {
            var fileAndFolderList = sftpClient.ls(sourcePath); // Let list of folder

            // Iterate through list of folder content
            for (var item : fileAndFolderList) {
                var dp = destinationPath + PATHSEPARATOR + item.getName();
                var sp = sourcePath + PATHSEPARATOR + item.getName();

                // Check if it is a file (not a directory).
                if (!item.isDirectory()) {
                    // Download only if changed later.
                    if (!(new File(dp)).exists()
                            || (item.getAttributes().getMtime() > Long
                                    .valueOf(new File(dp).lastModified()
                                            / (long) 1000)
                                    .intValue())) {

                        new File(dp);
                        // Download file from source (source filename, destination filename).
                        sftpClient.get(sp, dp);

                    }
                } else if (!(".".equals(item.getName()) || "..".equals(item.getName()))) {
                    // Empty folder copy.
                    new File(dp).mkdirs();
                    // Enter found folder on server to read
                    syncDirectory(sftpClient, sp, dp);
                    // its contents and create locally.
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("Error: " + e);
        }
    }

    public static void downloadDirectory(SFTPClient sftpClient, String sourcePath, String destinationPath) {
        try {
            var fileAndFolderList = sftpClient.ls(sourcePath); // Let list of folder

            // Iterate through list of folder content
            for (var item : fileAndFolderList) {
                var dp = destinationPath + PATHSEPARATOR + item.getName();
                var sp = sourcePath + PATHSEPARATOR + item.getName();

                // Check if it is a file (not a directory).
                if (!item.isDirectory()) {
                    new File(dp);
                    // Download file from source (source filename, destination filename).
                    sftpClient.get(sp, dp);

                } else if (!(".".equals(item.getName()) || "..".equals(item.getName()))) {
                    // Empty folder copy.
                    new File(dp).mkdirs();
                    // Enter found folder on server to read
                    downloadDirectory(sftpClient, sp, dp);
                    // its contents and create locally.
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println(String.format("downloadDirectory: %s | path: %s", e, sourcePath));
        }
    }

    public static void downloadDirectoryAsZip(SFTPClient sftpClient, String sourcePath, String destinationPath) {
        try {
            FileOutputStream fos = new FileOutputStream(destinationPath);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            downloadDirectoryAsZipRec(zipOut, sftpClient, sourcePath, destinationPath);
            zipOut.close();
            fos.close();

        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println(String.format("downloadDirectoryAsZip: %s | path: %s", e, sourcePath));
        }
    }

    public static void downloadDirectoryAsZipRec(ZipOutputStream zipOut, SFTPClient sftpClient, String sourcePath,
            String destinationPath) {
        try {
            var fileAndFolderList = sftpClient.ls(sourcePath);

            for (var item : fileAndFolderList) {
                var dp = destinationPath + PATHSEPARATOR + item.getName();
                var sp = sourcePath + PATHSEPARATOR + item.getName();

                if (!item.isDirectory()) {
                    // Download file from source (source filename, destination filename).
                    // sftpClient.get(sp, dp);
                    // using temp file
                    // File f = File.createTempFile("MyAppName-", ".tmp");
                    // f.deleteOnExit();

                    // sftpClient.get(sp, f.toPath().toString());
                    // FileInputStream fis = new FileInputStream(f);

                    RemoteFile f = sftpClient.getSFTPEngine().open(sp);
                    InputStream fis = f.new RemoteFileInputStream(0);

                    // var outstr = new ByteArrayOutputStream();
                    // StreamingInMemoryDestFile simdf = new StreamingInMemoryDestFile(outstr);
                    // sftpClient.get(sp, simdf);
                    // var fis = new ByteArrayInputStream(outstr.toByteArray());

                    ZipEntry zipEntry = new ZipEntry(sp);
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

                } else if (!(".".equals(item.getName()) || "..".equals(item.getName()))) {
                    downloadDirectoryAsZipRec(zipOut, sftpClient, sp, dp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(String.format("downloadDirectoryAsZipRec: %s | path: %s ", e, sourcePath));
        }
    }*/
}
