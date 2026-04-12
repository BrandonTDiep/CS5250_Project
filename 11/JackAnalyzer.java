import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JackAnalyzer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java JackAnalyzer <input file or directory>");
            return;
        }

        File fileIn = new File(args[0]);
        List<File> jackFiles = new ArrayList<>();

        // If it's a single file
        if (fileIn.isFile() && fileIn.getName().endsWith(".jack")) {
            jackFiles.add(fileIn);
        } 
        // If it's a directory
        else if (fileIn.isDirectory()) {
            File[] files = fileIn.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".jack")) {
                        jackFiles.add(f);
                    }
                }
            }
        }

        for (File f : jackFiles) {
            String outPath = f.getAbsolutePath().replace(".jack", ".xml");
            File fileOut = new File(outPath);
            System.out.println("Compiled " + f.getName() + " to " + fileOut.getName());
            
            CompilationEngine engine = new CompilationEngine(f, fileOut);
            engine.compileClass();
        }
    }
}