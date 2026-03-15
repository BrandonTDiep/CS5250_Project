import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Assembler {
    private File assembly;
    private BufferedWriter binary;
    private Code encoder;
    private SymbolTable table;

    public Assembler(File source, File target) throws IOException {
        this.assembly = source;
        this.binary = new BufferedWriter(new FileWriter(target));
        
        this.encoder = new Code();
        this.table = new SymbolTable();
    }

    public void parse() throws IOException {
        Parser parser = new Parser(this.assembly);
        while(parser.hasMoreCommands()) {
            parser.advance();
            String commandType = parser.commandType();
        

            if (commandType.equals("L_COMMAND")) {
                String symbol = parser.symbol();
                this.table.addEntry(symbol, this.table.getProgAddr());
            }
            else {
                this.table.incrProgAddr();
            }
        }
        parser.close();
    }

    public void parseSecond() throws IOException {
        Parser parser = new Parser(this.assembly);
        boolean firstLine = true; 
        
        while (parser.hasMoreCommands()) {
            parser.advance();
            String instruction = null;
            String commandType = parser.commandType();

            if (commandType.equals("A_COMMAND")) {
                String address = null;
                String symbol = parser.symbol();

                boolean isSymbol = !Character.isDigit(symbol.charAt(0));

                if (isSymbol) {
                    if (!this.table.contains(symbol)) {
                        this.table.addEntry(symbol, this.table.getDataAddr());
                        this.table.incrDataAddr();
                    }
                    address = Integer.toString(this.table.getAddr(symbol));
                }
                else {
                    address = symbol;
                }
                instruction = "0" + this.encoder.convertToBinary(address);
            }
            else if (commandType.equals("C_COMMAND")) {
                String jump = parser.jump();
                String comp = parser.comp();
                String dest = parser.dest();

                instruction = "111" + this.encoder.comp(comp) + this.encoder.dest(dest) + this.encoder.jump(jump);
            }


            if (!commandType.equals("L_COMMAND")) {
                if (!firstLine) {
                    this.binary.newLine(); 
                }
                this.binary.write(instruction);
                firstLine = false;
            }
        }
        
        parser.close();
        this.binary.flush();
        this.binary.close();
    }

}