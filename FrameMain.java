import java.util.ArrayList;
import java.util.List;

import frame.mips.MipsFrame;
import frame.Frame;

public class FrameMain {
    public static void main(String[] arguments) {
        List<Boolean> formals1 = new ArrayList<>();
        formals1.add(true);
        formals1.add(true);
        formals1.add(true);
        formals1.add(false);
        formals1.add(true);
        formals1.add(false);
        formals1.add(false);
        formals1.add(false);
        formals1.add(false);
        formals1.add(false);

        Frame frame1 = new MipsFrame("A", formals1);
        System.out.println(frame1.toString());

        ArrayList<Boolean> formals2 = new ArrayList<>();
        formals2.add(false);
        formals2.add(false);
        formals2.add(true);
        formals2.add(false);
        formals2.add(true);

        Frame frame2 = frame1.newFrame("B", formals2);
        System.out.println(frame2.toString());
    }
}
