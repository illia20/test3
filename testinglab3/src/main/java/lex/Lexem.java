package lex;

import java.util.Arrays;
import java.util.List;

public class Lexem {
    public final static List<String> RESERVED = Arrays.asList("auto", "break", "case", "char", "const", "continue", "default",	"do", "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short", "signed", "sizeof", "string", "static", "struct", "switch", "typedef", "union",	"unsigned", "using", "void", "volatile", "while");
    public final static List<String> PREDIR = Arrays.asList("define", "undef", "include", "if", "ifdef", "ifndef", "else", "elif", "endif", "line", "error", "warning", "pragma");
    public final static List<String> OPER = Arrays.asList("+", "-", "*", "/", "%", ">", "<", "++", "--", ">=", "<=", "%=", "*=", "/=", "+=", "-=","==", "!=", "<<", ">>", "=");

    private String value;
    private Type type;

    public Lexem(String value, Type type) {
        this.value = value;
        this.type = type;
    }
    public String getValue() {
        return value;
    }

    public String getType(){
        return type.toString();
    }

    public enum Type{
        NUMBER, CONST, COMMENT, RESERVED, OPERATOR, DELIMITER, IDENTIFIER, DIRECTIVE, ERROR
    }
}
