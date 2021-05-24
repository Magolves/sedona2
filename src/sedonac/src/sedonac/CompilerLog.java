//
// Copyright (c) 2006 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   30 May 06  Brian Frank  Creation
//

package sedonac;     

import sedona.util.Log;
import sedonac.util.AsciiColor;

import java.io.*;

/**
 * ComplerLog is responsible for outputing compiler messages
 */
public class CompilerLog
  extends sedona.util.Log
{
  private boolean withColors = true;
  private String logName = "";

  public CompilerLog()
  {
    this("sedonac", System.out, true);
  }
  
  public CompilerLog(PrintStream out)
  {
    this("sedonac", out, true);
  }

  public CompilerLog(String logName, PrintStream printStream, boolean withColors) {
    super(logName, printStream);
    this.withColors = withColors;
  }

  public String getLogName() {
    return logName;
  }

  public void setLogName(String logName) {
    this.logName = logName;
  }

  public void error(CompilerException e)
  {
    log(ERROR, e.toLogString(), e.cause);
  }                        
  
  public void log(int severity, String msg, Throwable ex)
  {
    if (severity < this.severity) return;

    // get console color
    final String color = getColor(severity);

    if (logName != null && logName.length() > 0) {
      out.println(String.format("[%s%s%s] %s%s%s",
              AsciiColor.BLUE_BOLD, logName, AsciiColor.RESET,
              color, msg, AsciiColor.RESET));
    } else  {
      out.println(String.format("- %s%s%s", color, msg, AsciiColor.RESET));
    }
    if (ex != null) ex.printStackTrace(out);
  }

  private String getColor(int severity) {
    switch(severity) {
      case Log.ERROR: return AsciiColor.RED_BOLD;
      case Log.WARN: return AsciiColor.YELLOW;
      case Log.DEBUG: return AsciiColor.WHITE;
      default: return AsciiColor.RESET;
    }
  }

}
