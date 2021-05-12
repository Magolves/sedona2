# Build the Sedona compiler

This folder contains a cmake script to build two variants of the Sedona compiler

- `sedonac` the default compiler without modifications
- `sedonac-rb` the modified compiler with additional features

## Additional features of `sedonac-rb`

- Compiles all kits by executing `sedonac-rb .` in the `src` directory
- Generates and renders UML diagrams to `<SEDONA_HOME>/doc/uml`. Rendering requires that the archive `plantuml.jar` is present in the directory `<SEDONA_HOME>/lib`.

## Build `sedonac-rb` jar

The folder `sedonac/src` contains an IJ project named `sedonac-uml.iml` which builds a jar file (`out\artifacts\sedonac_rb\sedonac-rb.jar`). Unfortunately it contains also the dependend jar files and need to be *removed manually*.
After that the archive can be copied to `<SEDONA_HOME>/lib`.

## Build `sedonac` executable

The `sedonac(-rb)` compilers built and installed via `cmake`. The binaries are installed into folder `<SEDONA_HOME>/bin`.

```powershell
PS> cmake . -G "Ninja"
PS> ninja install
```

After that you should be able to run `sedonac-rb` as follows

```powershell
PS c:\\sedona-home\bin> .\sedonac-rb.exe
[Make] Nothing to compile
[Make] Compile 0 kits in 0 steps
```

## Generation of UML diagrams

`sedonac-rb` offers the following options to generate UML diagrams

```text
-udoc        UML: Enable UML export. *Without this switch the kits are only compiled, but no UML diagram is generated*
-uall        UML: Export all types (instead reflective types only)
-ufq         UML: Use full-qualified type names
-usplit      UML: Generate overview and class-diagrams for each type (recommended for big kits)
-uprv        UML: Generate also private and protected members
-uplant      UML: Render generated diagrams with plantUML (plantUML.jar must be in classpath)
-usvg        UML: Render diagrams as SVG file (otherwise PNG is used)
```
