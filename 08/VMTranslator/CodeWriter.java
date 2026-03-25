import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeWriter {
    private int condLabel = 0;
    private String fileName;
    private PrintWriter out;
    private int callLabel = 0;
    private String currentFunction = "";

    public CodeWriter(File output) throws FileNotFoundException {
        this.out = new PrintWriter(output);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void writeInit() {
        this.out.write("@256\n");
        this.out.write("D=A\n");
        this.out.write("@SP\n");
        this.out.write("M=D\n");

        writeCall("Sys.init", 0);
    }

    public void writeArithmetic(String command) throws IOException {
        if(command.equals("add")) {
            this.out.write("@SP\n");
            this.out.write("AM=M-1\n");
            this.out.write("D=M\n");
            this.out.write("A=A-1\n");
            this.out.write("M=D+M\n");
        }
        else if(command.equals("sub")) {
            this.out.write("@SP\n");
            this.out.write("AM=M-1\n");
            this.out.write("D=M\n");
            this.out.write("A=A-1\n");
            this.out.write("M=M-D\n");
        }
        else if(command.equals("not")) {
            this.out.write("@SP\n");
            this.out.write("A=M-1\n");
            this.out.write("M=!M\n");
        }
        else if(command.equals("neg")) {
            this.out.write("@SP\n");
            this.out.write("A=M-1\n");
            this.out.write("M=-M\n");
        }
        else if(command.equals("or")) {
            this.out.write("@SP\n");
            this.out.write("AM=M-1\n");
            this.out.write("D=M\n");
            this.out.write("A=A-1\n");
            this.out.write("M=D|M\n");
        }
        else if(command.equals("and")) {
            this.out.write("@SP\n");
            this.out.write("AM=M-1\n");
            this.out.write("D=M\n");
            this.out.write("A=A-1\n");
            this.out.write("M=D&M\n");
        }
        else if (command.equals("eq") || command.equals("gt") || command.equals("lt")) {
            String jumpCommand = command.equals("eq") ? "JEQ" : (command.equals("gt") ? "JGT" : "JLT");            
            this.out.write("@SP\n");
            this.out.write("AM=M-1\n");
            this.out.write("D=M\n");
            this.out.write("A=A-1\n");
            this.out.write("D=M-D\n");
            this.out.write("@TRUE" + condLabel + "\n");
            this.out.write("D;" + jumpCommand + "\n");
            this.out.write("@SP\n");
            this.out.write("A=M-1\n");
            this.out.write("M=0\n");
            this.out.write("@END" + condLabel+ "\n");
            this.out.write("0;JMP\n");
            this.out.write("(TRUE" + condLabel + ")" + "\n");
            this.out.write("@SP\n");
            this.out.write("A=M-1\n");
            this.out.write("M=-1\n");
            this.out.write("(END" + condLabel + ")" + "\n");
            condLabel++;
        }
        else {
            throw new IllegalArgumentException("Error: wrong command");
        }
    }

    public void writeLabel(String label) {
        String scoped = (currentFunction == null || currentFunction.isEmpty()) ? label : currentFunction + "$" + label;
        this.out.write("(" + scoped + ")\n");
    }

    public void writeGoto(String label) {
        String scoped = (currentFunction == null || currentFunction.isEmpty()) ? label : currentFunction + "$" + label;
        this.out.write("@" + scoped + "\n");
        this.out.write("0;JMP\n");
    }

    public void writeIf(String label) {
        String scoped = (currentFunction == null || currentFunction.isEmpty()) ? label : currentFunction + "$" + label;
        this.out.write("@SP\n");
        this.out.write("AM=M-1\n");
        this.out.write("D=M\n");
        this.out.write("@" + scoped + "\n");
        this.out.write("D;JNE\n");
    }

    public void writeFunction(String functionName, int numLocals) {
        this.currentFunction = functionName;
        this.out.write("(" + functionName + ")\n");

        for (int i = 0; i < numLocals; i++) {
            this.out.write("@0\n");
            this.out.write("D=A\n");
            this.out.write("@SP\n");
            this.out.write("A=M\n");
            this.out.write("M=D\n");
            this.out.write("@SP\n");
            this.out.write("M=M+1\n");
        }
    }

    public void writeCall(String functionName, int numArgs) {
        String returnLabel = "RET_" + functionName + "_" + (callLabel++);

        this.out.write("@" + returnLabel + "\n");
        this.out.write("D=A\n");
        this.out.write("@SP\n");
        this.out.write("A=M\n");
        this.out.write("M=D\n");
        this.out.write("@SP\n");
        this.out.write("M=M+1\n");

        this.out.write("@LCL\n");
        this.out.write("D=M\n");
        this.out.write("@SP\n");
        this.out.write("A=M\n");
        this.out.write("M=D\n");
        this.out.write("@SP\n");
        this.out.write("M=M+1\n");

        this.out.write("@ARG\n");
        this.out.write("D=M\n");
        this.out.write("@SP\n");
        this.out.write("A=M\n");
        this.out.write("M=D\n");
        this.out.write("@SP\n");
        this.out.write("M=M+1\n");

        this.out.write("@THIS\n");
        this.out.write("D=M\n");
        this.out.write("@SP\n");
        this.out.write("A=M\n");
        this.out.write("M=D\n");
        this.out.write("@SP\n");
        this.out.write("M=M+1\n");

        this.out.write("@THAT\n");
        this.out.write("D=M\n");
        this.out.write("@SP\n");
        this.out.write("A=M\n");
        this.out.write("M=D\n");
        this.out.write("@SP\n");
        this.out.write("M=M+1\n");

        this.out.write("@SP\n");
        this.out.write("D=M\n");
        this.out.write("@5\n");
        this.out.write("D=D-A\n");
        this.out.write("@" + numArgs + "\n");
        this.out.write("D=D-A\n");
        this.out.write("@ARG\n");
        this.out.write("M=D\n");

        this.out.write("@SP\n");
        this.out.write("D=M\n");
        this.out.write("@LCL\n");
        this.out.write("M=D\n");

        this.out.write("@" + functionName + "\n");
        this.out.write("0;JMP\n");

        this.out.write("(" + returnLabel + ")\n");
    }

    public void writeReturn() {
        this.out.write("@LCL\n");
        this.out.write("D=M\n");
        this.out.write("@R13\n");
        this.out.write("M=D\n");

        this.out.write("@R13\n");
        this.out.write("D=M\n");
        this.out.write("@5\n");
        this.out.write("A=D-A\n");
        this.out.write("D=M\n");
        this.out.write("@R14\n");
        this.out.write("M=D\n");

        this.out.write("@SP\n");
        this.out.write("AM=M-1\n");
        this.out.write("D=M\n");
        this.out.write("@ARG\n");
        this.out.write("A=M\n");
        this.out.write("M=D\n");

        this.out.write("@ARG\n");
        this.out.write("D=M+1\n");
        this.out.write("@SP\n");
        this.out.write("M=D\n");

        this.out.write("@R13\n");
        this.out.write("A=M-1\n");
        this.out.write("D=M\n");
        this.out.write("@THAT\n");
        this.out.write("M=D\n");

        this.out.write("@R13\n");
        this.out.write("D=M\n");
        this.out.write("@2\n");
        this.out.write("A=D-A\n");
        this.out.write("D=M\n");
        this.out.write("@THIS\n");
        this.out.write("M=D\n");

        this.out.write("@R13\n");
        this.out.write("D=M\n");
        this.out.write("@3\n");
        this.out.write("A=D-A\n");
        this.out.write("D=M\n");
        this.out.write("@ARG\n");
        this.out.write("M=D\n");

        this.out.write("@R13\n");
        this.out.write("D=M\n");
        this.out.write("@4\n");
        this.out.write("A=D-A\n");
        this.out.write("D=M\n");
        this.out.write("@LCL\n");
        this.out.write("M=D\n");

        this.out.write("@R14\n");
        this.out.write("A=M\n");
        this.out.write("0;JMP\n");
    }

    public void writePushPop(String command, String segment, int index) {
        if (command.equals("push")) {
            if (segment.equals("constant")) {
                this.out.write("@" + index + "\n");
                this.out.write("D=A\n");
                this.out.write("@SP\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("M=M+1\n");
            }
            else if (segment.equals("local")) {
                this.out.write("@LCL\n");
                this.out.write("D=M\n");
                this.out.write("@" + index + "\n");
                this.out.write("A=D+A\n");
                this.out.write("D=M\n");
                this.out.write("@SP\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("M=M+1\n");
            }
            else if (segment.equals("argument")) {
                this.out.write("@ARG\n");
                this.out.write("D=M\n");
                this.out.write("@" + index + "\n");
                this.out.write("A=D+A\n");
                this.out.write("D=M\n");
                this.out.write("@SP\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("M=M+1\n");
            }
            else if (segment.equals("this")) {
                this.out.write("@THIS\n");
                this.out.write("D=M\n");
                this.out.write("@" + index + "\n");
                this.out.write("A=D+A\n");
                this.out.write("D=M\n");
                this.out.write("@SP\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("M=M+1\n");
            }
            else if (segment.equals("that")) {
                this.out.write("@THAT\n");
                this.out.write("D=M\n");
                this.out.write("@" + index + "\n");
                this.out.write("A=D+A\n");
                this.out.write("D=M\n");
                this.out.write("@SP\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("M=M+1\n");
            }
            else if (segment.equals("temp")) {
                this.out.write("@" + (index + 5) + "\n");
                this.out.write("D=M\n");
                this.out.write("@SP\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("M=M+1\n");
            }
            else if (segment.equals("pointer")) {
                if(index == 0) {
                    this.out.write("@THIS\n");
                    this.out.write("D=M\n");
                }
                else if(index == 1) {
                    this.out.write("@THAT\n");
                    this.out.write("D=M\n");
                } else {
                    throw new IllegalArgumentException("pointer index must be 0 or 1");
                }
                this.out.write("@SP\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("M=M+1\n");
            }
            else if (segment.equals("static")) {
                this.out.write("@" + fileName + "." + index + "\n");
                this.out.write("D=M\n");
                this.out.write("@SP\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("M=M+1\n");
            }
            else {
                throw new IllegalArgumentException("Unknown segment for push: " + segment);
            }
            
        }
        else if (command.equals("pop")) {
            if (segment.equals("local")) {
                this.out.write("@LCL\n");
                this.out.write("D=M\n");
                this.out.write("@" + index + "\n");
                this.out.write("D=D+A\n");
                this.out.write("@R13\n");
                this.out.write("M=D\n");          
                this.out.write("@SP\n");
                this.out.write("AM=M-1\n");
                this.out.write("D=M\n");
                this.out.write("@R13\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
            }
            else if (segment.equals("argument")) {
                this.out.write("@ARG\n");
                this.out.write("D=M\n");
                this.out.write("@" + index + "\n");
                this.out.write("D=D+A\n");
                this.out.write("@R13\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("AM=M-1\n");
                this.out.write("D=M\n");
                this.out.write("@R13\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
            }
            else if (segment.equals("this")) {
                this.out.write("@THIS\n");
                this.out.write("D=M\n");
                this.out.write("@" + index + "\n");
                this.out.write("D=D+A\n");
                this.out.write("@R13\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("AM=M-1\n");
                this.out.write("D=M\n");
                this.out.write("@R13\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
            }
            else if (segment.equals("that")) {
                this.out.write("@THAT\n");
                this.out.write("D=M\n");
                this.out.write("@" + index + "\n");
                this.out.write("D=D+A\n");
                this.out.write("@R13\n");
                this.out.write("M=D\n");
                this.out.write("@SP\n");
                this.out.write("AM=M-1\n");
                this.out.write("D=M\n");
                this.out.write("@R13\n");
                this.out.write("A=M\n");
                this.out.write("M=D\n");
            }
            else if (segment.equals("temp")) {
                this.out.write("@SP\n");
                this.out.write("AM=M-1\n");
                this.out.write("D=M\n");
                this.out.write("@" + (index + 5) + "\n");
                this.out.write("M=D\n");
            }
            else if (segment.equals("pointer")) {
                if(index == 0) {
                    this.out.write("@SP\n");
                    this.out.write("AM=M-1\n");
                    this.out.write("D=M\n");
                    this.out.write("@THIS\n");
                    this.out.write("M=D\n");
                }
                else if(index == 1) {
                    this.out.write("@SP\n");
                    this.out.write("AM=M-1\n");
                    this.out.write("D=M\n");
                    this.out.write("@THAT\n");
                    this.out.write("M=D\n");
                } else {
                    throw new IllegalArgumentException("pointer index must be 0 or 1");
                }
            }
            else if (segment.equals("static")) {
                this.out.write("@SP\n");
                this.out.write("AM=M-1\n");
                this.out.write("D=M\n");
                this.out.write("@" + fileName + "." + index + "\n");
                this.out.write("M=D\n");
            }
            else {
                throw new IllegalArgumentException("Unknown segment for pop: " + segment);
            }
        }
    }

    public void close() {
        this.out.close();
    }
}



