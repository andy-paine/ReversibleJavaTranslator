package com.company;

import com.company.Visitors.*;
import org.eclipse.jface.text.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        String root = System.getProperty("user.dir");

        String filename = args[0];
        String fullPath = root + "/" + filename;
        List methods = new ArrayList();

        for (int i=1; i<args.length; i++)
            methods.add(args[i]);


        String str = new Scanner(new File(fullPath + ".java")).useDelimiter
                ("\\A").next();


        Document doc = new Document(str);

        doc = TranslationUtil.visit(new NormalisedFirstPass(), doc, methods);
        doc = TranslationUtil.visit(new NormalisedSecondPass(), doc, methods);
        doc = TranslationUtil.visit(new NormalisedThirdPass(), doc, methods);
        doc = TranslationUtil.visit(new NormalisedFourthPass(), doc, methods);

        TranslationUtil.writeToFile(fullPath + "_Normalised.java", doc);

        doc = TranslationUtil.visit(new TransformedFirstPass(), doc, methods);
        doc = TranslationUtil.visit(new TransformedSecondPass(), doc, methods);
        doc = TranslationUtil.visit(new TransformedThirdPass(), doc, methods);

        TranslationUtil.writeToFile(fullPath + "_Transformed.java", doc);

        doc = TranslationUtil.visit(new ReversedFirstPass(), doc, methods);
        doc = TranslationUtil.visit(new ReversedSecondPass(), doc, methods);
        doc = TranslationUtil.visit(new ReversedThirdPass(), doc, methods);

        TranslationUtil.writeToFile(fullPath + "_Reversed.java", doc);

        doc = TranslationUtil.cleanupLabels(fullPath + "_Transformed.java", methods);
        TranslationUtil.writeToFile(fullPath + "_Transformed.java", doc);

        doc = TranslationUtil.cleanupLabels(fullPath + "_Reversed.java", methods);
        TranslationUtil.writeToFile(fullPath + "_Reversed.java", doc);

    }
}
