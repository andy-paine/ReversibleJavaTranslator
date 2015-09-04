package com.company;

import com.company.Visitors.CleanupVisitor;
import com.company.Visitors.CustomVisitor;
import com.company.Visitors.MethodVisitor;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Andy on 12/08/2015.
 */
public class TranslationUtil {

    public static ASTNode getContainingBlock(ASTNode node) {
        ASTNode block = node;
        while (block.getLocationInParent() != Block.STATEMENTS_PROPERTY)
            block = block.getParent();

        return block;
    }

    public static Document cleanupLabels(String path, List<String> methods) throws FileNotFoundException {
        methods = addReversedMethods(methods);

        String str = new Scanner(new File(path)).useDelimiter
                ("\\A").next();

        Document doc = new Document(str);

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(doc.get().toCharArray());

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        MethodVisitor methodVisitor = new MethodVisitor(methods);
        List<MethodDeclaration> methodDeclarations = methodVisitor.getMethodDeclarations(cu);

        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());

        CleanupVisitor visitor = new CleanupVisitor();
        visitor.setRewriter(rewriter);

        for (MethodDeclaration method: methodDeclarations)
            visitor.accept(method);

        TextEdit edits = rewriter.rewriteAST(doc, null);
        try {
            edits.apply(doc);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return doc;
    }

    public static NumberLiteral getBreakPointValue(ASTNode node) {
        Block block = (Block)node.getParent();
        Iterator it = block.statements().iterator();

        while (it.hasNext()) {
            Statement stmt = (Statement)it.next();
            if (stmt instanceof ExpressionStatement && ((ExpressionStatement) stmt).getExpression() instanceof Assignment
                    && ((Assignment) ((ExpressionStatement) stmt).getExpression()).getLeftHandSide() instanceof SimpleName
                    && ((SimpleName) ((Assignment) ((ExpressionStatement) stmt).getExpression()).getLeftHandSide()).getFullyQualifiedName().equals("broke_here"))
                return (NumberLiteral)((Assignment) ((ExpressionStatement) stmt).getExpression()).getRightHandSide();
        }

        return null;
    }

    public static List getPrefixConditions(Expression expression, List prefixList) {
        if (expression instanceof InfixExpression) {
            getPrefixConditions(((InfixExpression) expression).getRightOperand(), prefixList);
            getPrefixConditions(((InfixExpression) expression).getLeftOperand(), prefixList);
        } else if (expression instanceof PrefixExpression)
            prefixList.add(expression);

        return prefixList;
    }

    public static List getPostfixConditions(Expression expression, List postfixList) {
        if (expression instanceof InfixExpression) {
            getPostfixConditions(((InfixExpression) expression).getRightOperand(), postfixList);
            getPostfixConditions(((InfixExpression) expression).getLeftOperand(), postfixList);
        } else if (expression instanceof PostfixExpression)
            postfixList.add(expression);

        return postfixList;
    }

    public static List<VariableDeclarationStatement> getVariableDeclarationStatements(Block block) {
        List variableDeclarationStatements = new ArrayList<VariableDeclarationStatement>();
        Iterator it = block.statements().iterator();

        while (it.hasNext()) {
            Statement stmt = (Statement)it.next();
            if (stmt instanceof VariableDeclarationStatement)
                variableDeclarationStatements.add(stmt);
        }
        return variableDeclarationStatements;
    }

    public static Document visit(CustomVisitor visitor, Document source, List<String> methods) {
        methods = addReversedMethods(methods);

        Document returnDoc = new Document(source.get());

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(returnDoc.get().toCharArray());

        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        MethodVisitor methodVisitor = new MethodVisitor(methods);
        List<MethodDeclaration> methodDeclarations = methodVisitor.getMethodDeclarations(cu);

        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        visitor.setRewriter(rewriter);

        for (MethodDeclaration declaration: methodDeclarations)
            visitor.accept(declaration);

        TextEdit edits = rewriter.rewriteAST(returnDoc, null);
        try {
            edits.apply(returnDoc);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return returnDoc;
    }

    private static List<String> addReversedMethods(List<String> methods) {
        List<String> revMethods = new ArrayList<String>();
        for (String method: methods) {
            revMethods.add(method.toString());
            revMethods.add("rev_" + method.toString());
        }
        return revMethods;
    }

    public static void writeToFile(String filename, Document doc) {
        try {
            FileWriter output = new FileWriter(filename);
            output.write(doc.get());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isDestructive(Assignment.Operator op) {
        return op == Assignment.Operator.ASSIGN;
    }
}
