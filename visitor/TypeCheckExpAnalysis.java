package visitor;

import minijava.node.*;
import symbol.*;

import java.util.Map;

public class TypeCheckExpAnalysis extends BaseAnalysis {
    protected PType type;
    protected Map<PExpression, PType> types;

    public TypeCheckExpAnalysis(TypeCheckAnalysis analysis,
                                Map<PExpression, PType> types) {
        analysis.prepareChild(this);
        this.types = types;
    }

    public PType getType() {
        return type;
    }
    
    private PType typeCheck(PExpression expr) {
        expr.apply(this);
        types.put(expr, type);

        PType result = type;
        type = null;
        return result;
    }
    
    protected boolean checkIntBinaryExpression(
        PExpression expr, PExpression left, PExpression right, String operator)
    {
        boolean ok = true;

        PType leftType = typeCheck(left);
        if (!(leftType instanceof AIntType)) {
            reportError(left, "Type mismatch in left of %s expression: found %s, expected int", operator,
                        leftType);
            ok = false;
        }

        PType rightType = typeCheck(right);
        if (!(rightType instanceof AIntType)) {
            reportError(right, "Type mismatch in right of %s expression: found %s, expected int", operator,
                        rightType);
            ok = false;
        }

        type = new AIntType(new TInt());
        types.put(expr, type);
        return ok;
    }

    protected boolean checkBooleanBinaryExpression(
        PExpression expr, PExpression left, PExpression right, String operator)
    {
        boolean ok = true;
        
        PType leftType = typeCheck(left);
        if (!(leftType instanceof ABooleanType)) {
            reportError(left, "Type mismatch in left side of %s expression: found %s, expected boolean", operator, leftType);
            ok = false;
        }

        PType rightType = typeCheck(right);
        if (!(rightType instanceof ABooleanType)) {
            reportError(right, "Type mismatch in left side of %s expression: found %s, expected boolean", operator, rightType);
            ok = false;
        }

        type = new ABooleanType(new TBoolean());
        types.put(expr, type);
        return ok;
    }

    @Override
    public void caseAAndExpression(AAndExpression expr) {
        checkBooleanBinaryExpression(expr, expr.getLeft(), expr.getRight(), "AND");
    }

    @Override
    public void caseALessThanExpression(ALessThanExpression expr) {
        if(checkIntBinaryExpression(expr, expr.getLeft(), expr.getRight(), "LESS")) {
            type = new ABooleanType(new TBoolean());
            types.put(expr, type);
        }
    }

    @Override
    public void caseAPlusExpression(APlusExpression expr) {
        checkIntBinaryExpression(expr, expr.getLeft(), expr.getRight(), "PLUS");
    }

    @Override
    public void caseAMinusExpression(AMinusExpression expr) {
        checkIntBinaryExpression(expr, expr.getLeft(), expr.getRight(), "MINUS");
    }

    @Override
    public void caseATimesExpression(ATimesExpression expr) {
        checkIntBinaryExpression(expr, expr.getLeft(), expr.getRight(), "TIMES");
    }

    protected boolean checkArrayExpression(PExpression expr) {
        PType exprType = typeCheck(expr);

        type = new AIntArrayType(new TInt());
        types.put(expr, type);
        
        if(!(exprType instanceof AIntArrayType)) {
            reportError(expr, "Type mismatch in array reference: found %s, expected int[]",
                        exprType);
            return false;
        }

        return true;
    }
    protected boolean checkIndexExpression(PExpression expr) {
        PType exprType = typeCheck(expr);
        
        type = new AIntType(new TInt());
        types.put(expr, type);

        if(!(exprType instanceof AIntType)) {
            reportError(expr, "Type mismatch in array index: found %s, expected int",
                        exprType);
            return false;
        }

        return true;
    }

    @Override
    public void caseAArrayLookupExpression(AArrayLookupExpression expr) {
        checkArrayExpression(expr.getArray());
        checkIndexExpression(expr.getIndex());

        type = new AIntType(new TInt());
        types.put(expr, type);
    }

    @Override
    public void caseAArrayLengthExpression(AArrayLengthExpression expr) {
        checkArrayExpression(expr.getArray());
        type = new AIntType(new TInt());
        types.put(expr, type);
    }

    @Override
    public void caseACallExpression(ACallExpression expr) {
        PType instanceType = typeCheck(expr.getInstance());
        String methodName = expr.getName().toString();
        if(!(instanceType instanceof AIdentifierType)) {
            reportError(expr.getInstance(),
                        "Type mismatch in method call: found %s, expected instance of a class",
                        instanceType);
            type = null;
        } else {
            String className = Symbol.cleanId(
                ((AIdentifierType)instanceType).getName().toString());
            ClassS cls = symbolTable.getClass(className);
            Method method = cls.getMethodInHierarchy(methodName);
    
            if(method == null) {
                reportError(expr, "Unknown method %s for class %s", methodName, className);
                type = null;
            } else {
                applyAll(expr.getActuals());                
                type = method.getReturnType();
            }
        }
        
        types.put(expr, type);        
    }

    @Override
    public void caseAIntegerExpression(AIntegerExpression expr) {
        type = new AIntType(new TInt());
        types.put(expr, type);
    }

    @Override
    public void caseATrueExpression(ATrueExpression expr) {
        type = new ABooleanType(new TBoolean());
        types.put(expr, type);
    }

    @Override
    public void caseAFalseExpression(AFalseExpression expr) {
        type = new ABooleanType(new TBoolean());
        types.put(expr, type);
    }

    @Override
    public void caseAIdentifierExpression(AIdentifierExpression expr) {
        String id = Symbol.cleanId(expr.getName().toString());
        Variable var = currentMethod.getIdentifier(id);

        if(var == null) {
            reportError(expr, "Unknown identifier %s", id);
            type = new AIntType(new TInt());
        } else {
            type = var.getType();
        }
        
        types.put(expr, type);
    }

    @Override
    public void caseAThisExpression(AThisExpression expr) {
        type = currentClass.getType();
        types.put(expr, type);
    }

    @Override
    public void caseANewArrayExpression(ANewArrayExpression expr) {
        type = new AIntArrayType(new TInt());
        types.put(expr, type);
    }

    @Override
    public void caseANewObjectExpression(ANewObjectExpression expr) {
        String className = Symbol.cleanId(expr.getClassName().toString());
        ClassS cls = symbolTable.getClass(className);

        if(cls == null) {
            reportError(expr, "Unknown class %s", className);
            type = null;
        } else {
            type = cls.getType();
        }
        
        types.put(expr, type);
    }

    @Override
    public void caseANotExpression(ANotExpression expr) {
        PType exprType = typeCheck(expr.getExpression());
        if(exprType == null)
            exprType = Symbol.identifierType("void");

        if(!(exprType instanceof ABooleanType)) {
            reportError(expr.getExpression(),
                        "Type mismatch in NOT expression: found %s, expected boolean",
                        exprType);
        }
        
        type = new ABooleanType(new TBoolean());
        types.put(expr, type);
    }
}
