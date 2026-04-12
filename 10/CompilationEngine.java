import java.io.*;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private PrintWriter writer;
    
    public CompilationEngine(File inFile, File outFile) {
        tokenizer = new JackTokenizer(inFile);
        try {
            writer = new PrintWriter(new FileWriter(outFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }
    
    private void write(String tag, String val) {
        writer.println("<" + tag + "> " + escape(val) + " </" + tag + ">");
    }
    
    private String escape(String val) {
        return val.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;");
    }
    
    private void consume() {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            write("keyword", tokenizer.getTokenString());
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL) {
            write("symbol", tokenizer.getTokenString());
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            write("identifier", tokenizer.identifier());
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            write("integerConstant", String.valueOf(tokenizer.intVal()));
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            write("stringConstant", tokenizer.stringVal());
        }
        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }
    
    public void compileClass() {
        writer.println("<class>");
        consume(); 
        consume(); 
        consume(); 
        
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && 
              (tokenizer.keyword() == JackTokenizer.Keyword.STATIC || tokenizer.keyword() == JackTokenizer.Keyword.FIELD)) {
            compileClassVarDec();
        }
        
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && 
              (tokenizer.keyword() == JackTokenizer.Keyword.CONSTRUCTOR || 
               tokenizer.keyword() == JackTokenizer.Keyword.FUNCTION || 
               tokenizer.keyword() == JackTokenizer.Keyword.METHOD)) {
            compileSubroutine();
        }
        
        consume(); 
        writer.println("</class>");
        writer.close();
    }
    
    public void compileClassVarDec() {
        writer.println("<classVarDec>");
        consume(); 
        consume(); 
        consume(); 
        
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            consume(); 
            consume(); 
        }
        consume(); 
        writer.println("</classVarDec>");
    }
    
    public void compileSubroutine() {
        writer.println("<subroutineDec>");
        consume(); 
        consume(); 
        consume(); 
        consume(); 
        compileParameterList();
        consume(); 
        
        writer.println("<subroutineBody>");
        consume(); 
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword() == JackTokenizer.Keyword.VAR) {
            compileVarDec();
        }
        compileStatements();
        consume(); 
        writer.println("</subroutineBody>");
        
        writer.println("</subroutineDec>");
    }
    
    public void compileParameterList() {
        writer.println("<parameterList>");
        if (!(tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')')) {
            consume(); 
            consume(); 
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                consume(); 
                consume(); 
                consume(); 
            }
        }
        writer.println("</parameterList>");
    }
    
    public void compileVarDec() {
        writer.println("<varDec>");
        consume(); 
        consume(); 
        consume(); 
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            consume(); 
            consume(); 
        }
        consume(); 
        writer.println("</varDec>");
    }
    
    public void compileStatements() {
        writer.println("<statements>");
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            JackTokenizer.Keyword kw = tokenizer.keyword();
            if (kw == JackTokenizer.Keyword.LET) compileLet();
            else if (kw == JackTokenizer.Keyword.IF) compileIf();
            else if (kw == JackTokenizer.Keyword.WHILE) compileWhile();
            else if (kw == JackTokenizer.Keyword.DO) compileDo();
            else if (kw == JackTokenizer.Keyword.RETURN) compileReturn();
            else break;
        }
        writer.println("</statements>");
    }
    
    public void compileDo() {
        writer.println("<doStatement>");
        consume(); 
        consume(); 
        
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '.') {
            consume(); 
            consume(); 
        }
        consume(); 
        compileExpressionList();
        consume(); 
        consume(); 
        writer.println("</doStatement>");
    }
    
    public void compileLet() {
        writer.println("<letStatement>");
        consume(); 
        consume(); 
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
            consume(); 
            compileExpression();
            consume(); 
        }
        consume(); 
        compileExpression();
        consume(); 
        writer.println("</letStatement>");
    }
    
    public void compileWhile() {
        writer.println("<whileStatement>");
        consume(); 
        consume(); 
        compileExpression();
        consume(); 
        consume(); 
        compileStatements();
        consume(); 
        writer.println("</whileStatement>");
    }
    
    public void compileReturn() {
        writer.println("<returnStatement>");
        consume(); 
        if (!(tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';')) {
            compileExpression();
        }
        consume(); 
        writer.println("</returnStatement>");
    }
    
    public void compileIf() {
        writer.println("<ifStatement>");
        consume(); 
        consume(); 
        compileExpression();
        consume(); 
        consume(); 
        compileStatements();
        consume(); 
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword() == JackTokenizer.Keyword.ELSE) {
            consume(); 
            consume(); 
            compileStatements();
            consume(); 
        }
        writer.println("</ifStatement>");
    }
    
    public void compileExpression() {
        writer.println("<expression>");
        compileTerm();
        while (isOp()) {
            consume(); 
            compileTerm();
        }
        writer.println("</expression>");
    }
    
    private boolean isOp() {
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL) return false;
        char c = tokenizer.symbol();
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '&' || c == '|' || c == '<' || c == '>' || c == '=';
    }
    
    public void compileTerm() {
        writer.println("<term>");
        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST || 
            tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST ||
            tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            consume();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            String nextToken = tokenizer.peekTokenString();
            if ("[".equals(nextToken)) {
                consume(); 
                consume(); 
                compileExpression();
                consume(); 
            } else if ("(".equals(nextToken) || ".".equals(nextToken)) {
                consume(); 
                if (".".equals(nextToken)) {
                    consume(); 
                    consume(); 
                }
                consume(); 
                compileExpressionList();
                consume(); 
            } else {
                consume(); 
            }
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL) {
            if (tokenizer.symbol() == '(') {
                consume(); 
                compileExpression();
                consume(); 
            } else if (tokenizer.symbol() == '-' || tokenizer.symbol() == '~') {
                consume(); 
                compileTerm();
            }
        }
        writer.println("</term>");
    }

    public void compileExpressionList() {
        writer.println("<expressionList>");
        if (!(tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')')) {
            compileExpression();
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                consume(); 
                compileExpression();
            }
        }
        writer.println("</expressionList>");
    }
}