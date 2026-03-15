import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private static int PROGRAM_START_ADDR = 0;
    private static int PROGRAM_END_ADDR = 32676;
    private static int DATA_START_ADDR = 16;
    private static int DATA_END_ADDR = 16384;
    private Map<String, Integer> table;
    private int progAddr;
    private int dataAddr;

    public SymbolTable() {
        this.table = new HashMap<>();
        this.table.put("SP", 0);
        this.table.put("LCL", 1);
        this.table.put("ARG", 2);
        this.table.put("THIS", 3);
        this.table.put("THAT", 4);
        for (int i = 0; i <= 15; i++) {
            table.put("R" + i, i);
        }
        this.table.put("SCREEN", 16384);
        this.table.put("KBD", 24576); 

        this.progAddr = PROGRAM_START_ADDR;
        this.dataAddr = DATA_START_ADDR;
    }

    public int getProgAddr() {
        return this.progAddr;
    }

    public int getDataAddr() {
        return this.dataAddr;
    }
    
    public int getAddr(String symbol) {
        return this.table.get(symbol);
    }

    public void addEntry(String symbol, int addr) {
        this.table.put(symbol, Integer.valueOf(addr));
    }

    public boolean contains (String symbol) {
        return this.table.containsKey(symbol);
    }

    public void incrProgAddr() {
        this.progAddr++;
        if(this.progAddr > PROGRAM_END_ADDR) {
            throw new RuntimeException();
        }
    }

    public void incrDataAddr() {
        this.dataAddr++;
        if(this.dataAddr > DATA_END_ADDR) {
            throw new RuntimeException();
        }
    }

}