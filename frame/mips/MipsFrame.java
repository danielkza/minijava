package frame.mips;

import java.util.*;

import frame.*;
import symbol.Symbol;

public class MipsFrame extends Frame {
    private static Map<String, Integer> functions = new HashMap<>();

    static final int WORD_SIZE = 4;
    
    private static Temp reg(final String name) {
        return new Temp() {
            @Override
            public String toString() {
                return name;
            }
        };
    }
    
    static final Temp
        ZERO = reg("$0"), // zero reg
        AT = reg("$at"), // reserved for assembler
        V0 = reg("$v0"), // function result
        V1 = reg("$v1"), // second function result
        A0 = reg("$a0"), // argument1
        A1 = reg("$a1"), // argument2
        A2 = reg("$a2"), // argument3
        A3 = reg("$a3"), // argument4
        T0 = reg("$t0"), // caller-saved
        T1 = reg("$t1"),
        T2 = reg("$t2"),
        T3 = reg("$t3"),
        T4 = reg("$t4"),
        T5 = reg("$t5"),
        T6 = reg("$t6"),
        T7 = reg("$t7"),
        S0 = reg("$s0"), // callee-saved
        S1 = reg("$s1"),
        S2 = reg("$s2"),
        S3 = reg("$s3"),
        S4 = reg("$s4"),
        S5 = reg("$s5"),
        S6 = reg("$s6"),
        S7 = reg("$s7"),
        T8 = reg("$t8"), // caller-saved
        T9 = reg("$t9"),
        K0 = reg("$k0"), // reserved for OS kernel
        K1 = reg("$k1"), // reserved for OS kernel
        GP = reg("$gp"), // pointer to global area
        SP = reg("$sp"), // stack pointer
        S8 = reg("$fp"), // callee-save (frame pointer)
        RA = reg("$ra"); // return address

    // Register lists: must not overlap and must include every register that
    // might show up in code
    static final Temp[]
        // registers dedicated to special purposes
        specialRegs = { ZERO, AT, K0, K1, GP, SP },
        // registers to pass outgoing arguments
        argRegs = { A0, A1, A2, A3 },
        // registers that a callee must preserve for its caller
        calleeSaves = { RA, S0, S1, S2, S3, S4, S5, S6, S7, S8 },
        // registers that a callee may use without preserving
        callerSaves = { T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, V0, V1 };
    
    static final Temp FP = new Temp(); // virtual frame pointer (eliminated)

    private int offset = 0;
    private List<Access> actuals;

    public MipsFrame(String name, List<Boolean> formalsEscapes) {
        actuals = new LinkedList<>();
        this.formals = new LinkedList<>();

        Integer count = functions.get(name);
        if (count == null) {
            count = 0;
            this.label = new Label(name);
        } else {
            count = count + 1;
            this.label = new Label(name + "." + count);
        }

        functions.put(name, count);

        Iterator<Boolean> escapes = formalsEscapes.iterator();
        if(!escapes.hasNext())
            return;

        List<Temp> allArgRegs = new ArrayList<>(argRegs.length + 1);
        allArgRegs.add(V0);
        allArgRegs.addAll(Arrays.asList(argRegs));

        // Tratar os argumentos que vao nos registradores (A0 - A3)
        for(Temp reg : allArgRegs) {
            if (!escapes.hasNext())
                break;

            Access formal = allocLocal(escapes.next());
            this.formals.add(formal);

            actuals.add(new InReg(reg));

            if(formal instanceof InReg)
                offset += WORD_SIZE;
        }

        while (escapes.hasNext()) {
            this.formals.add(allocLocal(escapes.next()));
            actuals.add(new InFrame(offset));

            offset += WORD_SIZE;
        }
    }

    public Frame newFrame(Symbol symbol, List<Boolean> formals) {
        return newFrame(symbol.getId(), formals);
    }

    public Frame newFrame(String name, List<Boolean> formals) {
        if (this.label != null)
            name = this.label.name + "." + name;

        return new MipsFrame(name, formals);
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public Access allocLocal(boolean escape) {
        if (escape) {
            Access result = new InFrame(offset);
            offset += WORD_SIZE;
            return result;
        } else {
            return new InReg(new Temp());
        }
    }

    @Override public Temp FP() { return FP; }
    @Override public Temp RV() { return V0; }
    @Override public Access FPaccess() { return new InReg(FP); }
    @Override public Access RVaccess() { return new InReg(V0); }
    
    public Temp getArgReg(int i) {
        if(i < argRegs.length)
            return argRegs[i];

        return null;
    }



    public String toString() {
        String txt = "--------------------\nFrame: " + label + "\nformals\tactuals\n";

        for (int i = 0; i < formals.size(); i++) {
            String f = formals.get(i).toString();
            String a = actuals.get(i).toString();
            txt += f + "\t" + a + "\n";
        }

        txt += "--------------------\n";

        return txt;
    }
    
    

    @Override
    public String programEpilogue() {
        return
        "         .text            \n" +
        "         .globl _halloc   \n" +
        "_halloc:                  \n" +
        "         li $v0, 9        \n" +
        "         syscall          \n" +
        "         j $ra            \n" +
        "                          \n" +
        "         .text            \n" +
        "         .globl _printint \n" +
        "_printint:                \n" +
        "         li $v0, 1        \n" +
        "         syscall          \n" +
        "         la $a0, newl     \n" +
        "         li $v0, 4        \n" +
        "         syscall          \n" +
        "         j $ra            \n" +
        "                          \n" +
        "         .data            \n" +
        "         .align   0       \n" +
        "newl:    .asciiz \"\\n\"  \n" +
        "         .data            \n" +
        "         .align   0       \n" +
        "str_er:  .asciiz \" ERROR: abnormal termination\\n\" "+
        "                          \n" +
        "         .text            \n" +
        "         .globl _error    \n" +
        "_error:                   \n" +
        "         li $v0, 4        \n" +
        "         la $a0, str_er   \n" +
        "         syscall          \n" +
        "         li $v0, 10       \n" +
        "         syscall          \n" ;
    }
}
