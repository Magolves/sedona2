package sedonacmt;

/**
 * Base class for steps of the multi-kit compiler.
 */
public abstract class MultiCompilerStep {
    protected MultiCompilerContext context;

    public MultiCompilerStep(MultiCompilerContext context) {
        this.context = context;
    }

    public final void run() throws Exception {
        context.log.setLogName(getClass().getName());
        doRun();
        context.log.setLogName(null);
    }

    abstract void doRun() throws Exception;

    @Override
    public String toString() {
        return getClass().getName();
    }
}
