package sedonacmt;

import sedona.xml.XWriter;
import sedonac.CompilerException;
import sedonac.Location;
import sedonac.ast.KitDef;

import java.io.File;

import static sedonac.steps.WritePuml.UML_DIRECTORY_NAME;
import static sedonac.util.Env.SEDONA_HOME;
import static sedonac.util.UmlUtil.NL;

public class WriteKitsUmlDoc extends MultiCompilerStep {
    public static final String CSS_PATH = "css/style.css";
    public static final String IMAGES_PATH = "images";

    private File dir;

    public WriteKitsUmlDoc(MultiCompilerContext context) {
        super(context);

        final String umlSubDir = UML_DIRECTORY_NAME;

        File umlBaseDirectory;
        if (context.outDir != null) {
            umlBaseDirectory = new File(context.outDir + File.separator +
                    umlSubDir);
        } else {
            String sedona_home = System.getenv(SEDONA_HOME);
            if (sedona_home != null && (new File(sedona_home).exists())) {
                umlBaseDirectory = new File(sedona_home + File.separator + "doc" + File.separator + umlSubDir);
                context.log.debug("  SEDONA_HOME present, using " + umlBaseDirectory);
            } else {
                umlBaseDirectory = new File(umlSubDir);
            }
        }
        makeDirectory(umlBaseDirectory);
        
        dir = umlBaseDirectory;
    }

    @Override
    void doRun() {
        //if (!context.umlDoc) return;

        // Check preconditions
        if (dir == null) {
            context.log.error("Output dir has not been set!");
            return;
        }

        if (!dir.exists()) {
            context.log.error("Output dir does not exist!");
            return;
        }

       index();
    }

//////////////////////////////////////////////////////////////////////////
// IO
//////////////////////////////////////////////////////////////////////////    

    /**
     * Creates the desired directory (including required parents) and logs it.
     *
     * @param directory the directory to create.
     */
    private void makeDirectory(File directory) {
        if (!directory.exists()) {
            context.log.debug("Create directory " + directory.getAbsolutePath());
            if (!directory.mkdirs()) {
                context.log.error("Create directory " + directory.getAbsolutePath() + " failed");
            }
        }
    }

//////////////////////////////////////////////////////////////////////////
// Header/Footer
//////////////////////////////////////////////////////////////////////////

    private void exportPageHeader(XWriter out) {
        out.w("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"").w(NL);
        out.w(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">").w(NL);
        out.w("<html xmlns=\"http://www.w3.org/1999/xhtml\">").w(NL);
        out.w("<head>").w(NL);
        out.w("  <title>").w("Kit Index").w("</title>").w(NL);
        out.w("  <meta http-equiv='Content-type' content='text/html;charset=UTF-8' />").w(NL);
        out.w("  <link rel='stylesheet' type='text/css' href='" + CSS_PATH + "'/>").w(NL);
        out.w("</head>").w(NL);
        out.w("<body>").w(NL);
        out.w("<hr class='puml'>").w(NL);
        out.w("<a name=\"_top\">").w(NL);

        out.w("<h1>Kit Overview</h1>").w(NL);
        out.w("<hr>").w(NL);
    }

//////////////////////////////////////////////////////////////////////////
// Index
//////////////////////////////////////////////////////////////////////////

    private void index() {
        File f = new File(dir, "index.html");
        try {
            XWriter out = new XWriter(f);

            exportPageHeader(out);

            out.w("<div class='flex-kit-container'>");
            for (int i = 0; i < context.kits.size(); i++) {
                KitProxy kitProxy = context.kits.get(i);
                exportKit(out, kitProxy);
            }
            out.w("</div>");
            exportPageFooter(out);

            out.close();
            context.log.info(String.format(" Wrote %s", f.getAbsolutePath()));
        } catch (Exception e) {
            context.log.error(new CompilerException("Cannot write file", new Location(f), e));
        }
    }

    private void exportKit(XWriter out, KitProxy kitProxy) {
        KitDef kit = kitProxy.getKitDef();

        String warning = "";
        if (kitProxy.containsErrors()) {
            warning = String.format("<img class='icon-small' src='%s/alert-warning.svg'/>", IMAGES_PATH);
        }

        out.w(String.format("<div class='kit %s'>", kit.vendor)).w(NL);
        out.w(String.format("<a class='%s'  href='%s/index.html'><h4>%s%s</h4></a>", kit.vendor, kit.name,kit.name, warning));


        if (kit.description != null) {
            out.w(tag(kit.description, "small")).w(NL);
        }
        out.w("</div>").w(NL);
    }

    private void exportPageFooter(XWriter out) {
        out.w("<div class='copyright'><script type='text/javascript'>document.write(\"Copyright &#169; \" + new Date().getFullYear() + \" Oliver Wieland\")</script></div>").w(NL);
        out.w("</body>").w(NL);
        out.w("</html>").w(NL);
    }

    @SuppressWarnings("SameParameterValue")
    private String tag(String content, String tag) {
        return String.format("<%s>%s</%s>", tag, content, tag);
    }

}
