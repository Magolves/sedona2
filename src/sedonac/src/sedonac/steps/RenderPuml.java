package sedonac.steps;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.util.FileUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class RenderPuml extends CompilerStep {
    // Class names for PlantUML
    public static final String METHOD_GENERATED_IMAGES = "getGeneratedImages";
    public static final String METHOD_GET_PNG_FILE = "getPngFile";

    public static final String NET_SOURCEFORGE_PLANTUML_FILE_FORMAT_OPTION = "net.sourceforge.plantuml.FileFormatOption";
    public static final String NET_SOURCEFORGE_PLANTUML_FILE_FORMAT = "net.sourceforge.plantuml.FileFormat";
    public static final String NET_SOURCEFORGE_PLANTUML_SOURCE_FILE_READER = "net.sourceforge.plantuml.SourceFileReader";
    public static final String NET_SOURCEFORGE_PLANTUML_GENERATED_IMAGE = "net.sourceforge.plantuml.GeneratedImage";

    public RenderPuml(Compiler compiler) {
        super(compiler);

        checkForPlantUml(compiler);

    }

    @Override
    public void run() {
        if (!compiler.umlDoc) {
            return;
        }

        int cores = Runtime.getRuntime().availableProcessors();
        log.info(String.format("Setup compile thread pool using %d cores", cores));
        List<Future<Result>> results = new ArrayList<>();
        List<RenderTask> tasks = new ArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(cores);

        for(String key : compiler.umlMap.keySet()) {
            tasks.add(new RenderTask(key, compiler.umlMap.get(key)));
        }


        long start = System.currentTimeMillis();
        long duration = -1L;

        try {
            results.addAll(pool.invokeAll(tasks));
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.MINUTES);
            duration  = System.currentTimeMillis() - start;
        } catch (InterruptedException e) {
            log.error("  Rendering failed", e);
        }

        log.info(String.format("Rendered %d diagrams in %d s (%d cores)", results.size(), duration, cores));

        for (Future<Result> future : results) {
            try {
                Result result = future.get();
                if (result.isSuccess()) {
                    //log.info(String.format("Update diagram [%s]", result.getDiagramKey()));
                    compiler.umlMap.put(result.getDiagramKey(), result.getDiagramFile());
                } else {
                    log.error(String.format("Failed: %s [%s]", result.getDiagramKey(), result.getDiagramFile().getAbsolutePath()));
                }

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isUpToDate(File umlSourceFile) {
        File umlTargetFile = getTargetDiagramFile(umlSourceFile);

        return FileUtil.isUpToDate(umlSourceFile, umlTargetFile);
    }

    /**
     * Checks if diagram rendering is requested and issues a warning, plantUml is not present in
     * classpath.
     *
     * @param compiler the compiler instance
     */
    private void checkForPlantUml(Compiler compiler) {
        if (compiler.umlRenderWithPlantUml) {
            try {
                Class.forName(NET_SOURCEFORGE_PLANTUML_SOURCE_FILE_READER);
            } catch (ClassNotFoundException e) {
                log.warn("plantUML not found in classpath - cannot generate images");
            }
        }
    }

    /**
     * Gets the PlantUML file processor object via reflection.
     *
     * @param file  the plantUML source file
     * @param isSvg true, if diagram should be rendered as SVG; otherwise PNG is used
     * @return the file processor instance or null, if PlantUML is not present
     */
    private Object getUmlProcessor(File file, boolean isSvg) {
        try {
            Class<?> clazz = Class.forName(NET_SOURCEFORGE_PLANTUML_SOURCE_FILE_READER);

            if (clazz != null) {
                Class<?> formatOptionClazz = Class.forName(NET_SOURCEFORGE_PLANTUML_FILE_FORMAT_OPTION);
                Constructor<?> ctor = clazz.getDeclaredConstructor(File.class, File.class, formatOptionClazz);

                //noinspection JavaReflectionInvocation
                return ctor.newInstance(file, file.getAbsoluteFile().getParentFile(), createFileFormatObject(isSvg));
            }
        } catch (ClassNotFoundException e) {
            log.info("PlantUML: Not in classpath", e);
        } catch (IllegalAccessException e) {
            log.error("PlantUML: Illegal access", e);
        } catch (InstantiationException e) {
            log.error("PlantUML: Cannot instantiate class", e);
        } catch (NoSuchMethodException e) {
            log.error("PlantUML: Missing string constructor", e);
        } catch (InvocationTargetException e) {
            log.error("PlantUML: Cannot invoke constructor", e);
        } catch (Throwable e) {
            log.error("PlantUML: Rendering failed", e);
        }

        return null;
    }

    /**
     * Helper function to create the file format object
     *
     * @param isSvg true, if diagram should be rendered as SVG; otherwise PNG is used
     * @return the file format object which determines the output format
     * @throws Exception error occurred, e. g. class or method not found
     */
    private Object createFileFormatObject(boolean isSvg) throws Exception {
        Class<?> formatOptionClazz = Class.forName(NET_SOURCEFORGE_PLANTUML_FILE_FORMAT_OPTION);
        Class<?> formatClazz = Class.forName(NET_SOURCEFORGE_PLANTUML_FILE_FORMAT);

        Constructor<?> ctor = formatOptionClazz.getDeclaredConstructor(formatClazz);
        if (ctor != null) {

            java.lang.reflect.Field arg;
            if (isSvg) {
                arg = formatClazz.getDeclaredField("SVG");
            } else {
                arg = formatClazz.getDeclaredField("PNG");
            }

            //noinspection JavaReflectionInvocation
            return ctor.newInstance(arg.get(null));
        }

        return null;
    }

    /**
     * Renders the given (plant UML) file as an image. The image is either in PNG or SVG format, depending on
     * option '-usvg'.
     * This method does nothing unless option '-uplant' is set.
     *
     * @param umlSourceFile the (plant UML) file
     * @return the diagram image file or null, if no diagram has been created
     */
    private File renderUmlDiagram(File umlSourceFile) {
        if (!compiler.umlRenderWithPlantUml) return null;

        if (isUpToDate(umlSourceFile)) {
            log.debug(String.format("  RenderPuml [%s is up-to-date]", umlSourceFile.getName()));
            return getTargetDiagramFile(umlSourceFile);
        }

        Object reader = getUmlProcessor(umlSourceFile, compiler.umlSvg);

        if (reader != null) {
            try {
                log.debug(String.format("  RenderPuml [Render %s...]", umlSourceFile.getName()));
                java.lang.reflect.Method method = reader.getClass().getMethod(METHOD_GENERATED_IMAGES);
                List<?> files = (List<?>) method.invoke(reader);

                log.debug(String.format("  RenderPuml [%s -> %s]", umlSourceFile.getName(), files.get(0)));

                // Retrieve generated file via reflection
                Object file = files.get(0);
                Class<?> generatedImageClazz = Class.forName(NET_SOURCEFORGE_PLANTUML_GENERATED_IMAGE);
                java.lang.reflect.Method getPngFileMethod = generatedImageClazz.getMethod(METHOD_GET_PNG_FILE);
                File diagramFile = (File) getPngFileMethod.invoke(file);

                compiler.log.debug("  Plant UML returned: " + diagramFile);

                return diagramFile;
            } catch (NoSuchMethodException e) {
                log.error("PlantUML: Cannot generate image", e);
            } catch (IllegalAccessException e) {
                log.error("PlantUML: Cannot generate image (invalid access)", e);
            } catch (InvocationTargetException e) {
                log.error("PlantUML: Cannot generate image (invalid target)", e);
            } catch (Exception e) {
                log.error("PlantUML: Cannot generate image (unspecified)", e);
            }
        }

        return null;
    }

    private File getTargetDiagramFile(File pumlSourceFile) {
        if (compiler.umlSvg) {
            return new File(pumlSourceFile.getAbsolutePath().replace(".puml", ".svg"));
        } else {
            return new File(pumlSourceFile.getAbsolutePath().replace(".puml", ".png"));
        }
    }

    static class Result    {
        private String diagramKey;
        private File diagramFile;
        private boolean success;

        public Result(String diagramKey, File diagramFile) {
            this.diagramKey = diagramKey;
            this.diagramFile = diagramFile;
            success = diagramFile != null;
        }

        public String getDiagramKey() {
            return diagramKey;
        }

        public File getDiagramFile() {
            return diagramFile;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    class RenderTask implements Callable<Result> {
        private String key;
        private File pumlFile;

        public RenderTask(String key, File pumlFile) {
            this.key = key;
            this.pumlFile = pumlFile;
        }

        public String getKey() {
            return key;
        }

        @Override
        public Result call() {
            log.info("[" + pumlFile + "]");
            File diagram = renderUmlDiagram(pumlFile);

            return new Result(key, diagram);
        }
    }
}
