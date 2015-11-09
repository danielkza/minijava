package visitor;

import frame.Access;
import frame.InFrame;
import frame.InReg;
import tree.*;
import tree.node.*;
import frame.*;
import visitor.translate.*;

import java.util.*;

public class InterpreterVisitor implements ResultVisitor<Void> {
    public Iterator<Frag> frags;
    public HashMap<String, Integer> labels;
    public ArrayList<Command> program;
    public HashMap<String, Integer> temps;
    public ArrayList<Integer> memory;
    public int mp;
    public Stack<Integer> stack;
    public Stack<Integer> returns;
    public Access ret;
    

    public InterpreterVisitor(Iterator<Frag> frags) {
        this.frags = frags;
    }

    public void start() {
        labels = new HashMap<String, Integer>();
        program = new ArrayList<Command>();
        temps = new HashMap<String, Integer>();
        ret = null;
        
        while (frags.hasNext()) {
            Frag f = frags.next();
            if (f instanceof ProcFrag) {
                Frame frame = ((ProcFrag) f).frame;
                ret = frame.RVaccess();

                Stm body = ((ProcFrag) f).body;
                System.out.println(frame.toString());
                
                labels.put(frame.label.toString(), program.size());
                addCommand(body, new Command ("LABEL", frame.label.toString()));
                
                int numberOfParams = frame.formals.size();
                for (int i = numberOfParams - 1; i >= 0; i--) {
                    frame.Access access = frame.formals.get(i);
                    if(access instanceof InFrame) {
                        addCommand(body, new Command("CONST", access.toString()));
                        addCommand(body, new Command("MOVE", "MEM"));
                    } else {
                        String tempName = access.toString();
                        temps.put(tempName, 0);
                        addCommand(body, new Command("MOVE", tempName));
                    }                    
                }
                
                body.accept(this);
                addCommand(body, new Command("RETURN"));

            }
        }
        labels.put("EOP", program.size());
        program.add(new Command("LABEL", "EOP"));
        
        for(Command cmd : program)
            System.out.println(cmd);

        System.out.println("Running...");

        temps.put(ret.toString(), 0);
        stack = new Stack<Integer>();
        stack.push(0); // main() recebe um parâmetro
        returns = new Stack<Integer>();
        returns.push(program.size());
        memory = new ArrayList<Integer>();
        mp = 0;
        run();
    }

    protected void addCommand(Hospitable n, Command cmd) {
        cmd.tag = n.tag;
        program.add(cmd);
    }
    
    public int pp = 0;

    private void run() {
        while (pp < program.size()) {
            Command cmd = program.get(pp);
            cmd.run(this);
            pp++;
        }
        // for (int i = 0; i < memory.size(); i++)
        //   System.out.println(":" + i + " -> " + memory.get(i));
    }

    public Void visit(SEQ n) {
        n.left.accept(this);
        n.right.accept(this);
        return null;
    }

    public Void visit(LABEL n) {
        labels.put(n.label.toString(), program.size());
        addCommand(n, new Command("LABEL", n.label.toString()));
        return null;
    }

    public Void visit(JUMP n) {
        addCommand(n, new Command("JUMP", ((NAME) n.exp).label.toString()));
        return null;
    }

    public Void visit(CJUMP n) {
        n.left.accept(this);
        n.right.accept(this);
        addCommand(n, new Command("CJUMP", relop(n.relop), n.iftrue.toString(), n.iffalse.toString()));
        return null;
    }

    private String relop(int b) {
        switch (b) {
            case 0:
                return "EQ";
            case 2:
                return "LT";
            case 5:
                return "GE";
        }
        return "";
    }

    public Void visit(MOVE n) {
        n.src.accept(this);
        if (n.dst instanceof TEMP) {

            addCommand(n, new Command("MOVE", ((TEMP) n.dst).temp.toString()));
        } else if (n.dst instanceof MEM) {
            ((MEM) n.dst).exp.accept(this);

            addCommand(n, new Command("MOVE", "MEM"));
        }

        return null;
    }

    public Void visit(EXPR n) {
        n.exp.accept(this);
        addCommand(n, new Command("EXPR"));
        return null;
    }

    public Void visit(BINOP n) {
        n.left.accept(this);
        n.right.accept(this);
        addCommand(n, new Command("BINOP", binop(n.binop)));
        return null;
    }

    private String binop(int b) {
        switch (b) {
            case 0:
                return "PLUS";
            case 1:
                return "MINUS";
            case 2:
                return "MUL";
            case 4:
                return "AND";
        }
        return "";
    }

    public Void visit(MEM n) {
        n.exp.accept(this);
        addCommand(n, new Command("MEM"));
        return null;
    }

    public Void visit(TEMP n) {
        addCommand(n, new Command("TEMP", n.temp.toString()));
        return null;
    }

    public Void visit(ESEQ n) {
        n.stm.accept(this);
        n.exp.accept(this);
        return null;
    }

    public Void visit(NAME n) {
        labels.put(n.label.toString(), program.size());
        addCommand(n, new Command("LABEL", n.label.toString()));
        return null;
    }

    public Void visit(CONST n) {
        addCommand(n, new Command("CONST", String.valueOf(n.value)));
        return null;
    }

    public Void visit(CALL n) {
        for (Exp e : n.args)
            e.accept(this);
        
        addCommand(n, new Command("CALL", ((NAME) n.func).label.toString(), String.valueOf(n.args.size())));
        return null;
    }

}

class Command {
    public String command;
    public String param1;
    public String param2;
    public String param3;
    public String tag;

    Command(String cmd, String p1, String p2, String p3) {
        command = cmd;
        param1 = p1;
        param2 = p2;
        param3 = p3;
    }

    Command(String cmd, String p1, String p2) {
        command = cmd;
        param1 = p1;
        param2 = p2;
    }

    Command(String cmd, String p1) {
        command = cmd;
        param1 = p1;
    }

    Command(String cmd) {
        command = cmd;
    }

    public void run(InterpreterVisitor v) {
        //System.out.println(this);
        
        if (command.equals("RETURN")) {
            v.pp = v.returns.pop();
            v.stack.push(v.temps.get(v.ret.toString()).intValue());
        } else if (command.equals("CONST")) {
            v.stack.push(Integer.parseInt(param1));
        } else if (command.equals("MOVE")) {
            if (param1.equals("MEM")) {
                int address = v.stack.pop().intValue();
                int value = v.stack.pop().intValue();
                v.memory.set(address / 4, value);
            } else {
                v.temps.put(param1, v.stack.pop().intValue());
            }
        } else if (command.equals("MEM")) {
            int address = v.stack.pop().intValue();
            // System.out.println("***************** " + address + " -> " + v.memory.get(address/4).intValue());
            // for (int i = 0; i < v.memory.size(); i++)
            //   System.out.println(":" + i + " -> " + v.memory.get(i).intValue());
            v.stack.push(v.memory.get(address / 4).intValue());
        } else if (command.equals("BINOP")) {
            int r = v.stack.pop().intValue();
            int l = v.stack.pop().intValue();
            if (param1.equals("PLUS")) {
                v.stack.push(l + r);
            } else if (param1.equals("MINUS")) {
                v.stack.push(l - r);
            } else if (param1.equals("MUL")) {
                v.stack.push(l * r);
            }
        } else if (command.equals("CJUMP")) {
            int r = v.stack.pop().intValue();
            int l = v.stack.pop().intValue();
            if (param1.equals("LT")) {
                if (l < r) {
                    v.pp = v.labels.get(param2);
                } else {
                    v.pp = v.labels.get(param3);
                }
            } else if (param1.equals("EQ")) {
                if (l == r) {
                    v.pp = v.labels.get(param2);
                } else {
                    v.pp = v.labels.get(param3);
                }
            } else if (param1.equals("GE")) {
                if (l >= r) {
                    v.pp = v.labels.get(param2);
                } else {
                    v.pp = v.labels.get(param3);
                }
            }
        } else if (command.equals("TEMP")) {
            v.stack.push(v.temps.get(param1).intValue());
        } else if (command.equals("JUMP")) {
            v.pp = v.labels.get(param1);
        } else if (command.equals("CALL")) {
            if (param1.equals("_printint")) {
                System.out.println(v.stack.pop());
                v.stack.push(0);
            } else if (param1.equals("_halloc")) {
                int arraySize = v.stack.pop().intValue() / 4;
                for (int i = 0; i < arraySize; i++)
                    v.memory.add(0);
                v.stack.push(v.mp * 4);
                v.mp += arraySize;
            } else if (param1.equals("_error")) {
                System.out.println("ERRO: Acessando array fora dos limites!");
                v.stack.push(0);
            } else {
                v.returns.push(v.pp);
                v.pp = v.labels.get(param1);
            }
        }
    }

    public String toString() {
        List<String> params = new ArrayList<>();
        String reg = param1;
        params.add(param1);
        if(param2 != null) params.add(param2);
        if(param3 != null) params.add(param3);
        
        return String.format("%-6s %-34s %s", command,
                             Arrays.toString(params.toArray()), tag);
    }
}

