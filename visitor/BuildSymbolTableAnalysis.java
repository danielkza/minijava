package visitor;

import minijava.node.*;
import symbol.Method;
import symbol.Symbol;

import java.util.*;

public class BuildSymbolTableAnalysis extends BaseAnalysis {
    protected PType type;

    @Override
    public void caseAProgram(AProgram node) {
        node.getMainClass().apply(this);
        applyAll(node.getClassDecl());
    }

    protected void createClass(Node declaration, String className, String parentName,
                               List<PVariableDeclaration> variables,
                               List<PMethodDeclaration> methods)
    {
        if (!symbolTable.addClass(className, parentName)) {
            reportError(declaration, "Attempt to redefine class %s", className);
            return;
        }

        currentClass = symbolTable.getClass(className);
        for (PVariableDeclaration e : variables) {
            e.apply(this);
        }
        for (PMethodDeclaration e : methods) {
            e.apply(this);
        }
        currentClass = null;
    }

    @Override
    public void caseAMainClass(AMainClass node) {
        String className = node.getName().toString();
        symbolTable.addClass(className, null);
        currentClass = symbolTable.getClass(className);

        currentClass.addMethod("main", Symbol.identifierType("void"));

        Method mainMethod = currentClass.getMethod("main");
        mainMethod.addVar(node.getMethodParameter().toString(), Symbol.identifierType("String[]"));

        startMethod(mainMethod, true);

        node.getStatement().apply(this);

        endMethod(mainMethod);
        currentClass = null;
    }

    @Override
    public void caseASimpleClassDecl(ASimpleClassDecl node) {
        createClass(node, node.getName().toString(), null, node.getVariables(), node.getMethods());
    }

    @Override
    public void caseAExtendsClassDecl(AExtendsClassDecl node) {
        createClass(node, node.getName().toString(), node.getParent().toString(), node.getVariables(),
                node.getMethods());
    }

    @Override
    public void caseAVariableDeclaration(AVariableDeclaration node) {
        String varName = node.getName().toString();

        if(currentMethod != null) {
            if (!currentMethod.addVar(varName, node.getType())) {
                reportError(node, "Variable %s already defined in method %s::%s", varName,
                            currentClass.getId(), currentMethod.getId());
            }
        } else if (currentClass != null) {
            if (!currentClass.addVar(varName, node.getType())) {
                reportError(node, "Variable %s already defined in class %s", varName,
                            currentClass.getId());
            }
        }
    }

    @Override
    public void caseAMethodDeclaration(AMethodDeclaration node) {
        String methodName = node.getName().toString();
        if (!currentClass.addMethod(methodName, node.getReturnType())) {
            reportError(node, "Method %s already defined in class %s", methodName,
                        currentClass.getId());
        }

        Method method = currentClass.getMethod(methodName);
        startMethod(method, false);

        applyAll(node.getFormals());
        applyAll(node.getLocals());
        applyAll(node.getStatements());
        node.getReturnExpression().apply(this);

        endMethod(method);
    }

    @Override
    public void caseAFormalParameter(AFormalParameter node) {
        String paramName = node.getName().toString();
        if (!currentMethod.addParam(paramName, node.getType())) {
            reportError(node, "Parameter %s already defined in method %s::%s", paramName,
                        currentClass.getId(), currentMethod.getId());
        }
    }

    @Override
    public void caseABlockStatement(ABlockStatement node) {
        applyAll(node.getStatements());
    }
}
