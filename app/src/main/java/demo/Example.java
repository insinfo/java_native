package demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.time.Instant;

/*import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.Loader;
*/
public class Example {

    public static void main(String[] args) {

        // try {
        // -z --gzip -f --file= -v, --verbose -p, --preserve-permissions
        // tar -czvf file.tar.gz /var/www/dart/intranetbrowser
        // tar -c --gzip --verbose --preserve-permissions --file=file.tar.gz
        // /var/www/dart/intranetbrowser
        // scp root@192.168.133.13:/var/www/dart/file.tar.gz ./file.tar.gz
        var user = "root";
        var pass = "pass";
       // JSchExample.init("192.168.133.13", user, pass, "/var/www/dart/intranetbrowser", "C:/MyJavaProjects/java_native/download.zip");
        SshjExample.init("192.168.133.13", user, pass, "/var/www/teste", "C:/MyJavaProjects/java_native/download2.zip");
        // download file
        /*
         * var fileName = "./1439.pdf";
         * var url = new URL(
         * "https://appro.riodasostras.rj.gov.br/storage/riodasostrasapp/jornais/2022/4/36d6ac99-a827-449f-8126-8645cb5c114e.pdf"
         * );
         * var readableByteChannel = Channels.newChannel(url.openStream());
         * var fileOutputStream = new FileOutputStream(fileName);
         * fileOutputStream.getChannel()
         * .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
         * fileOutputStream.close();
         * readableByteChannel.close();
         */

        /*
         * var document = Loader.loadPDF(new File(fileName));
         * // PDFTextStripperByArea stripper = new PDFTextStripperByArea();
         * // stripper.setSortByPosition(true);
         * PDFTextStripper tStripper = new PDFTextStripper();
         * tStripper.setStartPage(1);
         * tStripper.setEndPage(1);
         * String pdfFileInText = tStripper.getText(document);
         * // System.setOut(new PrintStream(System.out, true, "UTF-8"));
         * System.out.println("Saida: " + pdfFileInText);
         * document.close();
         */

        // } catch (Exception e) {
        // System.out.println("Error: " + e);
        // }
    }

}