import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Path asmDir = Paths.get("asm");
        Path hackDir = Paths.get("hack");

        if (!Files.exists(asmDir)) {
            try {
                System.err.println("Error: 'asm' directory not found.");
                Files.createDirectory(asmDir);
            } catch (IOException ex) {
                System.err.println("Error creating 'asm' directory: " + ex.getMessage());
                return;
            }
        }

        if (!Files.exists(hackDir)) {
            try {
                System.err.println("Error: 'hack' directory not found.");
                Files.createDirectory(hackDir);
            } catch (IOException ex) {
                System.err.println("Error creating 'hack' directory: " + ex.getMessage());
                return;
            }
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(asmDir, "*.asm")) {
            for (Path entry : stream) {
                File inputFile = entry.toFile();
                String outName = inputFile.getName().replace(".asm", ".hack");
                File outputFile = new File(hackDir.toFile(), outName);


                Assembler assembler = new Assembler(inputFile, outputFile);
                assembler.parse();       
                assembler.parseSecond(); 
            }
        } catch (IOException ex) {
            System.err.println("Error processing files: " + ex.getMessage());
        }
    }
}