import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Parser {
    private Scanner scanner;
    private String[] commandParts;
    private static String[] OPTS_COMMANDS = new String[] {
        "add", "sub", "not", "neg", "and", "or", "gt", "lt", "eq"
    };
    private String type;
    private String arg1;
    private int arg2;

    public Parser(File filename) throws IOException {
        this.scanner = new Scanner(filename);
    }

    public boolean hasMoreCommands() {
        return scanner.hasNextLine();
    }

    public boolean isArithmetic() {
        if (this.type == null) return false;
        for (String opt : OPTS_COMMANDS) {
            if (opt.equals(this.type)) {
                return true;
            }
        }
        return false;
    }

    public void advance() {
        this.commandParts = null;
        this.type = null;
        this.arg1 = null;
        this.arg2 = 0;

        while (hasMoreCommands()) {
            String line = scanner.nextLine();
            int commentIdx = line.indexOf("//");
            if (commentIdx != -1) {
                line = line.substring(0, commentIdx);
            }
            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            commandParts = line.split("\\s+");
            this.type = commandParts[0];

            if (isArithmetic()) {
                this.arg1 = this.type;
                return;
            }

            if (commandParts.length > 1) {
                this.arg1 = commandParts[1];
            }
            if (commandParts.length > 2) {
                this.arg2 = Integer.parseInt(commandParts[2]);
            }
            return;
        }
    }

    public String commandType() {
        return this.type;
    }

    public String arg1() {
        return this.arg1;
    }

    public int arg2() {
        return this.arg2;
    }
}