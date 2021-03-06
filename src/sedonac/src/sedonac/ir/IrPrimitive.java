//
// Copyright (c) 2007 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   26 Apr 07  Brian Frank  Creation
//

package sedonac.ir;

import sedonac.namespace.*;

/**
 * IrPrimitive is used to model one of the predefined primitive types.
 */
public class IrPrimitive
  implements IrAddressable
{

//////////////////////////////////////////////////////////////////////////
// Constructor
//////////////////////////////////////////////////////////////////////////

  public IrPrimitive(PrimitiveType type)
  {
    this.type = type;
  }

//////////////////////////////////////////////////////////////////////////
// Access
//////////////////////////////////////////////////////////////////////////

  public String toString()
  {
    return type.toString();
  }

//////////////////////////////////////////////////////////////////////////
// IrAddressable
//////////////////////////////////////////////////////////////////////////

  public int getBlockIndex() { return blockIndex; }
  public void setBlockIndex(int i) { blockIndex = i; }

  public boolean alignBlockIndex() { return true; }

//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

  public final PrimitiveType type;
  public int blockIndex;

}
