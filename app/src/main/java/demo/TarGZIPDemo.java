package demo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class TarGZIPDemo {
    public static void init() {
        String SOURCE_FOLDER = "C:/MyJavaProjects/java_native/download";
        TarGZIPDemo tGzipDemo = new TarGZIPDemo();
        tGzipDemo.createTarFile(SOURCE_FOLDER);

    }

    private void createTarFile(String sourceDir) {
        TarArchiveOutputStream tarOs = null;
        try {
            var source = new File(sourceDir);
            // Using input name to create output name
            FileOutputStream fos = new FileOutputStream(source.getAbsolutePath().concat(".tar.gz"));
            GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
            tarOs = new TarArchiveOutputStream(gos);
            addFilesToTarGZ(sourceDir, "", tarOs);
        } catch (Exception e) {            
            System.out.println("createTarFile Error: " + e);
            e.printStackTrace();
        } finally {
            try {
                tarOs.close();
            } catch (IOException e) {
                System.out.println("createTarFile Error: " + e);
                e.printStackTrace();
            }
        }
    }

    public void addFilesToTarGZ(String filePath, String parent, TarArchiveOutputStream tarArchive) {
        try {
            File file = new File(filePath);
            // Create entry name relative to parent file path
            String entryName = parent + file.getName();
            // add tar ArchiveEntry
          
           tarArchive.putArchiveEntry(new TarArchiveEntry(file, entryName));
           tarArchive.closeArchiveEntry();
           
             if (file.isFile()) {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                // Write file content to archive
                IOUtils.copy(bis, tarArchive);
                tarArchive.closeArchiveEntry();
                bis.close();
            } else if (file.isDirectory()) {
                // no need to copy any content since it is
                // a directory, just close the outputstream
                tarArchive.closeArchiveEntry();
                // for files in the directories
                for (File f : file.listFiles()) {
                    // recursively call the method for all the subdirectories
                    addFilesToTarGZ(f.getAbsolutePath(), entryName + File.separator, tarArchive);
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("addFilesToTarGZ Error: " + e);
        }
    }
}