package frame.mips;

import java.util.*;

import frame.*;
import symbol.Symbol;

public class MipsFrame extends Frame {
    public static final int WORD_SIZE = 4;
    private static Map<String, Integer> functions = new HashMap<>();

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

        // Tratar o resto dos argumentos
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

    public Access allocLocal(boolean escape) {
        if (escape) {
            Access result = new InFrame(offset);
            offset += WORD_SIZE;
            return result;
        } else {
            return new InReg(new Temp());
        }
    }

    static final Temp
        ZERO = new Temp(), // zero reg
        AT = new Temp(), // reserved for assembler
        V0 = new Temp(), // function result
        V1 = new Temp(), // second function result
        A0 = new Temp(), // argument1
        A1 = new Temp(), // argument2
        A2 = new Temp(), // argument3
        A3 = new Temp(), // argument4
        T0 = new Temp(), // caller-saved
        T1 = new Temp(),
        T2 = new Temp(),
        T3 = new Temp(),
        T4 = new Temp(),
        T5 = new Temp(),
        T6 = new Temp(),
        T7 = new Temp(),
        S0 = new Temp(), // callee-saved
        S1 = new Temp(),
        S2 = new Temp(),
        S3 = new Temp(),
        S4 = new Temp(),
        S5 = new Temp(),
        S6 = new Temp(),
        S7 = new Temp(),
        T8 = new Temp(), // caller-saved
        T9 = new Temp(),
        K0 = new Temp(), // reserved for OS kernel
        K1 = new Temp(), // reserved for OS kernel
        GP = new Temp(), // pointer to global area
        SP = new Temp(), // stack pointer
        S8 = new Temp(), // callee-save (frame pointer)
        RA = new Temp(); // return address

    // Register lists: must not overlap and must include every register that
    // might show up in code
    private static final Temp[]
        // registers dedicated to special purposes
        specialRegs = { ZERO, AT, K0, K1, GP, SP },
        // registers to pass outgoing arguments
        argRegs = { A0, A1, A2, A3 },
        // registers that a callee must preserve for its caller
        calleeSaves = { RA, S0, S1, S2, S3, S4, S5, S6, S7, S8 },
        // registers that a callee may use without preserving
        callerSaves = { T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, V0, V1 };

    private static final Map<Temp, String> tempMap;
    private static final Map<String, String> regnameMap;

    static {
        tempMap = new HashMap<>(32);
        tempMap.put(ZERO, "$0");
        tempMap.put(AT,   "$at");
        tempMap.put(V0,   "$v0");
        tempMap.put(V1,   "$v1");
        tempMap.put(A0,   "$a0");
        tempMap.put(A1,   "$a1");
        tempMap.put(A2,   "$a2");
        tempMap.put(A3,   "$a3");
        tempMap.put(T0,   "$t0");
        tempMap.put(T1,   "$t1");
        tempMap.put(T2,   "$t2");
        tempMap.put(T3,   "$t3");
        tempMap.put(T4,   "$t4");
        tempMap.put(T5,   "$t5");
        tempMap.put(T6,   "$t6");
        tempMap.put(T7,   "$t7");
        tempMap.put(S0,   "$s0");
        tempMap.put(S1,   "$s1");
        tempMap.put(S2,   "$s2");
        tempMap.put(S3,   "$s3");
        tempMap.put(S4,   "$s4");
        tempMap.put(S5,   "$s5");
        tempMap.put(S6,   "$s6");
        tempMap.put(S7,   "$s7");
        tempMap.put(T8,   "$t8");
        tempMap.put(T9,   "$t9");
        tempMap.put(K0,   "$k0");
        tempMap.put(K1,   "$k1");
        tempMap.put(GP,   "$gp");
        tempMap.put(SP,   "$sp");
        tempMap.put(S8,   "$fp");
        tempMap.put(RA,   "$ra");

        regnameMap = new HashMap<>(32);
        for(Map.Entry<Temp, String> entry : tempMap.entrySet())
            regnameMap.put(entry.getKey().toString(), entry.getValue());
    }

    public String tempMap(Temp temp) {
        return tempMap.get(temp);
    }

    private String regName(String reg) {
        String regName = regnameMap.get(reg);
        return regName != null ? regName : reg;
    }

    public String toString() {
        String txt = "--------------------\nFrame: " + label + "\nformals\tactuals\n";

        for (int i = 0; i < formals.size(); i++) {
            String f = formals.get(i).toString();
            String a = actuals.get(i).toString();
            txt += regName(f) + "\t" + regName(a) + "\n";
        }

        txt += "--------------------\n";

        return txt;
    }
}
