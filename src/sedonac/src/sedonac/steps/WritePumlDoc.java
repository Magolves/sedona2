//
// Copyright (c) 2021 Oliver Wieland
// Licensed under the Academic Free License version 3.0
//
// History:
//   7 Jun 07 (its my b-day!)  Brian Frank  Creation
//

package sedonac.steps;

import sedona.util.TextUtil;
import sedona.xml.XWriter;
import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.Location;
import sedonac.ast.*;
import sedonac.namespace.Slot;
import sedonac.namespace.Type;
import sedonac.namespace.TypeUtil;
import sedonac.util.DocParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import static sedonac.util.HtmlUtil.html;
import static sedonac.util.UmlUtil.NL;

/**
 * WriteDoc generates the HTML Sedona docs for the APIs
 * if the Compiler.doc flag is set to true.
 */
public class WritePumlDoc
        extends CompilerStep {
    public static final String CLASS_DIAGRAM_ALT = "Class Diagram";
    public static final String CLASS_DIAGRAM_ICON = "hierarchy.svg";
    public static final String USER_INTERFACE_ALT = "Workbench View";
    public static final String USER_INTERFACE_ICON = "ui.svg";
    public static final String SEQUENCE_DIAGRAM_ALT = "Sequence diagram";
    public static final String SEQUENCE_DIAGRAM_ICON = "sequence-checkmark.svg";

    // Files & paths
    public static final String CSS_PATH = "../css/style.css";
    public static final String IMAGES_PATH = "../images";
    public static final String CLASS_ICON_LARGE = "icon-large";
    public static final String CLASS_ICON_SMALL = "icon-small";
    public static final String INHERITANCE = "Inheritance";
    public static final String SLOTS = "Slots";
    public static final String COMPONENT_PNG = "component.png";
    public static final String OBJECT_PNG = "object.png";
    public static final String SLOT_DETAILS = "Slot Details";
    public static final String TD_FIELD = "field";
    public static final String TD_METHOD = "method";    
    public static final String DEPENDENCIES = "Dependencies";

    private ArrayList<Type> hierarchyList;

//////////////////////////////////////////////////////////////////////////
// Construction
//////////////////////////////////////////////////////////////////////////

    public WritePumlDoc(Compiler compiler) {
        super(compiler);
    }

//////////////////////////////////////////////////////////////////////////
// Run
//////////////////////////////////////////////////////////////////////////

    public void run() {
        if (!compiler.umlDoc) return;

        if (compiler.umlOutDir == null) {
            log.error("UML base dir has not been set (step 'WritePuml' missing?)");
            return;
        }

        this.www = compiler.www;
        this.kit = compiler.ast;
        this.dir = compiler.umlOutDir;

        if (!kit.doc) return;

        hierarchyList = new ArrayList<>();

        log.info("  WritePumlDoc [" + dir + "]");

        filterTypes();
        exportIndex();
        export();
    }

//////////////////////////////////////////////////////////////////////////
// Filter Types
//////////////////////////////////////////////////////////////////////////

    private void filterTypes() {
        // filter types to doc - we only doc public non-test types
        ArrayList<TypeDef> acc = new ArrayList<>();
        for (int i = 0; i < kit.types.length; ++i) {
            TypeDef t = kit.types[i];
            if (isDoc(t)) acc.add(t);
        }
        this.types = acc.toArray(new TypeDef[0]);
        //noinspection unchecked
        Arrays.sort(types, typeCompare);
    }

//////////////////////////////////////////////////////////////////////////
// Header/Footer
//////////////////////////////////////////////////////////////////////////

    private void exportPageHeader(XWriter out, String title) {
        out.w("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"").w(NL);
        out.w(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">").w(NL);
        out.w("<html xmlns=\"http://www.w3.org/1999/xhtml\">").w(NL);
        out.w("<head>").w(NL);
        out.w("  <title>").w(title).w("</title>").w(NL);
        out.w("  <meta http-equiv='Content-type' content='text/html;charset=UTF-8' />").w(NL);
        out.w("  <link rel='stylesheet' type='text/css' href='" + CSS_PATH + "'/>").w(NL);
        out.w("</head>").w(NL);
        out.w("<body>").w(NL);
        out.w("<hr class='puml'>").w(NL);
        out.w("<a name=\"_top\">").w(NL);
    }

    private void exportKitHeader(XWriter out, KitDef kit) {
        out.w(String.format("<h1 class='title'>%s</h1>", kit.name));

        out.w(tag(String.format("Version <strong>%s (%s)</strong>", kit.version, kit.vendor), "p")).w(NL);

        if (kit.description != null) {
            out.w(tag(kit.description, "p")).w(NL);
        }
        out.w("<hr>").w(NL);
    }

    private void exportTypeHeader(XWriter out, TypeDef t) {
        out.w(String.format("<h1 class='title'>%s</h1>", t.qname()));

        if (t.doc != null) {
            out.w(tag(t.doc, "p"));
        }
        out.w("<hr>").w(NL);
    }

    private void exportFooter(XWriter out) {
        out.w("<div class='copyright'><script type='text/javascript'>document.write(\"Copyright &#169; \" + new Date().getFullYear() + \" Oliver Wieland\")</script></div>").w(NL);
        out.w("</body>").w(NL);
        out.w("</html>").w(NL);
    }

    private void exportKitNav(XWriter out, KitDef kit) {
        exportTypeOrKitNav(out, kit, null);
    }

    private void exportTypeNav(XWriter out, TypeDef t) {
        exportTypeOrKitNav(out, t.kit, t);
    }

    private void exportTypeOrKitNav(XWriter out, KitDef kit, TypeDef t) {
        out.w("<div class='flex-nav-container'>");
        out.w("  <div class='nav1'>").w(NL);
        out.w("    <a href='../index.html'>Kits</a>").w(NL);
        out.w("  </div>").w(NL);
        out.w("  <div class='nav2'>").w(NL);
        out.w("    <a href='index.html'>").w(kit.name).w("</a>").w(NL);
        out.w("  </div>").w(NL);

        if (t != null) {
            out.w("  <div class='nav3'>").w(NL);
            //out.w("    <p>").w(t.name).w("</p>").w(NL);
            out.w("    ").w(t.name).w(NL);
            out.w("  </div>").w(NL);
        }

        out.w("  <div class='spacer'>").w(NL);
        out.w("    &nbsp;").w(NL);
        out.w("  </div>").w(NL);

        out.w("  <div class='nav-logo'>").w(NL);
        // TODO: Make customizable
        out.w("    <img src='" + IMAGES_PATH + "/your_logo.svg' height='100%'/>").w(NL);
        out.w("  </div>").w(NL);

        out.w("</div> <!-- nav-container -->");
    }

    @SuppressWarnings("SameParameterValue")
    private String tag(String content, String tag) {
        return String.format("<%s>%s</%s>", tag, content, tag);
    }

//////////////////////////////////////////////////////////////////////////
// Index
//////////////////////////////////////////////////////////////////////////

    private void exportIndex() {
        File f = new File(dir, "index.html");
        try {
            XWriter out = new XWriter(f);
            exportIndex(out);
            out.close();
        } catch (Exception e) {
            throw err("Cannot write file", new Location(f), e);
        }
    }

    private void exportIndex(XWriter out) {
        exportPageHeader(out, kit.name);
        exportKitNav(out, kit);

        exportKitHeader(out, kit);

        out.w("<div class='flex-container'>");
        out.w("<nav>");
        out.w("<ul>").w(NL);

        for (TypeDef t : types) {
            out.w("  <li>");
            if (t.isaComponent()) {
                out.w("<img class='icon' src='" + IMAGES_PATH + "/" + COMPONENT_PNG + "'/>&nbsp;");
            } else {
                out.w("<img class='icon' src='" + IMAGES_PATH + "/" + OBJECT_PNG + "'/>&nbsp;");
            }
            out.w("<a href='").w(t.name).w(".html'>").w(t.name).w("</a>");
            if (t.base != null) {
                out.w("&nbsp;(");
                out.w(t.base.qname());
                out.w(")");
            }
            out.w("</li>").w(NL);
        }
        out.w("</ul>").w(NL);
        out.w("</nav>").w(NL);
        out.w("<div class='diagram'>").w(NL);
        // NOTE: You will no see the image if NoScript blocks content
        out.w(String.format("<iframe frameborder='0' width='100%%' height='100%%' src='class/%s.svg'><html><body>", kit.name)).w(NL);
        out.w(String.format("<img src='class/%s.svg'>", kit.name)).w(NL);
        out.w("</body></html><p>iframes are not supported by your browser.</p></iframe>").w(NL);
        out.w("</div>").w(NL);

        out.w("</div>").w(NL);
        exportFooter(out);
    }

//////////////////////////////////////////////////////////////////////////
// Generate
//////////////////////////////////////////////////////////////////////////

    private void export() {
        for (TypeDef type : types) export(type);
    }

    private void export(TypeDef t) {
        File f = new File(dir, t.name + ".html");
        try {
            XWriter out = new XWriter(f);
            export(t, out);
            out.close();
        } catch (Exception e) {
            throw err("Cannot write file", new Location(f), e);
        }
    }

    /**
     * Generate the documentation for this TypeDef.
     */
    void export(TypeDef t, XWriter out) {
        exportPageHeader(out, t.qname());
        exportTypeNav(out, t);

        // type details
        exportTypeHeader(out, t);

        out.w("<div class='flex-container'>");
        // inheritance
        exportInheritance(out, t);
        exportDependencies(out, t);

        File file = compiler.getDiagramFor(t);
        exportDiagramLink(out, file, CLASS_DIAGRAM_ICON, CLASS_DIAGRAM_ALT);
        file = compiler.getUiDiagramFor(t);
        exportDiagramLink(out, file, USER_INTERFACE_ICON, USER_INTERFACE_ALT);

        out.w("</div>"); // row
        out.w(NL);
        out.w(NL);

        // short slot list (each slot links to it's details section)
        exportSlotList(out, t);
        // slot details
        out.w("<hr/>").w(NL);
        out.w("<h2>" + SLOT_DETAILS + "</h2>");
        exportSlotDetails(out, t);

        exportFooter(out);
    }

    private void exportSlotDetails(XWriter out, TypeDef t) {
        SlotDef[] slots = t.slotDefs();
        StringBuilder sb = new StringBuilder();
        MethodDef m = null;

        //noinspection unchecked
        Arrays.sort(slots, slotCompare);

        for (SlotDef slot : slots) {
            if (!isDoc(slot)) continue;


            sb.append("<tr>").append(NL);

            // Print name of method - except if cstr, print type name instead of _iInit
            String sname = slot.name;
            if (slot.isMethod()) {
                m = (MethodDef) slot;
                if (m.isInstanceInit()) sname = m.parent.name();
            }

            String prefix = TD_FIELD;
            if (slot.isMethod()) {
                prefix = TD_METHOD;
            }

            sb.append(String.format("<td class='%s'>", prefix)).append(sname);

            sb.append(String.format("<a name='%s'/>", slot.name));
            sb.append(String.format("<td class='%s-desc'>", prefix)).append(NL);

            sb.append("<p class='sig'><code>");
            sb.append(makeSlotSignature(slot));
            sb.append("</code>").append(NL);


            // If any facets, print them last
            if ((slot.facets() != null) && (!slot.facets().isEmpty()))
                sb.append(" ").append(html(slot.facets().toString()));

            // Overrides
            if (m != null) {
                sb.append(makeSequenceDiagramLink(t, m));
                if (slot.isOverride()) {
                    sb.append("</p><p>Overrides ");
                    sb.append(makeOverrideLink(t, m));
                    sb.append("</p>");
                }
            }

            if (slot.doc != null) {
                sb.append(makeDoc(slot.doc));
            }

            sb.append("<td>").append(NL);
            sb.append("</tr>").append(NL);
        }

        if (sb.length() > 0) {
            out.w("<table border='0' class='slot-details'>").w(NL);
            out.w("  <tbody>").w(NL);
            out.w("  <th>Slot</th><th>Description</th>").w(NL);
            out.w("  <tbody>").w(NL);
            out.w(sb.toString());
            out.w("  </tbody>").w(NL);
            out.w("</table>").w(NL);
        }
    }

    /**
     * Exports a brief slot list (only names) of the given type.
     *
     * @param out the writer
     * @param t   the type
     */
    private void exportSlotList(XWriter out, TypeDef t) {
        SlotDef[] slots = t.slotDefs();
        out.w("<hr/>").w(NL);
        //noinspection unchecked
        Arrays.sort(slots, slotCompare);

        // short refs
        out.w("<h2>" + SLOTS + "</h2>");
        out.w("<h3>Fields</h3>");

        int exported = 0;
        for (SlotDef slot : slots) {
            if (!isDoc(slot) || slot.isMethod()) continue;

            if (exported > 0) {
                out.w("&#124;").w(NL);
            }

            exportSlotShortRef(slot, out);
            exported++;
        }

        out.w("<h3>Methods</h3>");
        exported = 0;
        for (SlotDef slot : slots) {
            if (!isDoc(slot) || slot.isField()) continue;

            if (exported > 0) {
                out.w("&#124;").w(NL);
            }

            exportSlotShortRef(slot, out);
            if (slot.isMethod()) {
                MethodDef m = (MethodDef) slot;
                exportSequenceDiagramLink(out, t, m);
            }

            exported++;
        }
    }

    private void exportInheritance(XWriter out, TypeDef t) {
        out.w("<div class='column-left inheritance'>");
        out.w("<h3>" + INHERITANCE + "</h3>").w(NL);

        hierarchyList.clear();
        hierarchyList.add(t);

        Type base = t.base;
        while (base != null) {
            hierarchyList.add(0, base);
            base = base.base();
        }
        int spaces = 0;
        out.w("<ul>");
        for (Type type : hierarchyList) {
            out.w(TextUtil.getSpaces(spaces)).w("<li>");
            exportTypeLink(type, out, false);
            out.w(TextUtil.getSpaces(spaces)).w("</li>").w(NL);
            spaces += 2;
        }
        out.w("</ul> <!-- inheritance -->").w(NL);
        out.w("</div>").w(NL); // inheritance column
    }

    private void exportDependencies(XWriter out, TypeDef t) {
        out.w("<div class='column-left'>");
        out.w("<h3>" + DEPENDENCIES + "</h3>").w(NL);

        HashSet<Type> dependencies = new HashSet<>();

        for (Slot slot : t.slotDefs()) {
            if (slot instanceof FieldDef) {
                FieldDef fieldDef = (FieldDef) slot;

                Type depType = fieldDef.type;
                if (fieldDef.type.isArray()) {
                    depType = fieldDef.type.arrayOf();
                }

                if (!depType.isPrimitive()) {
                    dependencies.add(depType);
                }
            }
        }

        if (dependencies.size() > 0) {
            out.w("<ul>").w(NL);
            for (Type depType : dependencies) {
                out.w(String.format("  <li>%s</li>", makeTypeLink(depType, false))).w(NL);
            }

            out.w("</ul> <!-- dependencies -->").w(NL);
        }
        out.w("</div>").w(NL); // inheritance column
    }

    private void exportDiagramLink(XWriter out, final File file, final String linkIcon, final String text) {
        if (file != null) {
            out.w("<div>");
            out.w(String.format("<h3>%s</h3>", text));
            out.w(String.format("<a href=\"%s/%s\"\" alt=\"%s\" class='" + WritePumlDoc.CLASS_ICON_LARGE + "'><img src='" + IMAGES_PATH + "/%s' width='100' height='auto'></a>",
                    file.getParentFile().getName(),
                    file.getName(), text,
                    linkIcon));
            out.w("</div>");
            out.w(NL);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void exportSequenceDiagramLink(XWriter out, final File file, final String text) {
        if (file != null) {
            out.w(String.format("<!-- '%s' -->", file.getAbsolutePath()));

            out.w(String.format("<a href=\"%s/%s/%s\"\" alt=\"%s\" target=\"_blank\" class='" + WritePumlDoc.CLASS_ICON_SMALL + "'><img src='" + IMAGES_PATH + "/%s'/></a>",
                    file.getParentFile().getParentFile().getName(),
                    file.getParentFile().getName(),
                    file.getName(), text,
                    WritePumlDoc.SEQUENCE_DIAGRAM_ICON));
            out.w(NL);
        }
    }

    /**
     * Generate the documentation for this SlotDef.
     */
    void exportSlotShortRef(SlotDef slot, XWriter out) {
        out.w(String.format("<a href='#%s'>%s</a>&nbsp;", slot.name, slot.name)).w(NL);
    }

    void exportSequenceDiagramLink(XWriter out, Type t, MethodDef methodDef) {
        File file = compiler.getDiagramFor(t, methodDef);
        exportSequenceDiagramLink(out, file, SEQUENCE_DIAGRAM_ALT);
    }


    void exportTypeLink(Type t, XWriter out, boolean shorten) {
        if (t.isPrimitive()) {
            out.w(t);
        } else if (t.isArray()) {
            exportTypeLink(t.arrayOf(), out, shorten);
            out.w("[]");
        } else if (!t.isPublic()) {
            exportTypeLink(t.base(), out, shorten);
        } else {
            String name = shorten ? t.name() : t.qname();
            String href = "../" + t.kit().name() + "/" + t.name() + ".html";
            out.w("<span><a href='").w(href).w("'>").w(name).w("</a></span>");
        }
    }

    //////////////////////////////////////////////////////////////////////////
// String-related methods
//////////////////////////////////////////////////////////////////////////
    String makeSlotSignature(SlotDef slot) {
        StringBuilder sb = new StringBuilder();

        if (slot.isField()) {
            FieldDef f = (FieldDef) slot;
            sb.append(makeSlotModifiers(slot, null));

            sb.append(makeTypeLink(f.type()));
            sb.append(" ").append(f.name());

            if (f.init != null) {
                sb.append(" = ").append(f.init);
            }
        } else {
            MethodDef m = (MethodDef) slot;
            sb.append(makeSlotModifiers(slot, null));

            sb.append(makeTypeLink(m.returnType()));

            // Print name of method - if cstr, print type name instead of _iInit
            String mname = m.name();
            if (m.isInstanceInit()) mname = m.parent.name();
            sb.append(" ").append(mname).append("(");

            for (int i = 0; i < m.params.length; i++) {
                if (i > 0) sb.append(", ");

                sb.append(makeTypeLink(m.params[i].type));
                sb.append(" ").append(m.params[i].name);
            }
            sb.append(")");

        }

        sb.append(NL);

        return sb.toString();
    }

    String makeSequenceDiagramLink(Type t, MethodDef methodDef) {
        File file = compiler.getDiagramFor(t, methodDef);
        return makeSequenceDiagramLink(file, SEQUENCE_DIAGRAM_ALT);
    }

    @SuppressWarnings("SameParameterValue")
    private String makeSequenceDiagramLink(final File file, final String text) {
        if (file != null) {

            return String.format("<!-- '%s' -->", file.getAbsolutePath()) +
                    String.format("<a href=\"%s/%s/%s\"\" alt=\"%s\" target=\"_blank\" class='" + WritePumlDoc.CLASS_ICON_SMALL + "'><img src='" + IMAGES_PATH + "/%s'/></a>",
                            file.getParentFile().getParentFile().getName(),
                            file.getParentFile().getName(),
                            file.getName(), text,
                            WritePumlDoc.SEQUENCE_DIAGRAM_ICON) +
                    NL;
        } else {
            return "";
        }
    }

    /**
     * Gets the type declaration as string.
     *
     * @param t the type
     * @return the type declaration string, e. g. 'public abstract class Foo extends Bar'
     */
    String makeTypeSignature(TypeDef t) {
        StringBuilder sb = new StringBuilder();

        sb.append("<").append("code").append(">");
        if (t.isPublic()) sb.append("public ");
        if (t.isInternal()) sb.append("internal ");
        if (t.isAbstract()) sb.append("<span class='abstract'>abstract</span> ");
        if (t.isConst()) sb.append("const ");
        if (t.isFinal()) sb.append("final ");

        sb.append(t.name);
        if (t.base != null) {
            sb.append(" extends ").append(t.base.name());
        }

        sb.append("</").append("code").append(">");

        return sb.toString();
    }

    /**
     * Print all modifiers associated with this slot
     */
    @SuppressWarnings("SameParameterValue")
    String makeSlotModifiers(SlotDef slot, String htag) {
        StringBuilder sb = new StringBuilder();

        if ((htag != null) && (htag.length() > 0)) sb.append("<").append(htag).append(">");
        if (slot.isPublic()) sb.append("public ");
        if (slot.isProtected()) sb.append("protected ");
        if (slot.isPrivate()) sb.append("private ");
        if (slot.isInternal()) sb.append("internal ");
        if (slot.isStatic()) sb.append("<span class='static'>static</span> ");
        if (slot.isAbstract()) sb.append("<span class='abstract'>abstract</span> ");       // abstract implies virtual
        else if (slot.isAction()) sb.append("<span class='reflective'>action</span> ");    // action implies virtual
        else if (slot.isVirtual()) sb.append("<span class='virtual'>virtual</span> ");
        if (slot.isNative()) sb.append("<span class='native'>native</span> ");
        if (slot.isOverride()) sb.append("<span class='virtual'>override</span> ");
        if (slot.isConst()) sb.append("<span class='const'>const</span> ");
        if (slot.isDefine()) sb.append("<span class='const'>define</span> ");
        if (slot.isInline()) sb.append("<span class='inline'>inline</span> ");
        if (slot.isProperty()) sb.append("<span class='reflective'>property</span> ");
        if ((htag != null) && (htag.length() > 0)) sb.append("</").append(htag).append(">");

        return sb.toString();
    }

    /**
     * Parse the text into a DocNode array and write out
     * as HTMl markup.
     */
    String makeDoc(String doc) {
        StringBuilder sb = new StringBuilder();

        DocParser.DocNode[] nodes = new DocParser(doc).parse();
        for (DocParser.DocNode node : nodes) {
            int id = node.id();
            String text = node.text;
            switch (id) {
                case DocParser.DocNode.PARA:
                    sb.append("<p>").append(html(text)).append("</p>").append(NL);
                    break;
                case DocParser.DocNode.PRE:
                    sb.append("<pre class='doc'>").append(html(text)).append("</pre>").append(NL);
                    break;
                default:
                    throw new IllegalStateException("Unknown DocNode id: " + id);
            }
        }

        return sb.toString();
    }

    /**
     * Gets the relative path of the type source file within the kit, e. g. 'test/TestFoo.sedona'.
     *
     * @param t the type to get the relative path for.,
     * @return the relative source file path
     */
    private String relPathWithinKit(TypeDef t) {
        File kitFile = new File(t.kit.loc.file);
        File kitDir = kitFile.getParentFile();
        File sourceFile = new File(t.loc.file);

        return sourceFile.getAbsolutePath().substring(kitDir.getAbsolutePath().length() + 1).replace("\\", "/");
    }

    String makeTypeLink(Type t) {
        return makeTypeLink(t, true);
    }

    String makeTypeLink(Type t, boolean shorten) {
        StringBuilder sb = new StringBuilder();

        if (t.isPrimitive()) {
            sb.append(t);
        } else if (t.isArray()) {
            makeTypeLink(t.arrayOf(), shorten);
            sb.append("[]");
        } else if (!t.isPublic()) {
            makeTypeLink(t.base(), shorten);
        } else {
            String name = shorten ? t.name() : t.qname();
            String href = "../" + t.kit().name() + "/" + t.name() + ".html";
            sb.append("<span><a href='").append(href).append("'>").append(name).append("</a></span>");
        }

        return sb.toString();
    }

    /**
     * Makes a HTML link to an overridden method. This method throws a runtime exception, if given type
     * has no base class.
     *
     * @param t the type overriding the method
     * @param m the overridden method
     * @return the HTML link
     */
    String makeOverrideLink(Type t, MethodDef m) {
        if (t.base() == null) {
            throw new RuntimeException("makeOverrideLink: base class is null");
        }

        Type base = t.base();

        Slot s = base.slot(m.name);
        if (s != null) {
            return String.format("<a href='../%s/%s.html#%s'>%s.%s</a>",
                    base.kit().name(),
                    base.name(),
                    m.name,
                    base.qname(),
                    m.name
            );
        } else {
            return makeOverrideLink(base, m);
        }
    }

//////////////////////////////////////////////////////////////////////////
// Utils
//////////////////////////////////////////////////////////////////////////

    boolean isDoc(TypeDef t) {
        if (!t.isPublic()) return !compiler.umlOnlyPublic;
        //noinspection RedundantIfStatement
        if (TypeUtil.isTestOnly(t)) return false;
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isDoc(SlotDef slot) {
        if (slot.isPrivate()) return !compiler.umlOnlyPublic;
        if (slot.isInternal()) return !compiler.umlOnlyPublic;
        //noinspection RedundantIfStatement
        if (slot.synthetic) return false;
        return true;
    }

    //////////////////////////////////////////////////////////////////////////
// Comparators
//////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("rawtypes")
    static class TypeComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            TypeDef a = (TypeDef) o1;
            TypeDef b = (TypeDef) o2;
            return a.name.compareTo(b.name);
        }
    }

    static TypeComparator typeCompare = new TypeComparator();

    @SuppressWarnings("rawtypes")
    static class SlotComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            SlotDef a = (SlotDef) o1;
            SlotDef b = (SlotDef) o2;

            // If types are the same, order by name string as usual
            final boolean bothMethods = (a instanceof MethodDef) && (b instanceof MethodDef);
            final boolean bothFields = (a instanceof FieldDef) && (b instanceof FieldDef);
            if (bothFields || bothMethods) {
                if (bothMethods) {
                    int c = Boolean.compare(b.isOverride(), b.isOverride());
                    if (c == 0) {
                        return a.name.compareTo(b.name);
                    }
                } else {
                    return a.name.compareTo(b.name);
                }
            }

            // If types are different, FieldDef comes first
            if (a instanceof FieldDef) return -1;
            return 1;

            //return a.name.compareTo(b.name);
        }
    }

    static SlotComparator slotCompare = new SlotComparator();

//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

    KitDef kit;
    File dir;
    TypeDef[] types;
    boolean www;
}
