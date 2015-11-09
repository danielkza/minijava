package visitor.translate;

import minijava.node.*;

import frame.Temp;
import frame.Label;
import frame.Frame;
import symbol.*;
import tree.Exp;
import tree.Hospitable;
import tree.Stm;
import tree.node.BINOP;
import tree.node.CJUMP;
import tree.node.ESEQ;
import tree.node.SEQ;
import visitor.BaseAnalysis;

import java.util.*;

public class Translate extends BaseAnalysis {
    private Frame globalFrame;
    private SymbolTable symbolTable;
    private Map<PExpression, PType> types;
    
    private Frame currentClassFrame;
    private Frame currentMethodFrame;
    
    private tree.Exp currentExpression;
    private tree.Stm currentStatement;
    private Stack<Node> nodeStack = new Stack<>();
    
    private Map<Symbol, Frame> frames = new HashMap<>();
    private Map<Variable, frame.Access> variableAccesses = new HashMap<>();
    private List<Frag> frags = new LinkedList<>();

    public Translate(Frame globalFrame, SymbolTable symbolTable,
                     Map<PExpression, PType> types) {
        this.globalFrame = globalFrame;
        this.symbolTable = symbolTable;
        this.types = types;
        
        for(ClassS cls : this.symbolTable.getClasses().values()) {
            if(!frames.containsKey(cls))
                frames.put(cls, parseClass(cls));
        }
    }
        
    public List<Frag> getResults() {
        return Collections.unmodifiableList(frags);
    }
    
    private Frame parseClass(ClassS cls) {
        ClassS baseClass = cls.getBaseClass();
        Frame baseFrame;
        int offset;
        
        if(baseClass != null) {
            baseFrame = frames.get(baseClass);
            if(baseFrame == null)
                baseFrame = parseClass(baseClass);
            
            offset = baseFrame.getOffset();
        } else {
            baseFrame = globalFrame;
            offset = 0;
        }
        
        Frame frame = baseFrame.newFrame(cls);
        frame.setOffset(offset);
        
        for (Variable v : cls.getVars().values()) {
            variableAccesses.put(v, frame.allocLocal(true));
        }
        
        for (Method method : cls.getMethods().values()) {
            frames.put(method, parseMethod(method, cls, frame));
        }
        
        return frame;
    }

    private Frame parseMethod(Method method, ClassS cls, Frame classFrame) {
        int numParams = method.getNumParams() + 1; // + this
        Frame frame = classFrame.newFrame(method,
            Collections.nCopies(numParams, false));        
        Iterator<frame.Access> formals = frame.formals.iterator();
        
        variableAccesses.put(cls.getThisVar(), formals.next());
        for(Variable p : method.getParams()) {
            variableAccesses.put(p, formals.next());
        }
        
        for(Variable v : method.getVars().values()) {
            variableAccesses.put(v, frame.allocLocal(false));
        }
        
        return frame;
    }
    
    private tree.Exp asExp(Hospitable e) {
        if(e instanceof tree.Exp) {
            return (tree.Exp)e;
        } else {
            return null;
        }
    }
    
    private tree.Stm asStm(Hospitable e) {
        if(e instanceof tree.Stm) {
            return (tree.Stm)e;
        } else if (e instanceof tree.Exp) {
            tree.Stm s = EXPR((tree.Exp)e);
            s.tag = e.tag;
            return s;
        } else {
            return null;
        }
    }
    
    private tree.Exp visitExp(Node node) {
        currentStatement = null;
        currentExpression = null;
        
        node.apply(this);
        tree.Exp exp = asExp(currentExpression);
        if(exp == null) {
            fail(node, "Expected expression");
            return null;
        }
        
        currentStatement = null;
        currentExpression = null;
        return exp;
    }
    
    
    private tree.Stm visitStm(Node node) {
        currentStatement = null;
        currentExpression = null;
        
        node.apply(this);
        tree.Stm stm;
        
        if(currentStatement != null)
            stm = currentStatement;
        else 
            stm = asStm(currentExpression);
        
        if(stm == null) {
            fail(node, "Expected statement");
            return null;
        }
        
        currentStatement = null;
        currentExpression = null;
        return stm;
    }
    
    private tree.Stm visitStms(List<PStatement> statements) {
        tree.Stm body = null;
        for (PStatement stm : statements) {
            tree.Stm next = visitStm(stm);
            body = SEQ(body, next);
        }
        return body;
    }
    
    public void procEntryExit(tree.Exp exp, Frame frame) {
        tree.Exp bodyExp = asExp(exp);        
        ProcFrag frag = new ProcFrag(MOVE(TEMP(frame.RV()), bodyExp), frame);
        frags.add(frag);
    }

    public void procEntryExit(tree.Stm stm, Frame frame) {
        ProcFrag frag = new ProcFrag(stm, frame);
        frags.add(frag);
    }
    
    private void setTag(Hospitable h, String tag) {
        if(h == null || tag == null || h.tag != null)
            return;
        
        h.tag = tag;
        if(h instanceof SEQ) {
            SEQ s = (SEQ) h;
            setTag(s.left, tag);
            setTag(s.right, tag);
        } else if(h instanceof ESEQ) {
            ESEQ e = (ESEQ) h;
            setTag(e.exp, tag);
            setTag(e.stm, tag);
        } else {
            List<Exp> kids = null;
            if(h instanceof Exp) {
                kids = ((Exp) h).kids();
            } else if(h instanceof Stm) {
                kids = ((Stm) h).kids();
            } else {
                return;
            }
            
            for(Exp kid : kids)
                setTag(kid, tag);
        }
    }
    
    @Override
    public void defaultOut(@SuppressWarnings("unused") Node node) {
        String tag = input.nodeLine(node);
        setTag(currentExpression, tag);
        setTag(currentStatement, tag);
    }
    
    @Override
    public void caseAProgram(AProgram node) {
        defaultIn(node);
        
        node.getMainClass().apply(this);
        DataFrag frag = new DataFrag(globalFrame.programEpilogue());
        frags.add(frag);
        
        for (PClassDecl e : node.getClassDecl()) {
            e.apply(this);
        }
        
        defaultOut(node);        
    }

    @Override
    public void caseAMainClass(AMainClass node) {
        defaultIn(node);
        
        currentClass = symbolTable.getClass(node.getName().toString());
        currentClassFrame = frames.get(currentClass);
        currentMethod = currentClass.getMethod("main");
        currentMethodFrame = frames.get(currentMethod);
        
        procEntryExit(visitStm(node.getStatement()), currentMethodFrame);
        
        currentMethodFrame = null;
        currentMethod = null;
        currentClassFrame = null;
        currentClass = null;
    
        defaultOut(node);
    }
    
    @Override
    public void caseASimpleClassDecl(ASimpleClassDecl node) {
        defaultIn(node);
        
        currentClass = symbolTable.getClass(node.getName().toString());
        currentClassFrame = frames.get(currentClass);
        
        for (PMethodDeclaration e : node.getMethods()) {
            e.apply(this);
        }
        
        currentClassFrame = null;
        currentClass = null;
    
        defaultOut(node);
    }
    
    @Override
    public void caseAMethodDeclaration(AMethodDeclaration node) {
        defaultIn(node);
        
        String metName = Symbol.cleanId(node.getName().toString());
        
        currentMethod = currentClass.getMethod(metName);
        currentMethodFrame = frames.get(currentMethod);
        
        tree.Stm body = visitStms(node.getStatements());
        tree.Exp retExp = visitExp(node.getReturnExpression());
        procEntryExit(ESEQ(body, retExp), currentMethodFrame);
    
        currentMethodFrame = null;
        currentMethod = null;
    
        defaultOut(node);
    }

    @Override
    public void caseAPrintlnStatement(APrintlnStatement node) {
        defaultIn(node);
        
        tree.Exp e = visitExp(node.getValue());
        List<tree.Exp> args1 = Arrays.asList(e);
        currentStatement = MOVE(
            TEMP(new Temp()),
            CALL(NAME(new Label("_printint")), args1)
        );
    
        defaultOut(node);
    }

    @Override
    public void caseAIntegerExpression(AIntegerExpression node) {
        defaultIn(node);
        
        int value = Integer.parseInt(Symbol.cleanId(node.getValue().toString()));
        currentExpression = CONST(value);
    
        defaultOut(node);
    }    
        
    private tree.Exp thisExpression(Node expr) {
        if(currentMethodFrame == null) {
            fail(expr, "`this` used in invalid context");
        } else if(currentMethodFrame.formals.isEmpty()) {
            fail(expr, "`this` used in frame with no parameters");
        }
        
        frame.Access access = variableAccesses.get(currentClass.getThisVar());
        return access.exp(TEMP(currentMethodFrame.FP()));
    }
    
   
    private tree.Exp identifier(Node expr, String name) {
        if(currentMethod == null) {
            fail(expr, "Identifier reference outside of method");
        }
        
        String varName = Symbol.cleanId(name);
        Variable var = currentMethod.getIdentifier(varName);
        if(var == null) {
            fail(expr, "Unknown variable %s", varName);
            return null;
        }
        
        frame.Access access = variableAccesses.get(var);
        if(var.getParent() instanceof ClassS) {
            return access.exp(thisExpression(expr));
        } else {
            return access.exp(TEMP(currentMethodFrame.FP()));
        }
    }
    
    @Override
    public void caseAIdentifierExpression(AIdentifierExpression node) {
        defaultIn(node);
        
        currentExpression = identifier(node, node.getName().toString());
    
        defaultOut(node);
    }
    
    @Override
    public void caseAIdentifier(AIdentifier node) {
        defaultIn(node);
        
        currentExpression = identifier(node, node.getName().toString());
    
        defaultOut(node);
    }

    @Override
    public void caseAThisExpression(AThisExpression node) {
        defaultIn(node);
        
        currentExpression = thisExpression(node);
    
        defaultOut(node);
    }
    
    @Override
    public void caseAAssignStatement(AAssignStatement node) {
        defaultIn(node);
        
        tree.Exp lvalue = visitExp(node.getName());
        tree.Exp rvalue = visitExp(node.getValue());
        currentStatement = MOVE(lvalue, rvalue);
    
        defaultOut(node);
    }
    
    @Override
    public void caseAPlusExpression(APlusExpression node) {
        defaultIn(node);
        
        tree.Exp expl = visitExp(node.getLeft());
        tree.Exp expr = visitExp(node.getRight());
        currentExpression = BINOP(BINOP.PLUS, expl, expr);
    
        defaultOut(node);
    }

    @Override
    public void caseAMinusExpression(AMinusExpression node) {
        defaultIn(node);
        
        tree.Exp expl = visitExp(node.getLeft());
        tree.Exp expr = visitExp(node.getRight());
        currentExpression = BINOP(BINOP.MINUS, expl, expr);
    
        defaultOut(node);
    }

    @Override
    public void caseATimesExpression(ATimesExpression node) {
        defaultIn(node);
        
        tree.Exp expl = visitExp(node.getLeft());
        tree.Exp expr = visitExp(node.getRight());
        currentExpression = BINOP(BINOP.MUL, expl, expr);
    
        defaultOut(node);
    }

    @Override
    public void caseAAndExpression(AAndExpression node) {
        defaultIn(node);
        
        Temp t1 = new Temp();
        Label done = new Label();
        Label ok1 = new Label();
        Label ok2 = new Label();
        tree.Exp left = visitExp(node.getLeft());
        tree.Exp right = visitExp(node.getRight());

        currentExpression = ESEQ(
            seq(
                MOVE(TEMP(t1), CONST(0)),
                CJUMP(tree.node.CJUMP.EQ, left, CONST(1), ok1, done),
                LABEL(ok1),
                CJUMP(tree.node.CJUMP.EQ, right, CONST(1), ok2, done),
                LABEL(ok2),
                MOVE(TEMP(t1), CONST(1)),
                LABEL(done)
            ),
            TEMP(t1)
        );
    
        defaultOut(node);
    }

    @Override
    public void caseALessThanExpression(ALessThanExpression node) {
        defaultIn(node);
        
        Temp t1 = new Temp();
        Label done = new Label();
        Label trueL = new Label();
        tree.Exp left = visitExp(node.getLeft());
        tree.Exp right = visitExp(node.getRight());

        currentExpression = ESEQ(
            seq(
                MOVE(TEMP(t1), CONST(0)),
                CJUMP(CJUMP.LT, left, right, trueL, done),
                LABEL(trueL),
                MOVE(TEMP(t1), CONST(1)),
                LABEL(done)
            ),
            TEMP(t1)
        );
    
        defaultOut(node);
    }

    @Override
    public void caseANotExpression(ANotExpression node) {
        defaultIn(node);
        
        tree.Exp e = visitExp(node.getExpression());
        currentExpression = BINOP(BINOP.MINUS, CONST(1), e);
    
        defaultOut(node);
    }

    @Override
    public void caseATrueExpression(ATrueExpression node) {
        defaultIn(node);
        
        currentExpression = CONST(1);
    
        defaultOut(node);
    }

    @Override
    public void caseAFalseExpression(AFalseExpression node) {
        defaultIn(node);
        
        currentExpression = CONST(0);
    
        defaultOut(node);
    }

    @Override
    public void caseAIfStatement(AIfStatement node) {
        defaultIn(node);
        
        Label T = new Label();
        Label F = new Label();
        Label D = new Label();
        tree.Exp exp = visitExp(node.getCondition());
        tree.Stm stmT = visitStm(node.getTrueStatement());
        tree.Stm stmF = visitStm(node.getFalseStatement());
        currentStatement = seq(
            CJUMP(CJUMP.EQ, exp, CONST(1), T, F),
            LABEL(T),
            stmT,
            JUMP(D),
            LABEL(F),
            stmF,
            LABEL(D)
        );
    
        defaultOut(node);
    }

    @Override
    public void caseAWhileStatement(AWhileStatement node) {
        defaultIn(node);
        
        Label check = new Label();
        Label body = new Label();
        Label done = new Label();
        
        tree.Exp condition = visitExp(node.getCondition());
        tree.Stm bodyStm = visitStm(node.getStatement());
        currentStatement = seq(
            LABEL(check),
            CJUMP(CJUMP.EQ, condition, CONST(1), body, done),
            LABEL(body),
            bodyStm,
            JUMP(check),
            LABEL(done)
        );
    
        defaultOut(node);
    }

    @Override
    public void caseABlockStatement(ABlockStatement node) {
        defaultIn(node);
        
        currentStatement = visitStms(node.getStatements());
    
        defaultOut(node);
    }

    @Override
    public void caseANewArrayExpression(ANewArrayExpression node) {
        defaultIn(node);
        
        Temp addr = new Temp();
        Temp pos = new Temp();
        Label check = new Label();
        Label done = new Label();
        Label body = new Label();

        tree.Exp numElems = visitExp(node.getSize());
        tree.Exp totalElems = BINOP(tree.node.BINOP.PLUS, numElems, CONST(1));
        tree.Exp size = BINOP(tree.node.BINOP.MUL, totalElems, CONST(4));

        // 1. call _halloc get pointer to space allocated in t1
        List<tree.Exp> args1 = Arrays.asList(size);
        
        tree.Stm getAddr = MOVE(TEMP(addr),
                                CALL(NAME(new Label("_halloc")), args1));

        // 2.Initialization
        tree.Stm init = seq(
            MOVE(TEMP(pos), CONST(4)),
            LABEL(check),
            CJUMP(tree.node.CJUMP.LT, TEMP(pos), size, body, done),
            LABEL(body),
            MOVE(MEM(BINOP(tree.node.BINOP.PLUS, TEMP(addr), TEMP(pos))),
                 CONST(0)),
            MOVE(TEMP(pos), BINOP(tree.node.BINOP.PLUS, TEMP(pos), CONST(4))),
            JUMP(check),
            LABEL(done),
            MOVE(MEM(TEMP(addr)), BINOP(tree.node.BINOP.MUL, numElems, CONST(4)))
        );

        currentExpression = ESEQ(SEQ(getAddr, init), TEMP(addr));
    
        defaultOut(node);
    }

    private tree.Exp arrayReference(tree.Exp array, tree.Exp index) {
        Temp tIndex = new Temp();
        Temp tSize = new Temp();

        Label F = new Label();
        Label T = new Label();

        List<tree.Exp> args1 = Collections.emptyList();

        tree.Stm checkIndex = seq(
            MOVE(TEMP(tIndex), BINOP(tree.node.BINOP.MUL, index, CONST(4))),
            MOVE(TEMP(tSize), MEM(array)),
            CJUMP(tree.node.CJUMP.GE, TEMP(tIndex), TEMP(tSize), T, F),
            LABEL(T),
            MOVE(TEMP(new Temp()), CALL(NAME(new Label("_error")), args1)),
            LABEL(F)
        );
        
        tree.Exp base = BINOP(BINOP.PLUS, array, CONST(4));
        tree.Exp offset = BINOP(BINOP.MUL, index, CONST(4));
        
        Temp t = new Temp();
        tree.Stm calc = seq(
            checkIndex,
            MOVE(TEMP(t), MEM(BINOP(BINOP.PLUS, base, offset)))
        );

        return ESEQ(calc, TEMP(t));
    }

    @Override
    public void caseAArrayLookupExpression(AArrayLookupExpression node) {
        defaultIn(node);
        
        tree.Exp array = visitExp(node.getArray());
        tree.Exp index = visitExp(node.getIndex());
        currentExpression = arrayReference(array, index);
    
        defaultOut(node);
    }
    
    
    @Override
    public void caseAArrayAssignStatement(AArrayAssignStatement node) {
        defaultIn(node);
        
        tree.Exp array = identifier(node, node.getName().toString());
        tree.Exp index = visitExp(node.getIndex());
        tree.Exp value = visitExp(node.getValue());
        
        if (!(array instanceof tree.node.TEMP)) {
            Temp tAux = new Temp();
            array = ESEQ(MOVE(TEMP(tAux), array), TEMP(tAux));
        }
        
        Temp offset = new Temp();
        Temp size = new Temp();
        Label error = new Label();
        Label ok = new Label();
        
        List<tree.Exp> args=  Collections.emptyList();
        
        tree.Stm calcOffset = seq(
            MOVE(TEMP(offset), BINOP(tree.node.BINOP.MUL, index, CONST(4))),
            MOVE(TEMP(size), MEM(array)),
            CJUMP(CJUMP.GE, TEMP(offset), TEMP(size), error, ok),
            LABEL(error),
            asStm(CALL(NAME(new Label("_error")), args)),
            LABEL(ok)
        );
        
        currentStatement = seq(
            calcOffset,
            MOVE(TEMP(offset),
                 BINOP(tree.node.BINOP.PLUS, TEMP(offset), CONST(4))),
            MOVE(TEMP(offset), BINOP(BINOP.PLUS, array, TEMP(offset))),
            MOVE(MEM(TEMP(offset)), value)
        );
    
        defaultOut(node);
    }
    

    @Override
    public void caseANewObjectExpression(ANewObjectExpression node) {
        defaultIn(node);
        
        String className = Symbol.cleanId(node.getClassName().toString());
        ClassS cls = symbolTable.getClass(className);
        if(cls == null) {
            reportError(node, "Unknown class %s", className);
            return;
        }
        
        // Allocate something even if the class has no members
        int size = Math.max(4, frames.get(cls).getOffset());
        Temp addr = new Temp();
        Temp pos = new Temp();
        Label check = new Label();
        Label body = new Label();
        Label done = new Label();
        
        List<tree.Exp> args1 = Arrays.asList(CONST(size));
        
        tree.Stm getAddr = MOVE(TEMP(addr),
                                CALL(NAME(new Label("_halloc")), args1));

        // 2.Initialization
        tree.Stm init = seq(
            MOVE(TEMP(pos), CONST(0)),
            LABEL(check),
            CJUMP(tree.node.CJUMP.LT, TEMP(pos), CONST(size), body, done),
            LABEL(body),
            MOVE(MEM(BINOP(tree.node.BINOP.PLUS, TEMP(addr), TEMP(pos))),
                 CONST(0)),
            MOVE(TEMP(pos), BINOP(tree.node.BINOP.PLUS, TEMP(pos), CONST(4))),
            JUMP(check),
            LABEL(done)
        );

        currentExpression = ESEQ(SEQ(getAddr, init), TEMP(addr));
    
        defaultOut(node);
    }

    @Override
    public void caseACallExpression(ACallExpression node) {
        defaultIn(node);
        
        tree.Exp instanceExp = visitExp(node.getInstance());
        PType instanceType = types.get(node.getInstance());
        if(!(instanceType instanceof AIdentifierType)) {
            fail(node, "Call instance is not an object");
            return;
        }
        
        String className = ((AIdentifierType)instanceType).getName().toString();
        String methodName = node.getName().toString();
        ClassS cls = symbolTable.getClass(className);
        Method method = cls.getMethodInHierarchy(methodName);
        Frame callMethodFrame = frames.get(method);
 
        List<tree.Exp> actuals = new LinkedList<>();
        Iterator<Variable> formals = method.getParams().iterator();
        
        for(PExpression argExp : node.getActuals()) {
            if(!formals.hasNext()) {
                fail(argExp, "Too many arguments for method call");
                return;
            }
            
            Variable formal = formals.next();
            PType argType = types.get(argExp);
            
            if(!symbolTable.compareTypes(formal.getType(), argType)) {
                fail(argExp,
                    "Type mismatch in method argument: found %s, expected instance %s",
                    argType, formal.getType());
                return;
            }
            
            tree.Exp exp = visitExp(argExp);
            actuals.add(exp);
        }
        
        if(formals.hasNext()) {
            fail(node, "Insufficient arguments for method call");
            return;
        }
        
        actuals.add(0, instanceExp);
        currentExpression = CALL(NAME(callMethodFrame.label), actuals);
    
        defaultOut(node);
    }

    private static tree.Exp CONST(int value) {
        return new tree.node.CONST(value);
    }

    private static tree.Exp NAME(Label label) {
        return new tree.node.NAME(label);
    }

    private static tree.Exp TEMP(Temp temp) {
        return new tree.node.TEMP(temp);
    }

    private static tree.Exp BINOP(int binop, tree.Exp left, tree.Exp right) {
        return new tree.node.BINOP(binop, left, right);
    }

    private static tree.Exp MEM(tree.Exp exp) {
        return new tree.node.MEM(exp);
    }

    private static tree.Exp CALL(tree.Exp func, List<tree.Exp> args) {
        return new tree.node.CALL(func, args);
    }

    private static tree.Exp ESEQ(tree.Stm stm, tree.Exp exp) {
        if (stm == null) return exp;
        return new tree.node.ESEQ(stm, exp);
    }

    private static tree.Stm MOVE(tree.Exp dst, tree.Exp src) {
        return new tree.node.MOVE(dst, src);
    }

    private static tree.Stm EXPR(tree.Exp exp) {
        return new tree.node.EXPR(exp);
    }

    private static tree.Stm JUMP(Label target) {
        return new tree.node.JUMP(target);
    }

    private static tree.Stm CJUMP(int relop, tree.Exp l, tree.Exp r, Label t,
                                  Label f) {
        return new tree.node.CJUMP(relop, l, r, t, f);
    }

    private static tree.Stm SEQ(tree.Stm left, tree.Stm right) {
        if (left == null)
            return right;
        if (right == null)
            return left;
        return new tree.node.SEQ(left, right);
    }

    private static tree.Stm seq(tree.Stm... statements) {
        tree.Stm stm = null;
        for(tree.Stm next : statements) {
            stm = SEQ(stm, next);
        }
        return stm;
    }
    
    private static tree.Stm LABEL(Label label) {
        return new tree.node.LABEL(label);
    }

}
