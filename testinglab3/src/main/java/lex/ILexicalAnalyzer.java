package lex;

import java.io.File;

public interface ILexicalAnalyzer {
    public void analyze(File file);
    public void save(File file);
    public void printToFile();
}
