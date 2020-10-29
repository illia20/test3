package lex.workfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface IFileWorker {
    public File openFile(String path) throws FileNotFoundException;
    public File createFile(String path) throws IOException;
    public void readFile(File file, Function function) throws FileNotFoundException, IOException;

}
