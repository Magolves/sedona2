package sedonacmt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainMulti {

    public static void main(String[] args) {
        Path path = Paths.get(".");

        if (args.length > 0) {
            path = Paths.get(args[0]);
        }

        List<Path> paths;
        try {
            paths = listFiles(path);

            compile(paths, args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // list all files from this path
    public static List<Path> listFiles(Path path) throws IOException {
        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.endsWith("kit.xml"))
                    .collect(Collectors.toList());
        }
        return result;

    }

    public static void compile(List<Path> kitFiles, String[] args) {
        KitSequencer kitSequencer = new KitSequencer(kitFiles);
        kitSequencer.compileKits(args);

        if (kitSequencer.getKits().size() > 0) {
            postCompile(args, kitSequencer.getKits());
        }
    }

    /**
     * Runs optional post-compile steps which work on all compiled kits.
     *
     * @param args the command-line arguments
     * @param kits the kits processed by the multi-compiler
     */
    public static void postCompile(String[] args, List<KitProxy> kits) {
        List<MultiCompilerStep> postCompileSteps;

        MultiCompilerContext multiCompilerContext = new MultiCompilerContext(args, kits);

        postCompileSteps = new ArrayList<>();
        postCompileSteps.add(new WriteKitsUmlDoc(multiCompilerContext));

        multiCompilerContext.log.info(String.format("Executing post-compile (%d steps)", postCompileSteps.size()));

        int successes = 0;
        for (MultiCompilerStep multiCompilerStep : postCompileSteps) {
            try {
                multiCompilerContext.log.info(String.format("  Post-compile step [%s] ...", multiCompilerStep));
                multiCompilerStep.run();

                successes++;
                multiCompilerContext.log.info(String.format("  Post-compile step [%s] finished", multiCompilerStep));
            } catch (Exception e) {
                multiCompilerContext.log.error(String.format("Step %s failed: %s", multiCompilerStep, e.getMessage()));
            }
        }

        multiCompilerContext.log.info(String.format("  Post-compile s finished [%d/%d steps]", successes, postCompileSteps.size()));

    }
}
