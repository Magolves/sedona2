package sedonacmt;

import sedona.xml.XElem;
import sedona.xml.XParser;
import sedonac.CompilerException;
import sedonac.Location;
import sedonac.ast.DependDef;
import sedonac.ast.KitDef;

import java.io.File;
import java.util.List;

public class KitProxy {
    public static final String SYS_KIT = "sys";
    private final XElem xml;
    private File kitFile;
    private KitDef kitDef;
    private boolean containsErrors = false;

    /**
     * Creates a new Kit proxy instance.
     *
     * @param kitFile the kit file representing the Sedona kit.
     * @throws Exception IO error
     */
    public KitProxy(File kitFile) throws Exception {
        this.kitFile = kitFile;
        // must go in ctor because it's final
        this.xml = XParser.make(kitFile).parse();

        parseKit();
    }

    public File getKitFile() {
        return kitFile;
    }

    public KitDef getKitDef() {
        return kitDef;
    }

    /**
     * Convenience for <code>kitDef.name</code>.
     *
     * @return kit name
     */
    public String kitName() {
        return kitDef != null ? kitDef.name : null;
    }

    public boolean containsErrors() {
        return containsErrors;
    }

    public void setContainsErrors(boolean containsErrors) {
        this.containsErrors = containsErrors;
    }

    /**
     * Convenience for <code>kitDef.depends</code>.
     *
     * @return kit dependencies
     */
    public DependDef[] kitDependencies() {
        return kitDef != null ? kitDef.depends : null;
    }

    /**
     * Checks if kit has any dependencies (this is only false for kit <code>sys</code>).
     *
     * @return true, if kit has dependencies
     */
    public boolean hasDependencies() {
        return kitDef != null && (kitDef.depends != null && kitDef.depends.length > 0);
    }

    /**
     * Checks if this kit depends on the given kit.
     *
     * @param kitProxy the kit to check
     * @return true, if this kit depends on the kit
     */
    public boolean dependsOn(KitProxy kitProxy) {
        return dependsOn(kitProxy.kitName());
    }

    /**
     * Checks if this kit depends on the given kit.
     *
     * @param kitProxies the kits to check
     * @return true, if this kit depends on the kit
     */
    public boolean dependsOnAny(List<KitProxy> kitProxies) {
        for (KitProxy kitProxy : kitProxies) {
            if (dependsOn(kitProxy.kitName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this kit depends on the given kit name.
     *
     * @param kitName the kit to check
     * @return true, if this kit depends on the kit
     */
    public boolean dependsOn(String kitName) {
        for (int i = 0; i < kitDependencies().length; i++) {
            DependDef dependDef = kitDependencies()[i];
            if (kitName.equals(dependDef.depend.name())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compares the two kits according to their dependencies.
     *
     * @param kit1 the first kit
     * @param kit2 the second kit
     * @return > 0 if kit1 depends on k2; < 0 : if kit2 depends on kit 1; 0: otherwise
     */
    public static int compareByDependency(KitProxy kit1, KitProxy kit2) {
        if (kit1.dependsOn(kit2)) {
            return 1;
        } else if (kit2.dependsOn(kit1)) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Parse the Sedona kit but considers only the nodes required to setup the
     * compilation sequence (name and dependent kits).
     */
    private void parseKit() {
        kitDef = new KitDef(new Location(kitFile));

        kitDef.name = xml.get("name");
        kitDef.vendor = xml.get("vendor");
        // kit.version fails ???
        kitDef.description = xml.get("description");

        parseDepends(kitDef, xml.elems("depend"));
    }

    /**
     * Parses the dependency section of<code>kit.xml</code>.
     *
     * @param kit      the kit definition
     * @param xdepends the XML section containing the kit dependencies
     */
    private void parseDepends(KitDef kit, XElem[] xdepends) {
        // depend elements
        kit.depends = new DependDef[xdepends.length];
        for (int i = 0; i < xdepends.length; ++i) {
            XElem x = xdepends[i];
            kit.depends[i] = new DependDef(new Location(x), x.getDepend("on"));
        }

        // check depend on sys
        boolean onSys = kit.name.equals("sys");
        for (int i = 0; i < kit.depends.length; ++i) {
            if (kit.depends[i].depend.name().equals(SYS_KIT)) {
                onSys = true;
                break;
            }
        }

        if (!onSys) throw new CompilerException("Must declare dependency on 'sys'", new Location(xml));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Kit ");
        sb.append(kitName());

        if (hasDependencies()) {
            sb.append(" {");
            for (int i = 0; i < kitDef.depends.length; i++) {
                DependDef dependDef = kitDef.depends[i];
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(dependDef.depend.name());
            }
            sb.append('}');
        }
        return sb.toString();
    }
}
