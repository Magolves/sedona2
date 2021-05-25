# Notes for later use

## 64bit-Support

Currently 32bit (`refSize`) is hard-coded in `steps/FieldLayout.java`:

```java
public FieldLayout(Compiler compiler)
{
    super(compiler);
    this.refSize = compiler.image == null ? 4 : compiler.image.refSize;
}
```

Additional sizes in `Namespace.java` needs to be adjusted

```java
public final PrimitiveType voidType   = primitive("void",   Type.voidId,  0);
public final PrimitiveType boolType   = primitive("bool",   Type.boolId,  1);
public final PrimitiveType byteType   = primitive("byte",   Type.byteId,  1);
public final PrimitiveType shortType  = primitive("short",  Type.shortId, 4); // 2
public final PrimitiveType intType    = primitive("int",    Type.intId,   8); // 4
public final PrimitiveType longType   = primitive("long",   Type.longId,  16); //8
public final PrimitiveType floatType  = primitive("float",  Type.floatId, 4);
public final PrimitiveType doubleType = primitive("double", Type.doubleId, 8);
```

Requires also an extra instruction for 128bit values?

SCode with `RefSize = 8`:

```text
[ 96%] Built target scode_platUnix
[ 97%] Sedona: Compile scode platMacOs
[ReadKits]   ReadKits [15 kits]
[FieldLayout] [0x       0] 0 fields with sizeof 1, offset 0
[FieldLayout] [0x       0] 0 fields with sizeof 2, offset 0
[FieldLayout] [0x       0] 0 fields with sizeof 4, offset 0
[FieldLayout] [0x       0] 0 fields with sizeof 1, offset 0
[FieldLayout] [0x       0] 0 fields with sizeof 2, offset 0
[FieldLayout] [0x       0] 0 fields with sizeof 4, offset 0
[FieldLayout] [0x       0] 1 fields with sizeof 1, offset 0
[FieldLayout] [0x       0] ==> off = 0, size match =false, primitive = false
[FieldLayout] [0x       0] 1 fields with sizeof 2, offset 0
[FieldLayout] [0x       0] ==> off = 0, size match =false, primitive = false
[FieldLayout] [0x       0] 1 fields with sizeof 4, offset 0
[FieldLayout] [0x       0] ==> off = 0, size match =false, primitive = false
[FieldLayout] Internal compiler error
```