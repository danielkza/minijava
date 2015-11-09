//package Translate;
package visitor.translate;

import frame.Frame;
import tree.Stm;

public class ProcFrag extends Frag {
    public Stm body;
    public Frame frame;

    public ProcFrag(Stm b, Frame f) {
        body = b;
        frame = f;
    }
}
