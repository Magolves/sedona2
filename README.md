# Sedona 2

Experimental extensions to Sedona. Sedona is very simple programming languages intended
for small controllers - similar to Lua. 
See [Sedona alliance](<https://www.sedona-alliance.org/resources.htm>).


## Improved compilation

The old Sedona Compiler `sedonac` was limited to compile a single kit at once. Even worse, it compiled the complete kit even if the kit was up-to-date.

The improved compiler `sedonac` and `sedonacmt` offers then following features to reduce compile time

### No unnecessary rebuild

Compilation is skipped if kit file is up-to-date.

### Generate UML diagrams

`sedonac` and `sedonacmt` generate ([PlantUML](<https://plantuml.com>) class and sequence diagrams.

### Build-capabilities

`sedonacmt` scans all sub-directories fot `kit.xml` and rebuilds all kits, if required. Furthermore it uses the kit dependencies to determine the build sequence and tries also to use as many cores as possible to parallelize compilation.

