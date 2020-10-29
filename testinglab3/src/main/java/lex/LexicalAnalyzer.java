package lex;

import lex.workfile.FileWorker;
import lex.workfile.IFileWorker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LexicalAnalyzer {

    private final static String delimiter = "{}[]();,";
    private final static String oper = "+-*/%=<>";
    private final static String splitter = " \n\r\t";

    private List<Lexem> lexems = new ArrayList<>();
    private State currentState = State.EMPTY;
    private StringBuilder value = new StringBuilder();

    private IFileWorker fileWorker;

    public void setFileWorker(IFileWorker fileworker){
        fileWorker = fileworker;
    }

    public File setAndOpenInputFile(String path) throws FileNotFoundException{
        File f = fileWorker.openFile(path);
        if(f == null){
            throw new FileNotFoundException();
        }
        return f;
    }

    public File setAndCreateOutputFile(String path) throws IOException {
        return fileWorker.createFile(path);
    }
    public void start(String in, String out) throws IOException{
        File input = setAndOpenInputFile(in);
        File output = setAndCreateOutputFile(out);
        analyze(input);
        save(output);
    }

    public void analyze(File file) throws FileNotFoundException, IOException {
        // FileWorker fw = new FileWorker();
        fileWorker.readFile(file, (symbol, prev) -> {
            if (checkDelimiters(symbol))
                return;
            automateMove(symbol, prev);
        });
    }

    public void save(File file) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(file));
            System.setOut(out);
            printToFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean checkDelimiters(char symbol) {
        if(currentState != State.STR_CONST && currentState != State.COMMENT_LINE && currentState != State.COMMENT) {
            if(delimiter.contains(String.valueOf(symbol))) {
                saveLeksem();
            }
            else if(splitter.contains(String.valueOf(symbol))) {
                saveLeksem();
                if(symbol == '\n') {
                    saveLeksem();
                }
                return true;
            }
        }
        return false;
    }

    private void automateMove(char symbol, char prev) {
        switch (currentState) {
            case WORD:
                if(oper.contains(String.valueOf(symbol))){
                    saveLeksem();
                    currentState = State.OPERATOR;
                }
                else if(!Character.isLetter(symbol) && !Character.isDigit(symbol)
                        && symbol != '_' && symbol != '$' && (symbol != '.' || prev == '.'))
                    currentState = State.ERROR;

                value.append(symbol);
                break;

            case OPERATOR:
                if(oper.contains(String.valueOf(symbol)))
                    value.append(symbol);
                else{
                    saveLeksem();
                    currentState = State.EMPTY;
                    value.append(symbol);
                }
                break;

            case DIRECTIVE:
                if(!Character.isLetter(symbol))
                    currentState = State.ERROR;
                value.append(symbol);
                break;

            case STR_CONST:
                value.append(symbol);
                if ((symbol == '\"' || symbol == '\'') && prev != '\\'){
                    saveLeksem();
                    return;
                }
                break;

            case COMMENT_LINE:
                if(symbol == '\n') {
                    saveLeksem();
                    break;
                }
                else {
                    value.append(symbol);
                    break;
                }

            case COMMENT:
                value.append(symbol);
                if(symbol == '/' && prev == '*') {
                    saveLeksem();
                }
                break;

            case NUMBER:
                if(!Character.isDigit(symbol)) {
                    if((symbol == 'x' || symbol == 'X')&& value.toString().equals("0")) {}
                    else if(Character.isLetter(symbol) && ('a' <= Character.toLowerCase(symbol) && Character.toLowerCase(symbol) <= 'f')
                            && (value.toString().startsWith("0x") || value.toString().startsWith("0X1"))) {}
                    else if("lfe".contains(String.valueOf(symbol).toLowerCase()) && Character.isDigit(prev)) {}
                    else if((String.valueOf(prev).equals("e") || String.valueOf(prev).equals("E")) &&
                            ((String.valueOf(symbol).equals("-")) || String.valueOf(symbol).equals("+") || Character.isDigit(symbol)) &&
                            ((!value.substring(0, value.length() - 1).toString().contains("e")) || !value.substring(0, value.length() - 1).toString().contains("E")) &&
                            (!value.substring(1, value.length()).toString().contains("+") || !value.substring(1, value.length()).toString().contains("-"))) {}
                    else if(oper.contains(String.valueOf(symbol))) {
                        saveLeksem();
                        currentState = State.OPERATOR;
                    }
                    else if((String.valueOf(symbol).equals("l") || String.valueOf(symbol).equals("L")) &&
                            (String.valueOf(prev).equals("L") || String.valueOf(prev).equals("l")) &&
                            !(value.toString().toLowerCase().contains("ll")) && !(value.substring(0, value.length()-1).toLowerCase().contains("L"))) {}
                    else if(symbol == '.' && !value.toString().contains("."));
                    else {
                        currentState = State.ERROR;
                    }
                }
                value.append(symbol);
                break;

            case DELIMITER:
                saveLeksem();
                currentState = State.EMPTY;

            case EMPTY:
                if(initEmptyState(symbol, prev)) break;
                value.append(symbol);
                currentState = State.ERROR;
                break;

            case ERROR:
                value.append(symbol);
                break;
        }
    }

    private boolean initEmptyState(char symbol, char prev) {
        if (Character.isLetter(symbol) || symbol == '_'){
            value.append(symbol);
            currentState = State.WORD;
            return true;
        }
        if (Character.isDigit(symbol)){
            value.append(symbol);
            currentState = State.NUMBER;
            return true;
        }
        if (symbol == '#'){
            value.append(symbol);
            currentState = State.DIRECTIVE;
            return true;
        }
        if (symbol == '\"' || symbol == '\''){
            value.append(symbol);
            currentState = State.STR_CONST;
            return true;
        }
        if (symbol == '/' && prev == '/'){
            lexems.remove(lexems.size() - 1);
            saveLeksem();
            value.append("//");
            currentState = State.COMMENT_LINE;
            return true;
        }
        if (symbol == '*' && prev == '/'){
            lexems.remove(lexems.size() - 1);
            saveLeksem();
            value.append("/*");
            currentState = State.COMMENT;
            return true;
        }
        if (symbol == '/'){
            return true;
        }
        if (delimiter.contains(String.valueOf(symbol))){
            value.append(symbol);
            currentState = State.DELIMITER;
            return true;
        }
        if (oper.contains(String.valueOf(symbol))){
            value.append(symbol);
            currentState = State.OPERATOR;
            return true;
        }
        return false;
    }

    private void saveLeksem() {
        String v = value.toString();
        if(v.isEmpty()) return;

        Lexem.Type type = null;
        if(currentState == State.NUMBER) type = Lexem.Type.NUMBER;
        else if(currentState == State.WORD) {
            if(Lexem.RESERVED.contains(v))
                type = Lexem.Type.RESERVED;
            else
                type = Lexem.Type.IDENTIFIER;
        }
        else if(currentState == State.STR_CONST) {
            type = Lexem.Type.CONST;
        }
        else if(currentState == State.COMMENT_LINE || currentState == State.COMMENT) {
            type = Lexem.Type.COMMENT;
        }
        else if(currentState == State.OPERATOR){
            if(Lexem.OPER.contains(v)){
                type = Lexem.Type.OPERATOR;
            }
            else {
                type = Lexem.Type.ERROR;
            }
        }
        else if(currentState == State.DIRECTIVE){
            if(Lexem.PREDIR.contains(v.substring(1)))
                type = Lexem.Type.DIRECTIVE;
            else
                type = Lexem.Type.ERROR;
        }
        else {
            type = Lexem.Type.valueOf(currentState.toString());
        }

        Lexem lexem = new Lexem(v, type);
        lexems.add(lexem);
        value.setLength(0);
        currentState = State.EMPTY;
    }

    public void printToFile() {
        for (int i = 0; i < lexems.size(); i++) {
            Lexem lexem = lexems.get(i);
            if(lexem.getType().equals("COMMENT") || lexem.getType().equals("COMMENT_LINE"))
                continue;
            System.out.println("<" + lexem.getValue() + "> - <" + lexem.getType() + ">");
        }
    }
    private enum State {
        EMPTY, WORD, DIRECTIVE, STR_CONST, COMMENT_LINE, COMMENT, NUMBER, DELIMITER, OPERATOR, ERROR
    }
}