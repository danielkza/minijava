package visitor;

import minijava.node.*;
import symbol.Method;
import symbol.Symbol;
import symbol.Variable;

import java.util.HashMap;
import java.util.Map;

public class TypeCheckAnalysis extends BaseAnalysis {
    private TypeCheckExpAnalysis expChecker;
    private Map<PExpression, PType> types = new HashMap<>();

    public TypeCheckAnalysis(BuildSymbolTableAnalysis analysis) {
        analysis.prepareChild(this);
    }

    public Map<PExpression, PType> getTypes() {
        return types;
    }
    
    public TypeCheckExpAnalysis getExpChecker() {
        if(expChecker == null) {
            expChecker = new TypeCheckExpAnalysis(this, types);
        }

        expChecker.setCurrentClass(currentClass);
        expChecker.setCurrentMethod(currentMethod);

        return expChecker;
    }
    
    private PType typeCheck(PExpression expr) {
        TypeCheckExpAnalysis expChecker = getExpChecker();
        expr.apply(expChecker);
        
        PType type = expChecker.getType();
        types.put(expr, type);
        
        return type;
    }

    @Override
    public void caseAProgram(AProgram node) {
        node.getMainClass().apply(this);
        applyAll(node.getClassDecl());
    }

    @Override
    public void caseAMainClass(AMainClass node) {
        String className = node.getName().toString();
        currentClass = symbolTable.getClass(className);

        Method mainMethod = currentClass.getMethod("main");
        startMethod(mainMethod, true);

        node.getMethodParameter().apply(this);
        node.getStatement().apply(this);

        endMethod(mainMethod);

        currentClass = null;
    }

    @Override
    public void caseASimpleClassDecl(ASimpleClassDecl node) {
        String className = node.getName().toString();
        currentClass = symbolTable.getClass(className);

        applyAll(node.getMethods());

        currentClass = null;
    }

    @Override
    public void caseAExtendsClassDecl(AExtendsClassDecl node) {
        String className = node.getName().toString();
        currentClass = symbolTable.getClass(className);

        applyAll(node.getMethods());

        currentClass = null;
    }

    @Override
    public void caseAMethodDeclaration(AMethodDeclaration node) {
        Method method = currentClass.getMethod(node.getName().toString());
        startMethod(method, false);

        applyAll(node.getStatements());

        PType retExpType = typeCheck(node.getReturnExpression());
        if(!symbolTable.compareTypes(node.getReturnType(), retExpType)) {
            reportError(node.getReturnExpression(),
                        "Type mismatch in return statement: found %s, expected %s (in method %s::%s)",
                        retExpType, node.getReturnType(), currentClass.getId(), currentMethod.getId());
        }

        endMethod(method);
    }

    @Override
    public void caseAIfStatement(AIfStatement node) {
        PType conditionType = typeCheck(node.getCondition());
        if (!(conditionType instanceof ABooleanType)) {
            reportError(node.getCondition(), "Type mismatch in if statement condition: found %s, expected boolean",
                        conditionType);
        }

        node.getTrueStatement().apply(this);
        node.getFalseStatement().apply(this);
    }

    @Override
    public void caseAWhileStatement(AWhileStatement node) {
        PType conditionType = typeCheck(node.getCondition());
        if (!(conditionType instanceof ABooleanType)) {
            reportError(node.getCondition(), "Type mismatch in while loop condition: found %s, expected boolean",
                        conditionType);
        }

        node.getStatement().apply(this);
    }

    @Override
    public void caseAPrintlnStatement(APrintlnStatement node) {
        PType valueType = typeCheck(node.getValue());
        if (!(valueType instanceof AIntType)) {
            reportError(node.getValue(), "Type mismatch in print statement: found %s, expected int",
                        valueType);
        }
    }

    @Override
    public void caseAAssignStatement(AAssignStatement node) {
        String varName = node.getName().toString();
        Variable variable = currentMethod.getIdentifier(varName);
        if(variable == null) {
            reportError(node.getName(), "Undefined variable %s in assignment", varName);
        } else {
            PType valueType = typeCheck(node.getValue());
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
        Variable variable = currentMethod.getIdentifier(varName);
        if(variable == null) {
            reportError(node.getName(), "Undefined variable %s in array assignment", varName);
        } else {
            PType arrayType = variable.getType();
            if (!(arrayType instanceof AIntArrayType)) {
                reportError(node.getIndex(), "Type mismatch in array assignment: found %s, expected int[]",
                            arrayType);
            }

            PType indexType = typeCheck(node.getIndex());
            if (!(indexType instanceof AIntType)) {
                reportError(node.getIndex(), "Type mismatch in array assignment index: found %s, expected int",
                            indexType);
            }

            PType valueType = typeCheck(node.getValue());
            // Cheat: we know that indexType is an Int, no need to create another instance
            if (!getSymbolTable().compareTypes(indexType, valueType)) {
                reportError(node.getValue(), "Type mismatch in array assignment value: found %s, expected %s",
                            valueType, variable.getType());
            }
        }
    }
}
