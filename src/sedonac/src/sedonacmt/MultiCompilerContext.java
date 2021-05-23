package sedonacmt;

import sedona.util.Log;
import sedonac.CompilerLog;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Offers a context object for multi-compiler steps and offers a common subset
 * of Sedona compiler options.
 */
public class MultiCompilerContext {
    public CompilerLog log;          // env -v
    public boolean doc;              // env -doc

    public File outDir;              // env -outDir
    // UML flags
    public boolean umlDoc = false;             // env -udoc
    // Parsed kits
    public List<KitProxy> kits;

    public MultiCompilerContext(String[] args, List<KitProxy> kits) {
        log = new CompilerLog();

        kits.sort(Comparator.comparing(KitProxy::kitName));

        this.kits = Collections.unmodifiableList(kits);

        initFromArgs(args);
    }

    private void initFromArgs(String[] args) {
        // process args
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            switch (arg) {
                case "-doc":
                    doc = true;
                    break;
                case "-outDir":
                    if (i + 1 >= args.length)
                        log.error("Missing outDir option");
                    else
                        outDir = new File(args[++i]);
                    break;
                case "-v":
                    log.severity = Log.DEBUG;
                    break;
                // UML options
                case "-udoc":
                    umlDoc = true;
                    break;
            }

        }
    }
}

