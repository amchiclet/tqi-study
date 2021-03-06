package bh;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A class used to representing particles in the N-body simulation.
 **/
public final class Body extends Node {
  MathVector vel;
  MathVector acc;
  MathVector newAcc;
  double phi;

  @Nullable Body next;

  /**
   * Create an empty body.
   **/
  public Body() {
    vel = new MathVector();
    acc = new MathVector();
    newAcc = new MathVector();
    phi = 0.0;
    next = null;
  }

  /**
   * Set the next body in the list.
   * 
   * @param n the body
   **/
  public final void setNext(@Nullable Body n) {
    next = n;
  }

  /**
   * Get the next body in the list.
   * 
   * @return the next body
   **/
  public final @Nullable Body getNext() {
    return next;
  }

  /**
   * Enlarge cubical "box", salvaging existing tree structure.
   * 
   * @param tree the root of the tree.
   * @param nsteps the current time step
   **/
  public final void expandBox(Tree tree, int nsteps) {
    MathVector rmid = new MathVector();

    boolean inbox = icTest(tree);
    while (!inbox) {
      double rsize = tree.rsize;
      rmid.addScalar(tree.rmin, 0.5 * rsize);

      for (int k = 0; k < MathVector.NDIM; k++) {
        if (pos.value(k) < rmid.value(k)) {
          double rmin = tree.rmin.value(k);
          tree.rmin.value(k, rmin - rsize);
        }
      }
      tree.rsize = 2.0 * rsize;
      if (tree.root != null) {
        MathVector ic = tree.intcoord(rmid);
        if (ic == null) throw new Error("Value is out of bounds");
        int k = oldSubindex(ic, IMAX >> 1);
        if (tree.root != null) {
          Cell newt = new Cell();
          newt.subp[k] = tree.root;
          tree.root = newt;
          inbox = icTest(tree);
        }
      }
    }
  }

  /**
   * Check the bounds of the body and return true if it isn't in the correct bounds.
   **/
  public final boolean icTest(Tree tree) {
    double pos0 = pos.value(0);
    double pos1 = pos.value(1);
    double pos2 = pos.value(2);

    // by default, it is in bounds
    boolean result = true;

    double xsc = (pos0 - tree.rmin.value(0)) / tree.rsize;
    if (!(0.0 < xsc && xsc < 1.0)) {
      result = false;
    }

    xsc = (pos1 - tree.rmin.value(1)) / tree.rsize;
    if (!(0.0 < xsc && xsc < 1.0)) {
      result = false;
    }

    xsc = (pos2 - tree.rmin.value(2)) / tree.rsize;
    if (!(0.0 < xsc && xsc < 1.0)) {
      result = false;
    }

    return result;
  }

  /**
   * Descend Tree and insert particle. We're at a body so we need to create a cell and attach this
   * body to the cell.
   * 
   * @param p the body to insert
   * @param xpic
   * @param l
   * @param tree the root of the data structure
   * @return the subtree with the new body inserted
   **/
  public final Node loadTree(Body p, MathVector xpic, int l, Tree tree) {
    // create a Cell
    Cell retval = new Cell();
    int si = subindex(tree, l);
    // attach this Body node to the cell
    retval.subp[si] = this;

    // move down one level
    si = oldSubindex(xpic, l);
    Node rt = retval.subp[si];
    if (rt != null)
      retval.subp[si] = rt.loadTree(p, xpic, l >> 1, tree);
    else
      retval.subp[si] = p;
    return retval;
  }

  /**
   * Return an enumeration of the bodies
   * 
   * @return an enumeration of the bodies
   **/
  public final Enumerator elements() {
    return new Enumerator(Body.this);
  }

  /**
   * Determine which subcell to select. Combination of intcoord and oldSubindex.
   * 
   * @param t the root of the tree
   **/
  public final int subindex(Tree tree, int l) {
    MathVector xp = new MathVector();

    double xsc = (pos.value(0) - tree.rmin.value(0)) / tree.rsize;
    xp.value(0, Math.floor(IMAX * xsc));

    xsc = (pos.value(1) - tree.rmin.value(1)) / tree.rsize;
    xp.value(1, Math.floor(IMAX * xsc));

    xsc = (pos.value(2) - tree.rmin.value(2)) / tree.rsize;
    xp.value(2, Math.floor(IMAX * xsc));

    int i = 0;
    for (int k = 0; k < MathVector.NDIM; k++) {
      if (((int) xp.value(k) & l) != 0) {
        i += Cell.NSUB >> (k + 1);
      }
    }
    return i;
  }

  /**
   * Return a string representation of a body.
   * 
   * @return a string representation of a body.
   **/
  public String asString() {
    return "Body " + super.asString();
  }

}
