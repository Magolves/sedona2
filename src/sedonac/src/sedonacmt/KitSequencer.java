package sedonacmt;

import sedonac.CompilerLog;
import sedonac.util.Env;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class KitSequencer {
    private CompilerLog log;
    private List<KitProxy> kits;
    private List<KitProxy> failedKits;
    private List<List<KitProxy>> compileSteps;

    public KitSequencer(List<Path> kitFiles) {
        log = new CompilerLog();
        kits = new ArrayList<>();
        failedKits = new ArrayList<>();

        log.setLogName("Make");

        setupKits(kitFiles);
        setupCompileSequence();
        makeSequence();
    }

    public List<KitProxy> getKits() {
        return kits;
    }

    private void setupKits(List<Path> kitFiles) {
        for (Path kitFile : kitFiles) {
            try {
                KitProxy kitProxy = new KitProxy(kitFile.toFile());
                kits.add(kitProxy);
                log.info(kitProxy.toString());
            } catch (Exception e) {
                log.warn(String.format("Error parsing '%s': %s", kitFile, e.getMessage()));
            }
        }
    }

    /**
     * Determines compile sequence according to the kit dependencies.
     */
    private void setupCompileSequence() {
        if (kits.size() == 0) {
            log.info("Nothing to compile");
            return;
        }

        kits.sort(KitProxy::compareByDependency);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kits.size(); i++) {
            KitProxy kitProxy = kits.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(kitProxy.kitName());
        }

        log.info(String.format("Sequence is [%s]", sb.toString()));
    }

    /**
     * Assembles compilation stages in a way that all required kits are built before the next
     * stage is executed. For Sedona this means that only sys can be compiled in the first stage while
     * in the next stage all kits which solely depend on sys can be build and so on.
     */
    private void makeSequence() {
        if (kits.size() == 0) {
            return;
        }

        compileSteps = new ArrayList<>();
        List<KitProxy> remainingKits = new ArrayList<>(kits);

        int step = 0;
        while(remainingKits.size() > 0) {
            compileSteps.add(new ArrayList<>());

            for (int i = 0; i < remainingKits.size(); i++) {
                KitProxy kitProxy = remainingKits.get(i);
                if (!kitProxy.dependsOnAny(remainingKits)) {
                    compileSteps.get(step).add(kitProxy);
                }
            }

            for (int i = 0; i < compileSteps.get(step).size(); i++) {
                KitProxy kitProxy = compileSteps.get(step).get(i);
                remainingKits.remove(kitProxy);

                log.debug(String.format("Step %d: %s", step, kitProxy));
            }
            ++step;
        }

        log.info(String.format("Compile %d kits in %d steps", kits.size(), step));
    }

    /**
     * Compiles all kits that have been found and passes the given commandline arguments
     * to each compiler instance.
     * @param args the command line arguments
     */
    public void compileKits(String[] args) {
        if (null == compileSteps || compileSteps.size() == 0) {
            return;
        }

        // FIXME: IrReader fails if multiple compilation threads running in parallel
        int cores = 1; //Runtime.getRuntime().availableProcessors();
        //log.info(String.format("Setup compile thread pool using %d cores", cores));
        List<Future<Result>> results = new ArrayList<>();


        long duration = -1L;
        for (int i = 0; i < compileSteps.size(); i++) {
            log.info(String.format("Compile [%d/%d]", (i+1), compileSteps.size()));
            List<CompilerTask> tasks = new ArrayList<>();
            List<KitProxy> kitProxies = compileSteps.get(i);

            ExecutorService pool = Executors.newFixedThreadPool(Math.min(cores, kitProxies.size()));

            for (KitProxy kitProxy : kitProxies) {
                tasks.add(new CompilerTask(kitProxy, args));
            }

            try {
                long start = System.currentTimeMillis();

                final List<Future<Result>> stepResults = pool.invokeAll(tasks);

                results.addAll(stepResults);
                pool.shutdown();
                pool.awaitTermination(10, TimeUnit.MILLISECONDS);
                duration = System.currentTimeMillis() - start;

                for (Future<Result> resultFuture : stepResults) {
                    final Result result = resultFuture.get();
                    if (!result.isSuccess()) {
                        i = compileSteps.size();
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Compilation failed", e);
                return;
            }
        }

        log.info(String.format("Compiled %d/%d kits (%d failed) in %d ms", results.size(), kits.size(), failedKits.size(), duration));

        for (Future<Result> future : results) {
            try {
                Result result = future.get();
                if (!result.isSuccess()) {
                    log.error(String.format("Failed: %s (%d)", result.getName(), result.getResult()));
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }



    static class Result {
        private String name;
        private String timestamp;
        private int result;

        public Result(String name, String timestamp, int result) {
            this.name = name;
            this.timestamp = timestamp;
            this.result = result;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public int getResult() {
            return result;
        }

        public boolean isSuccess() {
            return result == 0;
        }
    }

    class CompilerTask implements Callable<Result> {
        private String[] compilerArgs;
        private KitProxy kitProxy;

        CompilerTask(KitProxy kitProxy, String[] args) {
            this.compilerArgs = new String[args.length];
            this.kitProxy = kitProxy;

            System.arraycopy(args, 0, compilerArgs, 0, args.length);
            compilerArgs[0] = kitProxy.getKitFile().getAbsolutePath();
        }

        @Override
        public Result call() {
            final File kitFile = kitProxy.getKitFile();

            final String sedona_home = System.getenv(Env.SEDONA_HOME);
            if (sedona_home != null) {
                System.setProperty("sedona.home", sedona_home);
            }

            final String niagara_home = System.getenv(Env.NIAGARA_HOME);
            if (niagara_home != null) {
                System.setProperty("baja.home", niagara_home);
            }

            System.setProperty("user.dir", kitFile.getAbsolutePath());

            int result = sedonac.Main.doMain(compilerArgs);

            if (0 == result) {
                log.info(String.format("Compilation of kit %s/%s finished",
                        kitFile.getParentFile().getName(),
                        kitFile.getName()));
            } else {
                failedKits.add(kitProxy);
                kitProxy.setContainsErrors(true);
                log.error(String.format("Compilation of kit %s/%s failed with code %d",
                        kitFile.getParentFile().getName(),
                        kitFile.getName(),
                        result));
            }

            return new Result(kitProxy.kitName(), LocalDateTime.now().toString(), result);
        }


    }

}
