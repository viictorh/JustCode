package br.com.victor.justcode.pdf;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTree;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class ReadPDF {
    private static final String PDF_PATH = "C:\\Users\\victor.bello\\Downloads\\CONTRATO 3.pdf";

    public static void main(String[] args) throws IOException {
        try (PDDocument document = PDDocument.load(new File(PDF_PATH))) {
            if (!document.isEncrypted()) {
                readByRaw(document);
                readByFields(document);
                readNumbers(document);
                readByArea(document);
            }
        }
    }

    private static void readByArea(PDDocument document) throws IOException {
        System.out.println("============================");
        System.out.println("========== readByArea ========");
        System.out.println("==============================");

        Rectangle cpf = new Rectangle(235, 120, 375, 52);
        Rectangle qtdeParcelas = new Rectangle(200, 520, 190, 42);
        Rectangle valorParcela = new Rectangle(250, 560, 360, 62);

        List<Rectangle> rects = Arrays.asList(cpf, qtdeParcelas, valorParcela);
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        PDPage firstPage = document.getPage(0);
        for (Rectangle rect : rects) {
            stripper.addRegion("class1", rect);
            stripper.extractRegions(firstPage);
            System.out.println("Text in the area:" + rect);
            System.out.println(stripper.getTextForRegion("class1"));
        }

        firstPage = document.getPage(9);
        Rectangle rect = new Rectangle(0, 150, 420, 900);
        stripper.addRegion("class1", rect);
        stripper.extractRegions(firstPage);
        System.out.println("Text in the area PAGE 10:" + rect);
        System.out.println(stripper.getTextForRegion("class1"));

    }

    private static void readByRaw(PDDocument document) throws IOException {
        System.out.println("============================");
        System.out.println("========== readyRaw ========");
        System.out.println("============================");
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setStartPage(1);
        stripper.setEndPage(10);
        String text = stripper.getText(document);
        System.out.println("-------- " + text);
    }

    private static void readNumbers(PDDocument document) throws IOException {
        System.out.println("============================");
        System.out.println("========== readNumbers ========");
        System.out.println("============================");
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setStartPage(10);
        stripper.setEndPage(10);
        String text = stripper.getText(document);
        Pattern p = Pattern.compile("(?<!^[^\\d]*)\\d{20}");
        Matcher matcher = p.matcher(text);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

    private static void readByFields(PDDocument document) {
        System.out.println("============================");
        System.out.println("========== readByFields ========");
        System.out.println("============================");
        PDAcroForm acroForm = new PDAcroForm(document);
        PDFieldTree stripper = new PDFieldTree(acroForm);
        if (!stripper.iterator().hasNext()) {
            System.out.println("NONE ELEMENT FOUND");
            return;
        }

        for (Iterator<PDField> iterator = stripper.iterator(); iterator.hasNext();) {
            PDField next = iterator.next();
            System.out.println("Text:" + next);
        }
    }

}
