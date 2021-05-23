package sedonac.steps;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.ast.*;
import sedonac.ir.IrField;
import sedonac.ir.IrMethod;
import sedonac.namespace.Method;
import sedonac.namespace.Slot;
import sedonac.namespace.Type;
import sedonac.namespace.TypeUtil;
import sedonac.util.FileUtil;
import sedonac.util.SedonaUnits;
import sedonac.util.UmlUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static sedonac.util.Env.SEDONA_HOME;


/**
 * Generates class and sequence diagrams for all (reflective) types in the kit using <a href="https://plantuml.com">PlantUML</a>.
 *
 * <p><b>NOTE:</b> You may need to increase the image size via environment variable <tt>PLANTUML_LIMIT_SIZE</tt>.
 * See also <a href="https://plantuml.com/de/faq">Plant UML FAQ</a>
 */
public class WritePuml extends CompilerStep {
    public static final String UML_DIRECTORY_NAME = "uml";
    private static final String SEQ_DIRECTORY_NAME = "sequence";
    private static final String CLS_DIRECTORY_NAME = "class";
    private static final String UI_DIRECTORY_NAME = "ui";

    private static final String CLASS_DIAGRAM_PREFIX = "CD_";
    private static final String UI_DIAGRAM_PREFIX = "UI_";
    private static final String SEQUENCE_DIAGRAM_PREFIX = "SD_";

    private static final String NATIVE_ACTIVE_COLOR = "#005691";


    public static final String[] IGNORED_FILES = new String[]{"(\\w+)Offsets.sedona"};

    private File classBaseDir;
    private File uiBaseDir;
    private File sequenceBaseDir;

    // Commands for class and sequence diagram (salt will crash, if these lines are added!)
    private static final String[] PUML_COMMON_COMMANDS = new String[]{
            "skinparam defaultFontName Verdana",
            "skinparam defaultFontSize 12",
            "skinparam roundcorner 5",
            "' Note styles",
            "skinparam note {",
            "    BorderColor #525f6b",
            "    BackgroundColor #feebc5",
            "    FontColor #293036",
            "    FontSize 10",
            "}"

    };

    private static final String[] PUML_SEQUENCE_DIAGRAM_COMMANDS = new String[]{
            "' Sequence diagram styles ",
            "skinparam sequence {",
            "    ArrowColor #004768",
            "    LifeLineBorderColor #004768",
            "    LifeLineBackgroundColor #bfd5e3",
            "    ParticipantBorderColor #606061",
            "    ParticipantBackgroundColor #dfdfe0",
            "    ActorBorderColor #606061",
            "    ActorBackgroundColor #dfdfe0",
            "    EntityBorderColor #606061",
            "    EntityBackgroundColor dfdfe0",
            "    BoxBorderColor #606061",
            "}",
    };

    private static final String[] PUML_CLASS_DIAGRAM_COMMANDS = new String[]{
            "' Class diagram styles ",
            "left to right direction",
            "' Class stereotype",
            "skinparam stereotypeCBackgroundColor #bfd5e3",
            "' Interface stereotype",
            "skinparam stereotypeIBackgroundColor #7fd3d7",
            "' Class appearance",
            "skinparam class {",
            "    BackgroundColor #dfdfe0",
            "    BorderColor #004768",
            "    ArrowColor #004768",
            "}",
    };

    /**
     * Lambda interface to filter slots
     */
    interface FieldSelector {
        boolean match(Slot field);

    }

    /**
     * Checks if slot is a field definition.
     */
    FieldSelector isFieldDef = (x) -> x instanceof FieldDef;
    FieldSelector isMethod = (x) -> x instanceof Method;
    FieldSelector isMethodDef = (x) -> x instanceof MethodDef;

    /**
     * Checks if slot is visible in diagram (slot is either public or -uprv was set
     */
    FieldSelector isVisible = (x) -> x.isPublic() && !"meta".equals(x.name()) || !compiler.umlOnlyPublic;

    /**
     * Checks if given slot represents either an instance or a static init (ctor).
     */
    FieldSelector isInit = (x) -> isMethod.match(x) && ((Method) x).isInstanceInit() || ((Method) x).isStaticInit();

    FieldSelector isProperty = (x) -> isVisible.match(x) && isFieldDef.match(x) && x.isProperty() && !x.isAction();
    FieldSelector isAction = (x) -> isVisible.match(x) && x.isMethod() && x.isAction() && !isInit.match(x);
    FieldSelector isInstanceMethod = (x) -> isVisible.match(x) && x.isMethod() && !x.isAction() && !isInit.match(x) && !x.isStatic();
    FieldSelector isClassMethod = (x) -> isVisible.match(x) && x.isMethod() && !x.isAction() && !isInit.match(x) && x.isStatic();
    FieldSelector isInstanceField = (x) -> isVisible.match(x) && isFieldDef.match(x) && !x.isProperty() && !x.isStatic();
    FieldSelector isClassField = (x) -> isVisible.match(x) && isFieldDef.match(x) && !x.isProperty() && x.isStatic();


    /**
     * Constructor
     *
     * @param compiler compiler instance
     */
    public WritePuml(Compiler compiler) {
        super(compiler);

        setupDirectories(compiler);
    }

    @Override
    public void run() {
        if (!compiler.umlDoc) return;

        try {
            exportDiagrams();

            if (compiler.umlRenderWithPlantUml) {
                compiler.log.info(String.format("  WritePuml [%d diagrams]", compiler.umlMap.size()));
            }
        } catch (IOException e) {
            log.error("UML file generation failed", e);
        }
    }

    /**
     * Sets up directory structure for UML diagrams. If the output directory was passed via command line,
     * the subdirectories are created i that particular directory. Otherwise the current working directory
     * is used as output.
     * <p>
     * The structure is as follows: <tt>[output dir]/uml/[kit name]/{class|sequence|ui}</tt>.
     *
     * @param compiler the compiler instance
     */
    private void setupDirectories(Compiler compiler) {
        final String umlSubDir = UML_DIRECTORY_NAME +
                File.separator + compiler.ast.name;

        File umlBaseDirectory;
        if (compiler.outDir != null) {
            umlBaseDirectory = new File(compiler.outDir + File.separator +
                    umlSubDir);
        } else {
            String sedona_home = System.getenv(SEDONA_HOME);
            if (sedona_home != null && (new File(sedona_home).exists())) {
                umlBaseDirectory = new File(sedona_home + File.separator + "doc" + File.separator + umlSubDir);
                log.debug("  SEDONA_HOME present, using " + umlBaseDirectory);
            } else {
                umlBaseDirectory = new File(umlSubDir);
            }
        }
        makeDirectory(umlBaseDirectory);

        classBaseDir = new File(umlBaseDirectory, CLS_DIRECTORY_NAME);
        makeDirectory(classBaseDir);
        sequenceBaseDir = new File(umlBaseDirectory, SEQ_DIRECTORY_NAME);
        makeDirectory(sequenceBaseDir);
        uiBaseDir = new File(umlBaseDirectory, UI_DIRECTORY_NAME);
        makeDirectory(uiBaseDir);

        compiler.umlOutDir = umlBaseDirectory;
        compiler.log.debug(String.format("  UML output directory is '%s'", umlBaseDirectory));
    }

    /**
     * Creates the desired directory (including required parents) and logs it.
     *
     * @param directory the directory to create.
     */
    private void makeDirectory(File directory) {
        if (!directory.exists()) {
            compiler.log.debug("Create directory " + directory.getAbsolutePath());
            if (!directory.mkdirs()) {
                compiler.log.error("Create directory " + directory.getAbsolutePath() + " failed");
            }
        }
    }

    /**
     * Exports diagram(s) for this kit according to the -usplit flag.
     *
     * @throws IOException IO error
     */
    private void exportDiagrams() throws IOException {
        File overviewFile = new File(classBaseDir, compiler.ast.name + ".puml");

        // the overview file is currently always remade

        try (FileWriter overviewWriter = new FileWriter(overviewFile)) {
            writeUmlHeader(overviewWriter, compiler.ast.name);

            List<Type> types = filterTypes();
            for (Type type : types) {
                if (compiler.umlSplitClsDgms) {
                    // Add type to overview, but omit slots
                    writeTypeDef(overviewWriter, type, false);
                    overviewWriter.flush();
                    // Dedicated class diagram for type
                    exportClassDiagram(type);
                } else {
                    // Single CD for kit
                    writeTypeDef(overviewWriter, type, true);
                }

                exportSequenceDiagrams(type);
                exportUserInterface(type);
            }
            writeUmlFooter(overviewWriter);
        }

        compiler.storeDiagram(compiler.ast, overviewFile);

        compiler.log.info(String.format("  WriteUml [%s]", overviewFile.getAbsolutePath()));
    }

    private List<Type> filterTypes() {
        Type[] types;
        List<Type> filteredTypes = new ArrayList<>();

        if (compiler.umlAllTypes) {
            types = compiler.ast.types;
        } else {
            types = compiler.ast.reflectiveTypes;
        }

        for (String ignoredFile : IGNORED_FILES) {
            log.info(String.format("  WritePuml [Filter pattern '%s']", ignoredFile));
        }

        for (Type type : types) {
            if (type.isPrimitive()) {
                continue;
            }

            if (TypeUtil.isTestOnly(type)) {
                log.info(String.format("  WritePuml [Ignore test %s]", type.qname()));
                continue;
            }

            if (matchesIgnorePattern(type)) {
                log.info(String.format("  WritePuml [Ignore type %s (matches file pattern)]", type.qname()));
                continue;
            }

            filteredTypes.add(type);
        }
        return filteredTypes;
    }

    /**
     * Exports a dedicated class diagram for this type.
     *
     * @param type the type to export
     * @throws IOException IO error
     */
    private void exportClassDiagram(Type type) throws IOException {
        File classFile = new File(classBaseDir, CLASS_DIAGRAM_PREFIX + type.name() + ".puml");

        boolean isUpToDate = isTypeUpToDate(type, classFile);

        if (!isUpToDate) {
            try (FileWriter typeWriter = new FileWriter(classFile)) {
                // plantUML cannot handle dots in diagram name
                writeUmlHeader(typeWriter, CLASS_DIAGRAM_PREFIX + type.name());

                writeTitle(typeWriter, type.name());
                writeTypeDef(typeWriter, type, true);
                writeUmlFooter(typeWriter);
            }
        } else {
            log.debug(String.format(" Up-to-date: %s", classFile));
        }
        compiler.storeDiagram(type, classFile);
    }

    /**
     * Exports a mocked UI (via salt) for this type.
     *
     * @param type the type to export
     * @throws IOException IO error
     */
    private void exportUserInterface(Type type) throws IOException {
        //Skip if component contains no properties (beyond the meta slot) and no action
        if (countSlots(type, isProperty) <= 1 && countSlots(type, isAction) == 0) return;

        File uiFile = new File(uiBaseDir, UI_DIAGRAM_PREFIX + type.name() + ".puml");

        boolean isUpToDate = isTypeUpToDate(type, uiFile);

        if (!isUpToDate) {
            try (FileWriter uiTypeWriter = new FileWriter(uiFile)) {
                // plantUML cannot handle dots in diagram name
                writeUmlHeader(uiTypeWriter, UI_DIAGRAM_PREFIX + type.name());
                writeSalt(uiTypeWriter);
                writeUserInterface(uiTypeWriter, type);
                writeUmlFooter(uiTypeWriter);
            }
        } else {
            log.debug(String.format(" Up-to-date: %s", uiFile));
        }

        compiler.storeUiDiagram(type, uiFile);
    }

    /**
     * Inserts a title statement to the given diagram.
     *
     * @param out   the writer
     * @param title the title of the diagram
     * @throws IOException IO error
     */
    private void writeTitle(Writer out, final String title) throws IOException {
        out.write("title ");
        out.write(title);
        out.write(UmlUtil.NL);
    }

    /**
     * Inserts a salt statement to the given (UI) diagram.
     *
     * @param out the writer
     * @throws IOException IO error
     */
    private void writeSalt(Writer out) throws IOException {
        out.write("salt");
        out.write(UmlUtil.NL);
    }

    /**
     * Exports dedicated sequence diagrams for all methods of the given type.
     *
     * @param type the type to export
     * @throws IOException IO error
     */
    private void exportSequenceDiagrams(Type type) throws IOException {
        for (Slot slot : type.slots()) {
            if (!isMethodDef.match(slot)) {
                continue;
            }

            // skip abstract methods and c'tors
            if (slot.isAbstract() || isInit.match(slot)) {
                continue;
            }

            MethodDef methodDef = (MethodDef) slot;

            // Do we need a diagram at all?
            if (!hasSequenceStatements(methodDef.code)) {
                continue; // diagram would be empty -> stop
            }

            // We create a file for every method and a directory for every type
            final String diagramName = SEQUENCE_DIAGRAM_PREFIX + type.name() + "_" + slot.name();
            File seqDir = new File(sequenceBaseDir, type.name());
            makeDirectory(seqDir);

            File seqFile = new File(seqDir, diagramName + ".puml");

            boolean isUpToDate = isTypeUpToDate(type, seqFile);

            if (!isUpToDate) {
                CallStack visited = new CallStack();

                try (FileWriter seqWriter = new FileWriter(seqFile)) {
                    // plantUML cannot handle dots in diagram name
                    writeUmlHeader(seqWriter, diagramName);
                    writeLines(seqWriter, PUML_COMMON_COMMANDS);
                    writeLines(seqWriter, PUML_SEQUENCE_DIAGRAM_COMMANDS);
                    writeTitle(seqWriter, slot.qname());

                    changeActivation(seqWriter, null, type, true, false);
                    writeSequence(seqWriter, type, methodDef, visited);
                    changeActivation(seqWriter, null, type, false, false);

                    writeUmlFooter(seqWriter);
                }
            } else {
                log.debug(String.format(" Up-to-date: %s", seqFile));
            }

            compiler.storeDiagram(type, methodDef, seqFile);
        }
    }

    /**
     * Writes the calling sequence of the given method.
     *
     * @param out     the writer
     * @param owner   the owning (parent) type of the method
     * @param method  the method to write
     * @param visited the map containing visited methods
     * @throws IOException IO error
     */
    private void writeSequence(Writer out, Type owner, MethodDef method, CallStack visited) throws IOException {
        CallStack newStack = new CallStack(visited);
        newStack.add(method);

        writeSequence(out, owner, method.code, newStack);
    }

    /**
     * Writes the calling sequence of the given method.
     *
     * @param out     the writer
     * @param owner   the owning (parent) type of the method
     * @param block   the statement block
     * @param visited the map containing visited methods
     * @throws IOException IO error
     */
    private void writeSequence(Writer out, Type owner, Block block, CallStack visited) throws IOException {
        if (block == null) {
            return;
        }

        for (Stmt statement : block.stmts()) {
            if (statement.id == Stmt.EXPR_STMT) {
                Stmt.ExprStmt exprStmt = (Stmt.ExprStmt) statement;
                if (exprStmt.expr.id == Expr.CALL) {
                    Expr.Call call = (Expr.Call) exprStmt.expr;

                    if (call.target == null) {
                        // static call on current class
                        writeCall(out, owner, owner, call, visited);
                    } else if (call.target.type == null) {
                        // instance call on this
                        if (call.target.id == Expr.SUPER) {
                            writeCall(out, owner, owner.base(), call, visited);
                        } else {
                            writeCall(out, owner, owner, call, visited);
                        }
                    } else {
                        // instance call on other object
                        writeCall(out, owner, call.target.type, call, visited);
                    }
                }
            }

            // if statement
            if (statement.id == Stmt.IF) {
                writeIfStatement(out, owner, (Stmt.If) statement, visited);
            }

            // switch statement
            if (statement.id == Stmt.SWITCH) {
                writeSwitchStatement(out, owner, (Stmt.Switch) statement, visited);
            }

            // for statement
            if (statement.id == Stmt.FOR) {
                writeForStatement(out, owner, (Stmt.For) statement, visited);
            }

            // for-each statement
            if (statement.id == Stmt.FOREACH) {
                writeForEachStatement(out, owner, (Stmt.Foreach) statement, visited);
            }
        }
    }

    /**
     * Writes the contents of an if-statement into the sequence diagram.
     *
     * @param out       the writer
     * @param owner     the parent type
     * @param statement the if-statement instance
     * @param visited   the map containing visited methods
     * @throws IOException IO error
     */
    private void writeIfStatement(Writer out, Type owner, Stmt.If statement, CallStack visited) throws IOException {
        final boolean hasCallsInTrueBlock = hasSequenceStatements(statement.trueBlock);
        final boolean hasCallsInFalseBlock = hasSequenceStatements(statement.falseBlock);
        if (hasCallsInFalseBlock || hasCallsInTrueBlock) {
            out.write("alt ");
            out.write(statement.cond.toString());
            out.write(UmlUtil.NL);
            out.write(UmlUtil.INDENT);
            writeSequence(out, owner, statement.trueBlock, visited);

            if (hasCallsInFalseBlock) {
                // this is no bug, the second else is the label
                out.write("else ");
                out.write("else");
                out.write(UmlUtil.NL);
                out.write(UmlUtil.INDENT);
                writeSequence(out, owner, statement.falseBlock, visited);
            }
            out.write("end");
            out.write(UmlUtil.NL);
            out.write("' ------------- end if " + statement.cond.toString());
            out.write(UmlUtil.NL);
        }
    }

    /**
     * Writes the contents of an if-statement into the sequence diagram.
     *
     * @param out       the writer
     * @param owner     the parent type
     * @param statement the switch-statement instance
     * @param visited   the map containing visited methods
     * @throws IOException IO error
     */
    private void writeSwitchStatement(Writer out, Type owner, Stmt.Switch statement, CallStack visited) throws IOException {
        boolean hasCases = false;
        for (Stmt.Case theCase : statement.cases) {
            // skip cases without function calls
            hasCases = hasCases || hasSequenceStatements(theCase.block);
        }

        if (!hasCases) {
            return;
        }

        out.write("' ------------- switch start");
        out.write(UmlUtil.NL);

        out.write("alt switch");
        out.write(UmlUtil.NL);

        for (Stmt.Case theCase : statement.cases) {
            // skip cases without function calls
            if (!hasSequenceStatements(theCase.block)) {
                continue;
            }

            final String caseLabel = theCase.label.toString();
            out.write("' ------------- case " + caseLabel);
            out.write(UmlUtil.NL);
            out.write("else [case ");
            out.write(caseLabel);
            out.write("]");
            out.write(UmlUtil.NL);
            out.write(UmlUtil.INDENT);
            writeSequence(out, owner, theCase.block, visited);
        }
        out.write(UmlUtil.NL);
        out.write("end");
        out.write(UmlUtil.NL);
        out.write("' ------------- switch end");
        out.write(UmlUtil.NL);
    }

    /**
     * Writes the contents of an if-statement into the sequence diagram.
     *
     * @param out       the writer
     * @param owner     the parent type
     * @param statement the for-statement instance
     * @param visited   the map containing visited methods
     * @throws IOException IO error
     */
    private void writeForStatement(Writer out, Type owner, Stmt.For statement, CallStack visited) throws IOException {

        if (hasSequenceStatements(statement.block)) {
            out.write("loop {");
            out.write(statement.init.toString());
            out.write(" ; ");
            out.write(statement.cond.toString());
            out.write(" ; ");
            out.write(statement.update.toString());
            out.write(" }");
            out.write(UmlUtil.NL);
            out.write(UmlUtil.INDENT);
            writeSequence(out, owner, statement.block, visited);
            out.write("end");
            out.write(UmlUtil.NL);
        }
    }

    /**
     * Writes the contents of an if-statement into the sequence diagram.
     *
     * @param out       the writer
     * @param owner     the parent type
     * @param statement the foreach-statement instance
     * @param visited   the map containing visited methods
     * @throws IOException IO error
     */
    private void writeForEachStatement(Writer out, Type owner, Stmt.Foreach statement, CallStack visited) throws IOException {

        if (hasSequenceStatements(statement.block)) {
            out.write("loop {for ");
            out.write(statement.local.toString());
            out.write(" in ");
            out.write(statement.array.toString());
            out.write(" }");
            out.write(UmlUtil.NL);
            out.write(UmlUtil.INDENT);
            writeSequence(out, owner, statement.block, visited);
            out.write("end");
            out.write(UmlUtil.NL);
        }
    }

    /**
     * Writes a call entry into a sequence diagram.
     *
     * @param out     the writer
     * @param source  the source type (caller)
     * @param target  the target type (callee)
     * @param call    the called method
     * @param visited the map containing visited methods
     * @throws IOException IO error
     */
    private void writeCall(Writer out, Type source, Type target, Expr.Call call, CallStack visited) throws IOException {
        Method method = call.method;

        if (visited.contains(method)) {
            logCallLoop(method, "Possible call loop detected");

            Collections.reverse(visited);
            for (int i = 0; i < visited.size(); i++) {
                logCallLoop(visited.elementAt(i), String.format("[%02d] ", i));
            }
            return;
        }

        CallStack newStack = new CallStack(visited);
        newStack.add(method);

        out.write(source.name());
        out.write(" -> ");
        out.write(target.name());
        out.write(" : ");
        out.write(method.name());
        out.write(renderMethodArguments(method, true));
        out.write(UmlUtil.NL);

        // Add note if method has at least one parameter
        if (method.paramTypes().length > 0) {
            // numParams is not helpful, since it counts also the instance and wide params count as 2
            out.write("note right : ");
            out.write(call.toString().replace("this.", ""));
            out.write(UmlUtil.NL);
        }

        changeActivation(out, source, target, true, method.isNative());

        if (call.method instanceof MethodDef) {
            if (call.target == null || call.target.id != Expr.SUPER) {
                writeSequence(out, target, (MethodDef) method, newStack);
            } else {
                writeCall(out, source, source, call, newStack);
            }
        }

        if (source != target) {
            out.write("' ");
            out.write(source.name());
            out.write(" -> ");
            out.write(target.name());
            out.write(UmlUtil.NL);

            out.write(target.name());
            out.write(" --> ");
            out.write(source.name());
            //out.write("return ");
            //out.write(method.name()+"_" + target.name() + "_" + source.name());

            out.write(UmlUtil.NL);
        }

        changeActivation(out, source, target, false, method.isNative());
    }

    /**
     * Logs a possible call loop (one method calls another method which is already in the stack).
     * This may produce a lot of false, since it does not distinguish between instances. So e. g. AbstractState yields
     * a lot of theses messages, since it has members of the same type (and if state has a sub-state with the same
     * instance, this could cause indeed nasty situations).
     *
     * @param method  the method
     * @param message the message to show
     */
    private void logCallLoop(Method method, String message) {
        String loc = "?";
        if (isMethodDef.match(method)) {
            loc = ((MethodDef) method).loc.toString();
        }
        compiler.log.debug(String.format("%s {%s() [%s, %s]}", message, method.qname(), method.parent().qname(), loc));
    }

    /**
     * Checks if given statement block (method body) contains statement relevant for the sequence diagram.
     *
     * @param block the code block to check
     * @return true, if block contains at least on item to show in the sequence diagram.
     */
    private boolean hasSequenceStatements(Block block) {
        if (block == null) return false;

        for (Stmt stmt : block.stmts()) {
            // Method call
            if (stmt.id == Stmt.EXPR_STMT && ((Stmt.ExprStmt) stmt).expr.id == Expr.CALL) {
                return true;
            }
        }

        return false;
    }

    /**
     * Changes the activation of the given type (instance) in the sequence lifeline. If source and target are equal,
     * this method does nothing.
     *
     * @param out      the writer
     * @param source   the source type (set to null for initial activation)
     * @param target   the target type to (de)activate
     * @param activate true, if instance shall be activated; otherwise it is deactivated
     * @param isNative true, if method call is a native call
     * @throws IOException IO error
     */
    private void changeActivation(Writer out, Type source, Type target, boolean activate, boolean isNative) throws IOException {
        if (source == target) return;

        if (!activate) {
            // hack to build 'deactivate'
            out.write("de");
        }
        out.write("activate ");
        out.write(target.name());

        if (isNative) {
            out.write(" " + NATIVE_ACTIVE_COLOR);
        }
        out.write(UmlUtil.NL);
    }

    /**
     * Writes the Plant UML header
     *
     * @param out the writer
     * @throws IOException IO error
     */
    private void writeUmlHeader(Writer out, String name) throws IOException {
        out.write("@startuml ");
        out.write(name);
        out.write(UmlUtil.NL);
        out.write(UmlUtil.NL);
    }

    /**
     * Writes the Plant UML footer
     *
     * @param out the writer
     * @throws IOException IO error occurred
     */
    private void writeUmlFooter(Writer out) throws IOException {
        out.write(UmlUtil.NL);
        out.write("@enduml");
        out.write(UmlUtil.NL);
    }

    /**
     * Writes the given type as class entity to the given writer with or without slots.
     *
     * @param out       the writer
     * @param type      the type to export
     * @param withSlots true, if slots should be exported as well. Otherwise only associations and generalizations will appear
     * @throws IOException IO error
     */
    private void writeTypeDef(Writer out, Type type, boolean withSlots) throws IOException {
        writeLines(out, PUML_COMMON_COMMANDS);
        writeLines(out, PUML_CLASS_DIAGRAM_COMMANDS);

        if (type.isAbstract()) {
            out.write("abstract ");
        }

        if (type.base() != null) {
            out.write(String.format("class %s extends %s", typeName(type), typeName(type.base())));
        } else {
            out.write(String.format("class %s", typeName(type)));
        }

        if (withSlots) {
            out.write(" {");
            out.write(UmlUtil.NL);

            writeMemberSeparator(out, "Properties");

            for (Slot slot : type.slots()) {
                writeField(out, slot, isProperty);
            }

            writeMemberSeparator(out, "Fields");

            for (Slot slot : type.declared()) {
                writeField(out, slot, isInstanceField);
            }

            for (Slot slot : type.declared()) {
                writeField(out, slot, isClassField);
            }

            writeMemberSeparator(out, "Actions");

            for (Slot slot : type.declared()) {
                writeMethod(out, slot, isAction);
            }

            writeMemberSeparator(out, "Methods");

            for (Slot slot : type.declared()) {
                writeMethod(out, slot, isInstanceMethod);
            }

            for (Slot slot : type.declared()) {
                writeMethod(out, slot, isClassMethod);
            }


            out.write(UmlUtil.NL);
            out.write("}");
            out.write(UmlUtil.NL);

        }

        out.write(UmlUtil.NL);
        out.write("' Associations of ");
        out.write(typeName(type));
        out.write(UmlUtil.NL);

        for (Slot slot : type.declared()) {
            writeAssociations(out, slot, isFieldDef);
        }

        out.write(UmlUtil.NL);
    }

    /**
     * Writes the given type as mocked user interface.
     *
     * @param out  the writer
     * @param type the type to export
     * @throws IOException IO error
     */
    private void writeUserInterface(Writer out, Type type) throws IOException {
        out.write(" {");
        out.write(UmlUtil.NL);

        for (Slot slot : type.slots()) {
            writeUiField(out, slot, isProperty);
        }

        out.write("-- | -- | --");
        out.write(UmlUtil.NL);

        for (Slot slot : type.declared()) {
            writeUiMethod(out, slot, isAction);
        }

        out.write(UmlUtil.NL);
        out.write("}");
        out.write(UmlUtil.NL);

        out.write(UmlUtil.NL);
    }

    /**
     * Writes a member separator for a class.
     *
     * @param out the writer
     * @throws IOException IO error
     */
    private void writeMemberSeparator(Writer out, String sectionName) throws IOException {
        out.write(UmlUtil.INDENT);
        out.write("..");
        if (sectionName != null) {
            out.write(sectionName);
            out.write("..");
        }
        out.write(UmlUtil.NL);
    }

    /**
     * Writes a Sedona property using the given selector.
     *
     * @param out       the writer
     * @param slot      the field to write
     * @param predicate the field selector function
     * @throws IOException IO error
     */
    private void writeField(Writer out, Slot slot, FieldSelector predicate) throws IOException {
        if (!predicate.match(slot)) return;

        if (!slot.isField()) {
            compiler.log.warn(String.format("Passed non-field '%s' to writeField [%s]", slot.qname(), slot.toString()));
            return;
        }

        if (slot instanceof IrField) {
            compiler.log.warn(String.format("Passed IR field '%s' to writeField [%s]", slot.qname(), slot.toString()));
            return;
        }

        out.write(UmlUtil.INDENT);

        if (slot.isStatic()) {
            out.write("{static} ");
        }

        FieldDef field = (FieldDef) slot;

        out.write(UmlUtil.getScopePrefix(slot));

        out.write(slot.name());
        out.write(": ");

        out.write(typeName(field.type, false));
        if (field.type.isArray()) {
            out.write("[]");
        }

        if (field.init != null) {
            out.write(" = " + field.init.toString() + " ");
        }

        if (field.isConst()) {
            out.write(" <&lock-locked>");
        }


        if ((slot.flags() & Slot.RT_CONFIG) > 0) {
            out.write(" <&cog>");
        }

        out.write(UmlUtil.NL);
    }

    /**
     * Writes a Sedona property using the given selector as UI item.
     *
     * @param out       the writer
     * @param slot      the field to write
     * @param predicate the field selector function
     * @throws IOException IO error
     */
    private void writeUiField(Writer out, Slot slot, FieldSelector predicate) throws IOException {
        if (!predicate.match(slot)) return;

        if (!slot.isField()) {
            compiler.log.warn(String.format("Passed non-field '%s' to writeUiField [%s]", slot.qname(), slot.toString()));
            return;
        }

        if (slot instanceof IrField) {
            compiler.log.warn(String.format("Passed IR field '%s' to writeUiField [%s]", slot.qname(), slot.toString()));
            return;
        }

        FieldDef field = (FieldDef) slot;

        out.write(UmlUtil.INDENT);
        out.write(" ");

        writeUiLabel(out, field);
        out.write(" | . | ");

        switch (field.type().id()) {
            case Type.boolId:
                // a char must follow the brackets; otherwise it is interpreted as button
                out.write("[");
                out.write(String.valueOf(UmlUtil.getInitAsUiValue(field)));
                out.write("] .");
                break;
            case Type.intId:
            case Type.longId:
            case Type.byteId:
            case Type.shortId:
                if (UmlUtil.isEnum(field)) {
                    /*
                    String[] enums = getEnumLabels(field);
                    for (int i = 0; i < enums.length; i++) {
                        String anEnum = enums[i];
                        if (i > 0) {
                            out.write(" | ");
                        }
                        out.write("() ");
                        out.write(anEnum);
                    }
                    */
                    out.write("^");
                    out.write(String.valueOf(UmlUtil.getInitAsUiValue(field)));
                    out.write("^");
                    break;
                }
                // !! fall-through expected !!
            default:
                if (!UmlUtil.isReadOnly(field)) out.write("<b>");
                if (field.init != null) {
                    out.write(String.valueOf(UmlUtil.getInitAsUiValue(field)));
                } else {
                    out.write(UmlUtil.NO_INIT_STRING);
                }
                if (!UmlUtil.isReadOnly(field)) out.write("</b>");
        }

        String unit = SedonaUnits.getUnitAsString(field);
        if (unit.length() > 0) {
            out.write(" ");
            out.write(unit);
        }
        out.write(UmlUtil.NL);
    }

    /**
     * Writes a UI action using the given selector
     *
     * @param out      the writer
     * @param slot     the field to write
     * @param selector the field selector function
     * @throws IOException IO error
     */
    private void writeUiMethod(Writer out, Slot slot, FieldSelector selector) throws IOException {
        if (!selector.match(slot)) return;

        if (!slot.isMethod()) {
            compiler.log.warn(String.format("Passed non-method '%s' to writeMethod [%s]", slot.qname(), slot.toString()));
            return;
        }

        if (slot instanceof IrMethod) {
            compiler.log.warn(String.format("Passed IR type '%s' to writeMethod [%s]", slot.qname(), slot.toString()));
            return;
        }

        out.write(UmlUtil.INDENT);
        if (slot instanceof MethodDef) {
            MethodDef methodDef = (MethodDef) slot;
            if (methodDef.doc != null) {
                out.write("<color:gray><i>" + methodDef.doc.replace("\r|\n", "") + "</i> | ");
            }
        }

        out.write("[");

        out.write(UmlUtil.camelToHuman(slot.name()));
        out.write("]");


        out.write(UmlUtil.NL);
    }

    /**
     * Writes the user interface label for the given field.
     *
     * @param out      the writer
     * @param fieldDef the field to write the label for
     * @throws IOException IO error
     */
    private void writeUiLabel(Writer out, FieldDef fieldDef) throws IOException {
        // config properties are bold
        if (fieldDef.isRtConfig()) {
            out.write("<b>");
        }

        // Render read-only fields in grey color
        if (UmlUtil.isReadOnly(fieldDef)) {
            out.write("<color:gray>");
        }

        out.write(UmlUtil.camelToHuman(fieldDef.name()));

        if (fieldDef.isRtConfig()) {
            out.write("</b>");
        }
    }

    /**
     * Writes a Sedona property using the given selector
     *
     * @param out      the writer
     * @param slot     the field to write
     * @param selector the field selector function
     * @throws IOException IO error
     */
    private void writeAssociations(Writer out, Slot slot, FieldSelector selector) throws IOException {
        if (!selector.match(slot)) return;

        if (!slot.isField()) {
            compiler.log.warn(String.format("Passed non-field '%s' to writeField [%s]", slot.qname(), slot.toString()));
            return;
        }

        if (slot instanceof IrField) {
            compiler.log.warn(String.format("Passed IR field '%s' to writeField [%s]", slot.qname(), slot.toString()));
            return;
        }

        FieldDef field = (FieldDef) slot;

        // Omit primitive types (including Str and Buf)
        if (UmlUtil.isSedonaType(field.type)) {
            return;
        }

        Type parent = field.parent();

        out.write(typeName(parent, false));
        out.write(" ");

        // Show inline fields as aggregation
        if (field.isInline()) {
            out.write("o");
        }
        out.write("--> ");

        if (field.type.isArray()) {
            out.write("\"*\" ");
        } else {
            out.write("\"1\" ");
        }

        out.write(typeName(field.type, false));

        out.write(": ");
        out.write(slot.name());

        if (field.isStatic()) {
            out.write(" <<static>> ");
        }
        out.write(UmlUtil.NL);
    }

    /**
     * Writes a Sedona method using the given selector
     *
     * @param out      the writer
     * @param slot     the field to write
     * @param selector the field selector function
     * @throws IOException IO error
     */
    private void writeMethod(Writer out, Slot slot, FieldSelector selector) throws IOException {
        if (!selector.match(slot)) return;

        if (!slot.isMethod()) {
            compiler.log.warn(String.format("Passed non-method '%s' to writeMethod [%s]", slot.qname(), slot.toString()));
            return;
        }

        if (slot instanceof IrMethod) {
            compiler.log.warn(String.format("Passed IR type '%s' to writeMethod [%s]", slot.qname(), slot.toString()));
            return;
        }

        out.write(UmlUtil.INDENT);

        if (slot.isStatic()) {
            out.write("{static} ");
        }

        if (slot.isAbstract()) {
            out.write("{abstract} ");
        }

        out.write(UmlUtil.getScopePrefix(slot));
        out.write(slot.name());
        out.write(renderMethodArguments((MethodDef) slot, true));
        out.write(UmlUtil.NL);
    }

    /**
     * Renders the parameter list for the given method.
     *
     * @param method    the method to render the arguments for
     * @param withNames whether to add the parameter type or just the name
     * @return a string representing the method arguments, e. g. "(x: int)"
     */
    private String renderMethodArguments(Method method, @SuppressWarnings("SameParameterValue") boolean withNames) {
        if (method instanceof MethodDef) {
            return renderMethodArgumentsDef((MethodDef) method, withNames);
        } else {
            return renderMethodArgumentsIr(method);
        }
    }

    /**
     * Writes the given string array to the given writer line by line.
     *
     * @param out   the writer
     * @param lines the lines to write
     * @throws IOException IO error
     */
    private void writeLines(Writer out, String[] lines) throws IOException {
        out.write(UmlUtil.NL);
        for (String line : lines) {
            out.write(line);
            out.write(UmlUtil.NL);
        }
    }

    /**
     * Renders the parameter list for the given method definition.
     *
     * @param methodDef the method to render the arguments for
     * @param withNames whether to add the parameter type or just the name
     * @return a string representing the method arguments, e. g. "(x: int)"
     */
    private String renderMethodArgumentsDef(MethodDef methodDef, boolean withNames) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        for (int i = 0; i < methodDef.params.length; i++) {
            ParamDef paramDef = methodDef.params[i];
            if (i > 0) {
                sb.append(", ");
            }
            if (withNames) {
                sb.append(paramDef.name).append(": ");
            }
            sb.append(typeName(paramDef.type, false));
        }
        sb.append(")");

        if (methodDef.ret != null && !methodDef.ret.isVoid()) {
            sb.append(": ").append(typeName(methodDef.ret, false));
        }

        return sb.toString();
    }

    /**
     * Renders the parameter list for the given (IR) method. Opposing to method definitions IR methods only
     * offers the types but not the argument names.
     *
     * @param method the method to render the arguments for
     * @return a string representing the method arguments, e. g. "(int)"
     */
    private String renderMethodArgumentsIr(Method method) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        for (int i = 0; i < method.paramTypes().length; i++) {
            Type paramDef = method.paramTypes()[i];

            if (i > 0) {
                sb.append(", ");
            }

            sb.append(typeName(paramDef, false));

        }
        sb.append(")");

        if (method.returnType() != null && !method.returnType().isVoid()) {
            sb.append(": ").append(typeName(method.returnType(), false));
        }

        return sb.toString();
    }

    /**
     * Gets the full-qualified or simple type name (according to umlFq)
     *
     * @param type the type to get the type name for
     * @return the type name
     */
    private String typeName(Type type) {
        return typeName(type, compiler.umlFq);
    }

    /**
     * Gets the (full-qualified) type name according to the compiler setting -ufq
     *
     * @param type the type to get the name for
     * @return the (full-qualified) type name
     */
    private String typeName(Type type, boolean fullQualified) {
        if (fullQualified) {
            if (type.isArray()) {
                return type.arrayOf().qname();
            } else {
                return type.qname();
            }
        } else {
            if (type.isArray()) {
                return type.arrayOf().name();
            } else {
                return type.name();
            }
        }
    }

    /**
     * Count all slots matching the given selector.
     *
     * @param type     the type to check
     * @param selector the selector which a slot has to macth to
     * @return the number of matching slots
     */
    private int countSlots(Type type, FieldSelector selector) {
        int count = 0;
        for (Slot slot : type.slots()) {
            if (selector.match(slot)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * Check if type is ignored (source file matches at least one pattern defined
     * in {@link #IGNORED_FILES}).
     *
     * @param t the type to check
     * @return true, if given type should be ignored
     */
    private boolean matchesIgnorePattern(Type t) {
        if (t instanceof TypeDef) {
            TypeDef typeDef = (TypeDef) t;
            String name = (new File(typeDef.loc.file)).getName();

            for (String ignoredFile : IGNORED_FILES) {
                if (name.matches(ignoredFile)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks, if the types´ source file is older than the given target file.
     *
     * @param type    the type to check.
     * @param umlFile the target file to check against source file.
     * @return true, if target file is up-to-date; otherwise false
     */
    private boolean isTypeUpToDate(Type type, File umlFile) {
        return type instanceof TypeDef && FileUtil.isUpToDate(new File(((TypeDef) type).loc.file), umlFile);
    }



    static class CallStack extends Stack<Method> {

        public CallStack(CallStack visited) {
            this.addAll(visited);
        }

        public CallStack() {
            super();
        }
    }
}
