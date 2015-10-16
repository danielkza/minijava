package visitor;

import minijava.node.*;
import symbol.Method;
import symbol.Symbol;
import symbol.Variable;

public class TypeCheckAnalysis extends BaseAnalysis {
    private TypeCheckExpAnalysis expChecker;

    public TypeCheckAnalysis() {
        super();
    }

    public TypeCheckAnalysis(BuildSymbolTableAnalysis analysis) {
        analysis.prepareChild(this);
    }

    public TypeCheckExpAnalysis getExpChecker() {
        if(expChecker == null) {
            expChecker = new TypeCheckExpAnalysis(this);
        }

        expChecker.setCurrClass(currClass);
        expChecker.setCurrMethod(currMethod);

        return expChecker;
    }

    @Override
    public void caseAProgram(AProgram node) {
        node.getMainClass().apply(this);
        applyAll(node.getClassDecl());
    }

    @Override
    public void caseAMainClass(AMainClass node) {
        String className = node.getName().toString();
        currClass = symbolTable.getClass(className);

        Method mainMethod = currClass.getMethod("main");
        startMethod(mainMethod, true);

        node.getMethodParameter().apply(this);
        node.getStatement().apply(this);

        endMethod(mainMethod);

        currClass = null;
    }

    @Override
    public void caseASimpleClassDecl(ASimpleClassDecl node) {
        String className = node.getName().toString();
        currClass = symbolTable.getClass(className);

        applyAll(node.getMethods());

        currClass = null;
    }

    @Override
    public void caseAExtendsClassDecl(AExtendsClassDecl node) {
        String className = node.getName().toString();
        currClass = symbolTable.getClass(className);

        applyAll(node.getMethods());

        currClass = null;
    }

    @Override
    public void caseAMethodDeclaration(AMethodDeclaration node) {
        Method method = currClass.getMethod(node.getName().toString());
        startMethod(method, false);

        applyAll(node.getStatements());

        TypeCheckExpAnalysis expChecker = getExpChecker();
        node.getReturnExpression().apply(expChecker);

        PType retExpType = expChecker.getType();
        if(!symbolTable.compareTypes(node.getReturnType(), retExpType)) {
            reportError(node.getReturnExpression(),
                        "Type mismatch in return statement: found %s, expected %s (in method %s::%s)",
                        retExpType, node.getReturnType(), currClass.getId(), currMethod.getId());
        }

        endMethod(method);
    }

    @Override
    public void caseAIfStatement(AIfStatement node) {
        TypeCheckExpAnalysis expChecker = getExpChecker();

        node.getCondition().apply(expChecker);
        PType conditionType = expChecker.getType();

        if (!(conditionType instanceof ABooleanType)) {
            reportError(node.getCondition(), "Type mismatch in if statement condition: found %s, expected boolean",
                        conditionType);
        }

        node.getTrueStatement().apply(this);
        node.getFalseStatement().apply(this);
    }

    @Override
    public void caseAWhileStatement(AWhileStatement node) {
        TypeCheckExpAnalysis expChecker = getExpChecker();
        node.getCondition().apply(expChecker);
        PType conditionType = expChecker.getType();
        if (!(conditionType instanceof ABooleanType)) {
            reportError(node.getCondition(), "Type mismatch in while loop condition: found %s, expected boolean",
                        conditionType);
        }

        node.getStatement().apply(this);
    }

    @Override
    public void caseAPrintlnStatement(APrintlnStatement node) {
        TypeCheckExpAnalysis expChecker = getExpChecker();
        node.getValue().apply(expChecker);
        PType valueType = expChecker.getType();
        if (!(valueType instanceof AIntType)) {
            reportError(node.getValue(), "Type mismatch in print statement: found %s, expected int",
                        valueType);
        }
    }

    @Override
    public void caseAAssignStatement(AAssignStatement node) {
        String varName = node.getName().toString();
        Variable variable = currMethod.getIdentifier(varName);
        if(variable == null) {
            reportError(node.getName(), "Undefined variable %s in assignment", varName);
        } else {
            TypeCheckExpAnalysis expChecker = getExpChecker();
            node.getValue().apply(expChecker);

            PType valueType = expChecker.getType();
            if(valueType == null)
                valueType = Symbol.identifierType("void");

            if (!getSymbolTable().compareTypes(variable.getType(), valueType)) {
                reportError(node.getValue(), "Type mismatch in assignment value: found %s, expected %s",
                            valueType, variable.getType());
            }
        }
    }

    @Override
    public void caseAArrayAssignStatement(AArrayAssignStatement node) {
        String varName = node.getName().toString();
        Variable variable = currMethod.getIdentifier(varName);
        if(variable == null) {
            reportError(node.getName(), "Undefined variable %s in array assignment", varName);
        } else {
            TypeCheckExpAnalysis expChecker = getExpChecker();

            node.getIndex().apply(expChecker);
            PType indexType = expChecker.getType();

            if (!(indexType instanceof AIntType)) {
                reportError(node.getIndex(), "Type mismatch in array assignment index: found %s, expected int",
                            indexType);
            }

            node.getValue().apply(expChecker);
            PType valueType = expChecker.getType();

            if (!getSymbolTable().compareTypes(variable.getType(), valueType)) {
                reportError(node.getValue(), "Type mismatch in array assignment value: found %s, expected %s",
                            valueType, variable.getType());
            }
        }
    }
}
