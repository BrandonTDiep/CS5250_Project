import java.io.File;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private VMWriter vmWriter;
    private SymbolTable symbolTable;
    
    private String className;
    private int labelIndex = 0;

    public CompilationEngine(File inFile, File outFile) {
        tokenizer = new JackTokenizer(inFile);
        vmWriter = new VMWriter(outFile);
        symbolTable = new SymbolTable();
        if (tokenizer.hasMoreTokens()) tokenizer.advance();
    }

    private void consume() {
        if (tokenizer.hasMoreTokens()) tokenizer.advance();
    }

    public void compileClass() {
        consume(); 
        className = tokenizer.identifier();
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
        vmWriter.close();
    }

    public void compileClassVarDec() {
        SymbolTable.Kind kind = (tokenizer.keyword() == JackTokenizer.Keyword.STATIC) ? SymbolTable.Kind.STATIC : SymbolTable.Kind.FIELD;
        consume(); 
        
        String type = tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD ? tokenizer.keyword().name().toLowerCase() : tokenizer.identifier();
        consume(); 
        
        String name = tokenizer.identifier();
        symbolTable.define(name, type, kind);
        consume(); 
        
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            consume(); 
            name = tokenizer.identifier();
            symbolTable.define(name, type, kind);
            consume(); 
        }
        consume(); 
    }

    public void compileSubroutine() {
        symbolTable.startSubroutine();
        JackTokenizer.Keyword subroutineType = tokenizer.keyword();
        consume(); 
        consume(); 
        
        String subroutineName = tokenizer.identifier();
        consume(); 
        
        if (subroutineType == JackTokenizer.Keyword.METHOD) {
            symbolTable.define("this", className, SymbolTable.Kind.ARG);
        }
        
        consume(); 
        compileParameterList();
        consume(); 
        
        consume(); 
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword() == JackTokenizer.Keyword.VAR) {
            compileVarDec();
        }
        
        vmWriter.writeFunction(className + "." + subroutineName, symbolTable.varCount(SymbolTable.Kind.VAR));
        
        if (subroutineType == JackTokenizer.Keyword.METHOD) {
            vmWriter.writePush(VMWriter.Segment.ARG, 0);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);
        } else if (subroutineType == JackTokenizer.Keyword.CONSTRUCTOR) {
            vmWriter.writePush(VMWriter.Segment.CONST, symbolTable.varCount(SymbolTable.Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);
        }
        
        compileStatements();
        consume(); 
    }

    public void compileParameterList() {
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ')') {
            String type = tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD ? tokenizer.keyword().name().toLowerCase() : tokenizer.identifier();
            consume();
            String name = tokenizer.identifier();
            symbolTable.define(name, type, SymbolTable.Kind.ARG);
            consume();
            
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                consume(); 
                type = tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD ? tokenizer.keyword().name().toLowerCase() : tokenizer.identifier();
                consume();
                name = tokenizer.identifier();
                symbolTable.define(name, type, SymbolTable.Kind.ARG);
                consume();
            }
        }
    }

    public void compileVarDec() {
        consume(); 
        String type = tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD ? tokenizer.keyword().name().toLowerCase() : tokenizer.identifier();
        consume(); 
        
        String name = tokenizer.identifier();
        symbolTable.define(name, type, SymbolTable.Kind.VAR);
        consume(); 
        
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            consume(); 
            name = tokenizer.identifier();
            symbolTable.define(name, type, SymbolTable.Kind.VAR);
            consume();
        }
        consume(); 
    }

    public void compileStatements() {
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            JackTokenizer.Keyword kw = tokenizer.keyword();
            if (kw == JackTokenizer.Keyword.LET) compileLet();
            else if (kw == JackTokenizer.Keyword.IF) compileIf();
            else if (kw == JackTokenizer.Keyword.WHILE) compileWhile();
            else if (kw == JackTokenizer.Keyword.DO) compileDo();
            else if (kw == JackTokenizer.Keyword.RETURN) compileReturn();
            else break;
        }
    }

    public void compileLet() {
        consume(); 
        String varName = tokenizer.identifier();
        consume(); 
        
        boolean isArray = false;
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
            isArray = true;
            pushVariable(varName);
            consume(); 
            compileExpression();
            consume(); 
            vmWriter.writeArithmetic(VMWriter.Command.ADD);
        }
        
        consume(); 
        compileExpression();
        consume(); 
        
        if (isArray) {
            vmWriter.writePop(VMWriter.Segment.TEMP, 0);
            vmWriter.writePop(VMWriter.Segment.POINTER, 1);
            vmWriter.writePush(VMWriter.Segment.TEMP, 0);
            vmWriter.writePop(VMWriter.Segment.THAT, 0);
        } else {
            popVariable(varName);
        }
    }

    public void compileDo() {
        consume(); 
        compileCall();
        consume(); 
        vmWriter.writePop(VMWriter.Segment.TEMP, 0); 
    }

    private void compileCall() {
        String name = tokenizer.identifier();
        consume();
        
        int nArgs = 0;
        String funcName = name;
        
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '.') {
            consume(); 
            String subName = tokenizer.identifier();
            consume();
            
            String type = symbolTable.typeOf(name);
            if (type != null) { 
                pushVariable(name);
                funcName = type + "." + subName;
                nArgs = 1;
            } else { 
                funcName = name + "." + subName;
            }
        } else { 
            vmWriter.writePush(VMWriter.Segment.POINTER, 0);
            funcName = className + "." + name;
            nArgs = 1;
        }
        
        consume(); 
        nArgs += compileExpressionList();
        consume(); 
        
        vmWriter.writeCall(funcName, nArgs);
    }

    public void compileWhile() {
        String l1 = "WHILE_EXP" + (labelIndex);
        String l2 = "WHILE_END" + (labelIndex++);
        
        consume(); 
        vmWriter.writeLabel(l1);
        consume(); 
        compileExpression();
        consume(); 
        
        vmWriter.writeArithmetic(VMWriter.Command.NOT);
        vmWriter.writeIf(l2);
        
        consume(); 
        compileStatements();
        consume(); 
        
        vmWriter.writeGoto(l1);
        vmWriter.writeLabel(l2);
    }

    public void compileReturn() {
        consume(); 
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            vmWriter.writePush(VMWriter.Segment.CONST, 0);
        } else {
            compileExpression();
        }
        consume(); 
        vmWriter.writeReturn();
    }

    public void compileIf() {
        String l1 = "IF_TRUE" + (labelIndex);
        String l2 = "IF_FALSE" + (labelIndex);
        String l3 = "IF_END" + (labelIndex++);
        
        consume(); 
        consume(); 
        compileExpression();
        consume(); 
        
        vmWriter.writeIf(l1);
        vmWriter.writeGoto(l2);
        vmWriter.writeLabel(l1);
        
        consume(); 
        compileStatements();
        consume(); 
        
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword() == JackTokenizer.Keyword.ELSE) {
            vmWriter.writeGoto(l3);
            vmWriter.writeLabel(l2);
            consume(); 
            consume(); 
            compileStatements();
            consume(); 
            vmWriter.writeLabel(l3);
        } else {
            vmWriter.writeLabel(l2);
        }
    }

    public void compileExpression() {
        compileTerm();
        while (isOp()) {
            char op = tokenizer.symbol();
            consume();
            compileTerm();
            writeOp(op);
        }
    }

    public void compileTerm() {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            vmWriter.writePush(VMWriter.Segment.CONST, tokenizer.intVal());
            consume();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            String str = tokenizer.stringVal();
            vmWriter.writePush(VMWriter.Segment.CONST, str.length());
            vmWriter.writeCall("String.new", 1);
            for (int i = 0; i < str.length(); i++) {
                vmWriter.writePush(VMWriter.Segment.CONST, (int) str.charAt(i));
                vmWriter.writeCall("String.appendChar", 2);
            }
            consume();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            JackTokenizer.Keyword kw = tokenizer.keyword();
            if (kw == JackTokenizer.Keyword.TRUE) {
                vmWriter.writePush(VMWriter.Segment.CONST, 0);
                vmWriter.writeArithmetic(VMWriter.Command.NOT);
            } else if (kw == JackTokenizer.Keyword.FALSE || kw == JackTokenizer.Keyword.NULL) {
                vmWriter.writePush(VMWriter.Segment.CONST, 0);
            } else if (kw == JackTokenizer.Keyword.THIS) {
                vmWriter.writePush(VMWriter.Segment.POINTER, 0);
            }
            consume();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            String next = tokenizer.peekTokenString();
            if ("[".equals(next)) { 
                String varName = tokenizer.identifier();
                consume();
                pushVariable(varName);
                consume(); 
                compileExpression();
                consume(); 
                vmWriter.writeArithmetic(VMWriter.Command.ADD);
                vmWriter.writePop(VMWriter.Segment.POINTER, 1);
                vmWriter.writePush(VMWriter.Segment.THAT, 0);
            } else if ("(".equals(next) || ".".equals(next)) { 
                compileCall();
            } else { 
                pushVariable(tokenizer.identifier());
                consume();
            }
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL) {
            if (tokenizer.symbol() == '(') {
                consume();
                compileExpression();
                consume();
            } else if (tokenizer.symbol() == '-' || tokenizer.symbol() == '~') {
                char op = tokenizer.symbol();
                consume();
                compileTerm();
                if (op == '-') vmWriter.writeArithmetic(VMWriter.Command.NEG);
                else vmWriter.writeArithmetic(VMWriter.Command.NOT);
            }
        }
    }

    public int compileExpressionList() {
        int count = 0;
        if (!(tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')')) {
            compileExpression();
            count++;
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                consume();
                compileExpression();
                count++;
            }
        }
        return count;
    }

    private boolean isOp() {
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL) return false;
        char c = tokenizer.symbol();
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '&' || c == '|' || c == '<' || c == '>' || c == '=';
    }

    private void writeOp(char op) {
        if (op == '+') vmWriter.writeArithmetic(VMWriter.Command.ADD);
        else if (op == '-') vmWriter.writeArithmetic(VMWriter.Command.SUB);
        else if (op == '*') vmWriter.writeCall("Math.multiply", 2);
        else if (op == '/') vmWriter.writeCall("Math.divide", 2);
        else if (op == '&') vmWriter.writeArithmetic(VMWriter.Command.AND);
        else if (op == '|') vmWriter.writeArithmetic(VMWriter.Command.OR);
        else if (op == '<') vmWriter.writeArithmetic(VMWriter.Command.LT);
        else if (op == '>') vmWriter.writeArithmetic(VMWriter.Command.GT);
        else if (op == '=') vmWriter.writeArithmetic(VMWriter.Command.EQ);
    }

    private void pushVariable(String name) {
        SymbolTable.Kind kind = symbolTable.kindOf(name);
        int index = symbolTable.indexOf(name);
        if (kind == SymbolTable.Kind.STATIC) vmWriter.writePush(VMWriter.Segment.STATIC, index);
        else if (kind == SymbolTable.Kind.FIELD) vmWriter.writePush(VMWriter.Segment.THIS, index);
        else if (kind == SymbolTable.Kind.ARG) vmWriter.writePush(VMWriter.Segment.ARG, index);
        else if (kind == SymbolTable.Kind.VAR) vmWriter.writePush(VMWriter.Segment.LOCAL, index);
    }

    private void popVariable(String name) {
        SymbolTable.Kind kind = symbolTable.kindOf(name);
        int index = symbolTable.indexOf(name);
        if (kind == SymbolTable.Kind.STATIC) vmWriter.writePop(VMWriter.Segment.STATIC, index);
        else if (kind == SymbolTable.Kind.FIELD) vmWriter.writePop(VMWriter.Segment.THIS, index);
        else if (kind == SymbolTable.Kind.ARG) vmWriter.writePop(VMWriter.Segment.ARG, index);
        else if (kind == SymbolTable.Kind.VAR) vmWriter.writePop(VMWriter.Segment.LOCAL, index);
    }
}