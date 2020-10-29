package lex;

import lex.workfile.FileWorker;

import java.io.File;
import java.io.IOException;

public class Application {

    private final static String RES_PATH = "src/main/resources/";

    public static void main(String[] args) throws IOException {
        /*
        FileWorker fw = new FileWorker();
        File inputFile = fw.openFile(RES_PATH + "main.cpp");
        File outputFile = fw.createFile(RES_PATH + "index.txt");
        */
        FileWorker fw = new FileWorker();
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        lexicalAnalyzer.setFileWorker(fw);
        lexicalAnalyzer.start(RES_PATH + "main.cpp", RES_PATH + "index.txt");
    }

}
