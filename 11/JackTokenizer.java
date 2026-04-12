import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

public class JackTokenizer {
    public enum TokenType { KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST }
    public enum Keyword { CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE, RETURN, TRUE, FALSE, NULL, THIS }
    
    private String token;
    private TokenType tokenType;
    private List<String> tokens = new ArrayList<>();
    private List<TokenType> types = new ArrayList<>();
    private int currentIdx = -1;
    
    private static final String KEYWORD_REGEX = "\\b(class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return)\\b";
    private static final String SYMBOL_REGEX = "[\\{\\}\\(\\)\\[\\]\\.\\,\\;\\+\\-\\*\\/\\&\\|\\<\\>\\=\\~]";
    private static final String INT_REGEX = "\\d+";
    private static final String STR_REGEX = "\"[^\"]*\"";
    private static final String ID_REGEX = "[a-zA-Z_]\\w*";
    
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
        "(?<keyword>" + KEYWORD_REGEX + ")|" +
        "(?<symbol>" + SYMBOL_REGEX + ")|" +
        "(?<int>" + INT_REGEX + ")|" +
        "(?<str>" + STR_REGEX + ")|" +
        "(?<id>" + ID_REGEX + ")"
    );
    
    private static final Map<String, Keyword> keywordMap = new HashMap<>();
    static {
        for (Keyword k : Keyword.values()) {
            keywordMap.put(k.name().toLowerCase(), k);
        }
    }
    
    public JackTokenizer(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            // Remove block comments and line comments
            content = content.replaceAll("(?s)/\\*.*?\\*/", "");
            content = content.replaceAll("//.*", "");
            
            Matcher m = TOKEN_PATTERN.matcher(content);
            while (m.find()) {
                tokens.add(m.group());
                if (m.group("keyword") != null) types.add(TokenType.KEYWORD);
                else if (m.group("symbol") != null) types.add(TokenType.SYMBOL);
                else if (m.group("int") != null) types.add(TokenType.INT_CONST);
                else if (m.group("str") != null) types.add(TokenType.STRING_CONST);
                else if (m.group("id") != null) types.add(TokenType.IDENTIFIER);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean hasMoreTokens() {
        return currentIdx + 1 < tokens.size();
    }
    
    public void advance() {
        currentIdx++;
        token = tokens.get(currentIdx);
        tokenType = types.get(currentIdx);
    }
    
    public TokenType tokenType() { return tokenType; }
    
    public Keyword keyword() { return keywordMap.get(token); }
    
    public char symbol() { return token.charAt(0); }
    
    public String identifier() { return token; }
    
    public int intVal() { return Integer.parseInt(token); }
    
    public String stringVal() { return token.substring(1, token.length() - 1); } // Drops the quotes
    
    public String getTokenString() { return token; }

    // Helper for LL(1) peek operations in CompilationEngine
    public String peekTokenString() {
        if (currentIdx + 1 < tokens.size()) {
            return tokens.get(currentIdx + 1);
        }
        return null;
    }
}