import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class VMWriter {
    public enum Segment { CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP }
    public enum Command { ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT }

    private PrintWriter writer;

    public VMWriter(File outFile) {
        try {
            writer = new PrintWriter(new FileWriter(outFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePush(Segment seg, int index) {
        writer.println("push " + getSegString(seg) + " " + index);
    }

    public void writePop(Segment seg, int index) {
        writer.println("pop " + getSegString(seg) + " " + index);
    }

    public void writeArithmetic(Command command) {
        writer.println(command.name().toLowerCase());
    }

    public void writeLabel(String label) {
        writer.println("label " + label);
    }

    public void writeGoto(String label) {
        writer.println("goto " + label);
    }

    public void writeIf(String label) {
        writer.println("if-goto " + label);
    }

    public void writeCall(String name, int nArgs) {
        writer.println("call " + name + " " + nArgs);
    }

    public void writeFunction(String name, int nLocals) {
        writer.println("function " + name + " " + nLocals);
    }

    public void writeReturn() {
        writer.println("return");
    }

    public void close() {
        writer.close();
    }

    private String getSegString(Segment seg) {
        switch (seg) {
            case CONST: return "constant";
            case ARG: return "argument";
            case LOCAL: return "local";
            case STATIC: return "static";
            case THIS: return "this";
            case THAT: return "that";
            case POINTER: return "pointer";
            case TEMP: return "temp";
            default: return "";
        }
    }
}