package lex.workfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileWorker implements IFileWorker{
    public File openFile(String path){
        File file = new File(path);
        if(!file.exists()){
            return null;
        }
        return file;
    }

    public File createFile(String path) throws IOException {
        File file = new File(path);
        if(!file.exists()){
            System.out.println("File " + path + " not found!");
            file.createNewFile();
        }

        return file;
    }

    public void readFile(File file, Function f) throws IOException {
            FileInputStream stream = new FileInputStream(file);
            int c, prev = 0;
            while ((c = stream.read()) != -1){
                f.handle((char)c, (char)prev);
                prev = c;
            }
            f.handle(' ', (char) prev);
    }
}
