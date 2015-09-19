package visitor;

import minijava.node.*;
import minijava.analysis.*;

public class PrettyPrinter extends DepthFirstAdapter {
  private int tab = 0;

  private void printTab() {
    for (int i = 0; i < tab; i++)
      System.out.print("  ");
  }

  private void printNode(Node n) {
    printTab();
    String klass = n.getClass().toString();
    System.out.print(klass.substring(klass.lastIndexOf('.') + 1).replaceAll("\\s+",""));
  }

  private void print(String s) {
    printTab();
    System.out.println(s);
  }

  public void defaultOut(Node node) {
    tab--;
  }

  public void inAProgram(AProgram node) {
    tab++;
    printNode(node);
    print("");
  }

  public void inAMainClass(AMainClass node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getName());
  }

  public void inAExtendsClassDecl(AExtendsClassDecl node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getName());
  }

  public void inASimpleClassDecl(ASimpleClassDecl node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getName());
  }

  public void inAMethodDeclaration(AMethodDeclaration node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getName());
  }

  public void inAVariableDeclaration(AVariableDeclaration node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getName());
  }

  public void inAFormalParameter(AFormalParameter node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getName());
  }

  public void inAIntType(AIntType node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAIntArrayType(AIntArrayType node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inABooleanType(ABooleanType node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getName());
  }

  public void inAIdentifierType(AIdentifierType node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getName());
  }

  public void inABlockStatement(ABlockStatement node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAIfStatement(AIfStatement node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAWhileStatement(AWhileStatement node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAPrintlnStatement(APrintlnStatement node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAAssignStatement(AAssignStatement node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAArrayAssignStatement(AArrayAssignStatement node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAAndExpression(AAndExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inALessThanExpression(ALessThanExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAPlusExpression(APlusExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAMinusExpression(AMinusExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inATimesExpression(ATimesExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAArrayLookupExpression(AArrayLookupExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAArrayLengthExpression(AArrayLengthExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inACallExpression(ACallExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAIntegerExpression(AIntegerExpression node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getValue());
  }

  public void inATrueExpression(ATrueExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAFalseExpression(AFalseExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAIdentifierExpression(AIdentifierExpression node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getName());
  }

  public void inAThisExpression(AThisExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inANewArrayExpression(ANewArrayExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inANewObjectExpression(ANewObjectExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inANotExpression(ANotExpression node) {
    tab++;
    printNode(node);
    System.out.println();
  }

  public void inAIdentifier(AIdentifier node) {
    tab++;
    printNode(node);
    System.out.println(": " + node.getName());
  }

}
