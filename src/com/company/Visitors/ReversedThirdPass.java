package com.company.Visitors;

import com.company.TranslationUtil;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * Created by Andy on 30/08/2015.
 */
public class ReversedThirdPass extends CustomVisitor {

    public ReversedThirdPass() { super(); }

    @Override
    public boolean visit(VariableDeclarationStatement statement) {
        ASTNode block = TranslationUtil.getContainingBlock(statement);
        ListRewrite listRewrite = rewriter.getListRewrite(block.getParent(), Block.STATEMENTS_PROPERTY);

        listRewrite.remove(statement, null);
        listRewrite.insertFirst(ASTNode.copySubtree(statement.getAST(), statement), null);

        return true;
    }

    @Override
    public boolean visit(ExpressionStatement expressionStatement) {
        if (expressionStatement.getLocationInParent() == LabeledStatement.BODY_PROPERTY && ((LabeledStatement) expressionStatement.getParent()).getLabel().getFullyQualifiedName().equals("out_of_scope")) {
            MethodInvocation saveInvoc = (MethodInvocation)expressionStatement.getExpression();

            MethodInvocation restore = expressionStatement.getAST().newMethodInvocation();
            restore.setName(expressionStatement.getAST().newSimpleName("RESTORE"));

            Assignment assignment = expressionStatement.getAST().newAssignment();
            assignment.setLeftHandSide((Expression) ASTNode.copySubtree(expressionStatement.getAST(), (ASTNode) saveInvoc.arguments().get(0)));
            assignment.setOperator(Assignment.Operator.ASSIGN);
            assignment.setRightHandSide(restore);
            ExpressionStatement stmt = expressionStatement.getAST().newExpressionStatement(assignment);

            rewriter.set(expressionStatement.getParent(), LabeledStatement.BODY_PROPERTY, stmt, null);
        } else if (expressionStatement.getExpression() instanceof MethodInvocation && ((MethodInvocation)expressionStatement.getExpression()).getName().getFullyQualifiedName().equals("SAVE")) {
            ASTNode block = TranslationUtil.getContainingBlock(expressionStatement);
            ListRewrite listRewrite = rewriter.getListRewrite(block.getParent(), Block.STATEMENTS_PROPERTY);

            MethodInvocation restore = expressionStatement.getAST().newMethodInvocation();
            restore.setName(expressionStatement.getAST().newSimpleName("RESTORE"));

            Assignment assignment = expressionStatement.getAST().newAssignment();
            assignment.setLeftHandSide((Expression)ASTNode.copySubtree(expressionStatement.getAST(), (ASTNode)((MethodInvocation) expressionStatement.getExpression()).arguments().get(0)));
            assignment.setOperator(Assignment.Operator.ASSIGN);
            assignment.setRightHandSide(restore);
            ExpressionStatement stmt = expressionStatement.getAST().newExpressionStatement(assignment);
            listRewrite.replace(expressionStatement, stmt, null);
        }

        if (expressionStatement.getExpression() instanceof Assignment && TranslationUtil.isDestructive(((Assignment) expressionStatement.getExpression()).getOperator())) {
            ASTNode block = TranslationUtil.getContainingBlock(expressionStatement);
            ListRewrite listRewrite = rewriter.getListRewrite(block.getParent(), Block.STATEMENTS_PROPERTY);

            listRewrite.remove(expressionStatement, null);
        }

        return true;
    }
}
