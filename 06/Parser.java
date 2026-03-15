import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Parser {
    private BufferedReader reader;
    private String currLine;
    private String nextLine;

    public Parser(File filename) throws IOException {
        try {
            this.reader = new BufferedReader(new FileReader(filename));
            this.currLine = null;
            this.nextLine = this.getLine();
        } catch (FileNotFoundException e) {
            System.out.println("Error file missing");
            
        } catch (IOException e) {
            System.out.println("Error can't read file");
        } 
    }

    public String getLine() throws IOException {
        String line = this.reader.readLine();

        while(line != null) {
            line = line.trim();
            
            if(line.isEmpty() || line.startsWith("//")) {
                line = this.reader.readLine();
                continue;
            }
            int commentIdx = line.indexOf("//");
            if(commentIdx != -1) {
                line = line.substring(0, commentIdx);
            }
            return line;
        }
        return null;
 
    }

    public void advance() {
        this.currLine = this.nextLine;
        try {
            this.nextLine = getLine();
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
    }

    public String commandType() {
        String line = this.currLine.trim();

        if (line.startsWith("(") && line.endsWith(")")) {
            return "L_COMMAND";
        }
        else if (line.startsWith("@")) {
            return "A_COMMAND";
        }
        else {
            return "C_COMMAND";
        }
    }

    public String symbol() {
        String line = this.currLine.trim();

        if (this.commandType().equals("L_COMMAND")){
            return line.substring(1, currLine.length() - 1);
        }
        else if (this.commandType().equals("A_COMMAND")) {
            return line.substring(1);
        }         
        else {
           return null; 
        }
    }

    public String jump() {
        String line = this.currLine.trim();
        if (line.contains(";"))
            return line.substring(line.indexOf(";") + 1);
        return null;
    }

    public String comp() {
        String line = this.currLine.trim();
        if (line.contains("="))
            line = line.substring(line.indexOf("=") + 1);
        if (line.contains(";"))
            return line.substring(0, line.indexOf(";"));
        return line;
    }

    public String dest() {
        String line = this.currLine.trim();
        if (line.contains("="))
            return line.substring(0, line.indexOf("="));
        return null;
    }


    public void close() {
        try {
            this.reader.close();
        } catch (IOException e) {
            System.out.println("Error file close");
        }
    }

    public boolean hasMoreCommands() {
        return this.nextLine != null;
    }

    @Override
    public void finalize() {
        this.close();
    }

}