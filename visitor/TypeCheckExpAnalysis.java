package visitor;

import minijava.node.*;

public class TypeCheckExpAnalysis extends BaseAnalysis {
    protected PType type;

    public TypeCheckExpAnalysis() {
        super();
    }

    public TypeCheckExpAnalysis(TypeCheckAnalysis analysis) {
        analysis.prepareChild(this);
    }

    public PType getType() {
        return type;
    }

    protected boolean checkIntBinaryExpression(PExpression left, PExpression right, String operator) {
        boolean ok = true;
        left.apply(this);
        if (!(type instanceof AIntType)) {
            reportError(left, "Type mismatch in left of %s expression: found %s, expected int", operator,
                        type);
            ok = false;
        }

        right.apply(this);
        if (!(type instanceof AIntType)) {
            reportError(right, "Type mismatch in right of %s expression: found %s, expected int", operator,
                        type);
            ok = false;
        }

        type = new AIntType(new TInt());

        return ok;
    }

    protected boolean checkBooleanBinaryExpression(Node left, Node right, String operator) {
        boolean ok = true;
        left.apply(this);
        if (!(type instanceof ABooleanType)) {
            reportError(left, "Type mismatch in left side of %s expression: found %s, expected boolean", operator);
            ok = false;
        }

        right.apply(this);
        if (!(type instanceof ABooleanType)) {
            reportError(right, "Type mismatch in left side of %s expression: found %s, expected boolean", operator);
            ok = false;
        }

        type = new ABooleanType(new TBoolean());

        return ok;
    }

    @Override
    public void caseAAndExpression(AAndExpression node) {
        checkBooleanBinaryExpression(node.getLeft(), node.getRight(), "AND");
    }

    @Override
    public void caseALessThanExpression(ALessThanExpression node) {
        if(checkIntBinaryExpression(node.getLeft(), node.getRight(), "LESS")) {
            type = new ABooleanType(new TBoolean());
        }
    }

    @Override
    public void caseAPlusExpression(APlusExpression node) {
        checkIntBinaryExpression(node.getLeft(), node.getRight(), "PLUS");
    }

    @Override
    public void caseAMinusExpression(AMinusExpression node) {
        checkIntBinaryExpression(node.getLeft(), node.getRight(), "MINUS");
    }

    @Override
    public void caseATimesExpression(ATimesExpression node) {
        checkIntBinaryExpression(node.getLeft(), node.getRight(), "TIMES");
    }

    protected boolean checkArrayExpression(PExpression expression) {
        expression.apply(this);

        if(!(type instanceof AIntArrayType)) {
            reportError(expression, "Type mismatch in array reference: found %s, expected int[]",
                        type.toString());
            type = new AIntArrayType(new TInt());
            return false;
        }

        return true;
    }
    protected boolean checkIndexExpression(PExpression expression) {
        expression.apply(this);

        if(!(type instanceof AIntType)) {
            reportError(expression, "Type mismatch in array index: found %s, expected int",
                        type.toString());
            type = new AIntType(new TInt());
            return false;
        }

        return true;
    }

    @Override
    public void caseAArrayLookupExpression(AArrayLookupExpression node) {
        checkArrayExpression(node.getArray());
        checkIndexExpression(node.getIndex());

        type = new AIntType(new TInt());
    }

    @Override
    public void caseAArrayLengthExpression(AArrayLengthExpression node) {
        checkArrayExpression(node.getArray());
        type = new AIntType(new TInt());
    }

    @Override
    public void caseACallExpression(ACallExpression node) {
        node.getInstance().apply(this);

        String methodName = node.getName().toString();
        if(!(type instanceof AIdentifierType)) {
            reportError(node.getInstance(), "Type mismatch in method call: found %s, expected instance of a class",
                        type.toString());
            type = null;
            return;
        }

        String className = ((AIdentifierType) type).getName().toString();
        Class cls = symbolTable.getClass(className);
        Method method = cls.getMethodInHierarchy(methodName);

        if(method == null) {
            reportError(node, "Unknown method %s for class %s", methodName, className);
            type = null;
            return;
        }

        type = method.getReturnType();
    }

    @Override
    public void caseAIntegerExpression(AIntegerExpression node) {
        type = new AIntType(new TInt());
    }

    @Override
    public void caseATrueExpression(ATrueExpression node) {
        type = new ABooleanType(new TBoolean());
    }

    @Override
    public void caseAFalseExpression(AFalseExpression node) {
        type = new ABooleanType(new TBoolean());
    }

    @Override
    public void caseAIdentifierExpression(AIdentifierExpression node) {
        String id = node.getName().toString();
        Variable var = currMethod.getIdentifier(id);

        if(var == null) {
            reportError(node, "Unknown identifier %s", id);
            type = new AIntType(new TInt());
        } else {
            type = var.getType();
        }
    }

    @Override
    public void caseAThisExpression(AThisExpression node) {
        type = currClass.getType();
    }

    @Override
    public void caseANewArrayExpression(ANewArrayExpression node) {
        type = new AIntArrayType(new TInt());
    }

    @Override
    public void caseANewObjectExpression(ANewObjectExpression node) {
        String className = node.getClassName().toString();
        Class cls = symbolTable.getClass(className);

        if(cls == null) {
            reportError(node, "Unknown class %s", className);
            type = null;
        } else {
            type = cls.getType();
        }
    }

    @Override
    public void caseANotExpression(ANotExpression node) {
        node.getExpression().apply(this);

        PType expType = type;
        if(expType == null)
            expType = Symbol.identifierType("void");

        if(!(type instanceof ABooleanType)) {
            reportError(node, "Type mismatch in NOT expression: found %s, expected boolean",
                        expType.toString());
            type = new ABooleanType(new TBoolean());
        }
    }
}
