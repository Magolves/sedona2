# Sedona 2

Experimental extensions to Sedona.

See [Sedona alliance](<https://www.sedona-alliance.org/resources.htm>)]

## 64bit-Support

Currently 32bit (`refSize`) is hard-coded in `steps/FieldLayout.java`:

```java
public FieldLayout(Compiler compiler)
{
    super(compiler);
    this.refSize = compiler.image == null ? 4 : compiler.image.refSize;
}
```