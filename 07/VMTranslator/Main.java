import java.io.File;

public class Main {
    public static void main(String[] args) {
        File fileOrDir = (args.length == 0) ? new File(".") : new File(args[0]);

        try {
            if (fileOrDir.isFile() && fileOrDir.getName().endsWith(".vm")) {
                File outputFile = new File(fileOrDir.getAbsolutePath().replace(".vm", ".asm"));
                CodeWriter writer = new CodeWriter(outputFile);
                translateFile(fileOrDir, writer);
                writer.close();
            } 
            else if (fileOrDir.isDirectory()) {
                File[] vmFilesHere = fileOrDir.listFiles((dir, name) -> name.endsWith(".vm"));
                if (vmFilesHere != null && vmFilesHere.length > 0) {
                    File outputFile = new File(fileOrDir, outputNameForDirectory(fileOrDir) + ".asm");
                    CodeWriter writer = new CodeWriter(outputFile);
                    for (File file : vmFilesHere) {
                        translateFile(file, writer);
                    }
                    writer.close();
                }

                File[] subDirs = fileOrDir.listFiles(File::isDirectory);
                if (subDirs != null) {
                    for (File dir : subDirs) {
                        File[] vmFiles = dir.listFiles((d, name) -> name.endsWith(".vm"));
                        if (vmFiles != null && vmFiles.length > 0) {
                            File outputFile = new File(dir, dir.getName() + ".asm");
                            CodeWriter writer = new CodeWriter(outputFile);
                            for (File file : vmFiles) {
                                translateFile(file, writer);
                            }
                            writer.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during translation:");
            e.printStackTrace();
        }
    }

    private static String outputNameForDirectory(File dir) throws Exception {
        String name = dir.getName();
        if (name.equals(".") || name.isEmpty()) {
            return dir.getCanonicalFile().getName();
        }
        return name;
    }

    private static void translateFile(File inputFile, CodeWriter writer) throws Exception {
        Parser parser = new Parser(inputFile);
        
        String fileName = inputFile.getName();
        if (fileName.lastIndexOf(".") != -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        writer.setFileName(fileName);

        while (parser.hasMoreCommands()) {
            parser.advance();
            
            String cmd = parser.commandType();
            
            if (cmd == null || cmd.isEmpty()) {
                continue;
            }

            if (parser.isArithmetic()) {
                writer.writeArithmetic(cmd);
            } 
            else if (cmd.equals("push") || cmd.equals("pop")) {
                writer.writePushPop(cmd, parser.arg1(), parser.arg2());
            }
        }
    }
}