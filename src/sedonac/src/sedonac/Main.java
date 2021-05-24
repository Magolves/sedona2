//
// Copyright (c) 2006 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   30 May 06  Brian Frank  Creation
//

package sedonac;

import sedona.Env;
import sedona.util.Log;
import sedona.util.Version;
import sedonac.test.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Main command line entry point for the Sedona compiler.
 */
public class Main
{

  public static void usage()
  {
    println("usage:");
    println("  sedonac [options] <input file>");
    println("inputs:");
    println("  dir          directory containing kit.xml file");
    println("  kit.xml      compile Sedona source files into kit file");
    println("  scode.xml    compile Sedona kits into a scode image");
    println("  *.sax        convert sax to sab");
    println("  *.sab        convert sab to sax");
    println("options:");
    println("  -doc         generate HTML Sedona docs for kit");
    println("  -outDir      output directory");
    println("  -v           verbose logging");
    println("  -ver         print version info and exit");
    println("  -? -help     print this usage synopsis");
    println("  -test        run test suite");
    println("  -layout      dump field layout (when compiling image)");
    println("  -kitVersion  force output kit to have specified version");
    println("  -noOptimize  skip const folding and optimization steps");
    println("  -noChecksum  exclude checksums from sax if input is sab file");
    println("  -stageSim    stage platform for simulated SVM build");
    println("");
    println("  -udoc        UML: Enable UML export");
    println("  -uall        UML: Export all types (instead reflective types only)");
    println("  -ufq         UML: Use full-qualified type names");
    println("  -usplit      UML: Generate overview and class-diagrams for each type (recommended for big kits)");
    println("  -uprv        UML: Generate also private and protected members");
    println("  -uplant      UML: Render generated diagrams with plantUML");
    println("                    (plantUML.jar must be in classpath)");
    println("  -usvg        UML: Render diagrams as SVG file (otherwise PNG is used)");
  }

  private static void errUsage(String err)
  {
    if (err != null) println("ERROR: " + err + '\n');
    usage();
    System.exit(1);
  }

  public static void println(String msg)
  {
    System.out.println(msg);
  }

  public static int doMain(String args[])
  {
    // check vm version
    if (!Env.checkJavaVersion())
      return 1;

    // no args
    if (args.length == 0)
    {
      usage();
      return 1;
    }

    // if first arg is class name, then rsun that main (so
    // launcher can always use this entry point)
    if (args[0].startsWith("sedona.") || args[0].startsWith("sedonac."))
    {
      try
      {
        Class cls = Class.forName(args[0]);
        String[] a = new String[args.length-1];
        System.arraycopy(args, 1, a, 0, a.length);
        cls.getMethod("main", new Class[] { String[].class })
          .invoke(null, new Object[] { a });
      }
      catch (InvocationTargetException e)
      {
        e.getTargetException().printStackTrace();
      }
      catch (Exception e)
      {
        System.out.println("ERROR: Cannot run main: " + args[0]);
        e.printStackTrace();
      }
      return 0;
    }

    // init compiler
    Compiler compiler = new Compiler();
    String input = null;

    // process args
    for (int i=0; i<args.length; ++i)
    {
      String arg = args[i];
      if (arg.equals("-?") || arg.equals("-help"))
      {
        usage();
        return 1;
      }
      else if (arg.equals("-doc"))
      {
        compiler.doc = true;
      }
      else if (arg.equals("-outDir"))
      {
        if (i+1 >= args.length)
          errUsage("Missing outDir option");
        else
          compiler.outDir = new File(args[++i]);
      }
      else if (arg.equals("-ver"))
      {
        Env.printVersion("Sedona Compiler");
        return 1;
      }
      else if (arg.equals("-v"))
      {
        compiler.log.severity = Log.DEBUG;
      }
      else if (arg.equals("-noOptimize"))
      {
        compiler.optimize = false;
      }
      else if (arg.equals("-www"))
      {
        compiler.www = true;
      }
      else if (arg.equals("-layout"))
      {
        compiler.dumpLayout = true;
      }
      else if (arg.equals("-test"))
      {
        String testName = null;
        if (i+1 < args.length) testName = args[i+1];
        return Test.run(testName);
      }
      else if (arg.equals("-kitVersion"))
      {
        if (i+1 >= args.length)
          errUsage("Missing kitVersion option");
        else
          compiler.kitVersion = new Version(args[++i]);
      }
      else if (arg.equals("-noChecksum"))
      {
        compiler.nochk = true;
      }
      else if (arg.equals("-stageSim"))
      {
        compiler.sim = true;
      }
      // UML options
      else if (arg.equals("-udoc"))
      {
        compiler.umlDoc = true;
      }
      else if (arg.equals("-ufq"))
      {
        compiler.umlFq = true;
      }
      else if (arg.equals("-uall"))
      {
        compiler.umlAllTypes = true;
      }
      else if (arg.equals("-usplit"))
      {
        compiler.umlSplitClsDgms = true;
      }
      else if (arg.equals("-uprv"))
      {
        compiler.umlOnlyPublic = false;
      }
      else if (arg.equals("-uplant"))
      {
        compiler.umlRenderWithPlantUml = true;
      }
      else if (arg.equals("-usvg"))
      {
        compiler.umlSvg = true;
      }
      else if (arg.startsWith("-"))
      {
        errUsage("Unrecognized option " + arg);
      }
      else
      {
        if (input == null)
          input = arg;
        else
          println("WARNING: Ignoring argument " + arg);
      }
    }

    if (input == null)
    {
      println("ERROR: No input specified");
      return 1;
    }

    // run compiler as setup by arguments
    try
    {
      compiler.compile(new File(input));
      int num = compiler.warnings.size();
      if (num > 0)
        System.out.println("*** Success with " + num + " warning(s) ***");
      else
        System.out.println("*** Success! ***");
      return 0;
    }
    catch(CompilerException e)
    {
      int numWarn = compiler.warnings.size();
      int numErr  = compiler.logErrors();
      if (numErr == 0)
        e.printStackTrace();
      else
      {
        System.out.println("*** FAILED with " + numErr + " error(s) and " + numWarn + " warning(s) ***");
      }
      return 1;
    }
    catch(Throwable e)
    {
      compiler.log.error("Internal compiler error", e);
      e.printStackTrace();
      return 1;
    }
  }

  public static void main(String args[])
  {
    try
    {
      int r = doMain(args);
      System.exit(r);
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
  }

}
