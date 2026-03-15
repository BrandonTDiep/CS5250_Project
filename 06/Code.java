import java.util.HashMap;
import java.util.Map;

public class Code {
    private Map<String, String> jumpMap;
    private Map<String, String> compMap;
    private Map<String, String> destMap;

    public Code() {
        this.jumpMap = new HashMap<>();
        this.compMap = new HashMap<>();
        this.destMap = new HashMap<>();

        // jump mapping
        this.jumpMap.put("NULL", "000"); 
        this.jumpMap.put("JGT", "001"); 
        this.jumpMap.put("JEQ", "010");
        this.jumpMap.put("JGE", "011"); 
        this.jumpMap.put("JLT", "100"); 
        this.jumpMap.put("JNE", "101");
        this.jumpMap.put("JLE", "110"); 
        this.jumpMap.put("JMP", "111");

        // comp mappings
        this.compMap.put("0", "0101010");
        this.compMap.put("1", "0111111");
        this.compMap.put("-1", "0111010");
        this.compMap.put("D", "0001100");
        this.compMap.put("A", "0110000");
        this.compMap.put("M", "1110000");
        this.compMap.put("!D", "0001101");
        this.compMap.put("!A", "0110001");
        this.compMap.put("!M", "1110001");
        this.compMap.put("-D", "0001111");
        this.compMap.put("-A", "0110011");
        this.compMap.put("-M", "1110011");
        this.compMap.put("D+1", "0011111");
        this.compMap.put("A+1", "0110111");
        this.compMap.put("M+1", "1110111");
        this.compMap.put("D-1", "0001110");
        this.compMap.put("A-1", "0110010");
        this.compMap.put("M-1", "1110010");
        this.compMap.put("D+A", "0000010");
        this.compMap.put("D+M", "1000010");
        this.compMap.put("D-A", "0010011");
        this.compMap.put("D-M", "1010011");
        this.compMap.put("A-D", "0000111");
        this.compMap.put("M-D", "1000111");
        this.compMap.put("D&A", "0000000");
        this.compMap.put("D&M", "1000000");
        this.compMap.put("D|A", "0010101");
        this.compMap.put("D|M", "1010101");   

        // dest mapping
        this.destMap.put("NULL", "000"); 
        this.destMap.put("M", "001"); 
        this.destMap.put("D", "010");
        this.destMap.put("MD", "011");
        this.destMap.put("A", "100"); 
        this.destMap.put("AM", "101");
        this.destMap.put("AD", "110"); 
        this.destMap.put("AMD", "111");
    }

    public String jump(String mnemonic) {
        return jumpMap.getOrDefault(mnemonic, "000");
    }

    public String comp(String mnemonic) {
        return compMap.getOrDefault(mnemonic, "0000000");
    }

    public String dest(String mnemonic) {
        return destMap.getOrDefault(mnemonic, "000");
    }

    public String convertToBinary(String number) {
        int decimal = Integer.parseInt(number);
        String binary = Integer.toBinaryString(decimal);
        return String.format("%15s", binary).replace(' ', '0');
    }

}