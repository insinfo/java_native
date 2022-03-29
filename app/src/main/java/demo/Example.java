package demo;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;


public class Example {

    public static void main(String[] args) {
        try {
            var document = PDDocument.load(new File("1417.pdf"));
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);
            PDFTextStripper tStripper = new PDFTextStripper();
            tStripper.setStartPage(1);
            tStripper.setEndPage(1);
            String pdfFileInText = tStripper.getText(document);
            //String reversed = Utils.reverseString(str);
            System.out.println("Saida: " + pdfFileInText);

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }


}
