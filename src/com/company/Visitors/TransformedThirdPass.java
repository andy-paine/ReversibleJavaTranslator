package com.company.Visitors;

import com.company.TranslationUtil;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andy on 01/09/2015.
 */
public class TransformedThirdPass extends CustomVisitor{

    public TransformedThirdPass() {
        super();
    }

    /**********************************************************
     Every time a variable drops out of scope, SAVE it
     *********************************************************/
    @Override
    public boolean visit(Block block) {
        Iterator it = block.statements().iterator();
        ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);

        while (it.hasNext()) {
            Statement stmt = (Statement) it.next();
            /* if the statement is a declaration, that variable will drop out of scope */
            if (stmt instanceof VariableDeclarationStatement) {
                /* move declaration to the top of it's scope */
                listRewrite.insertFirst(ASTNode.copySubtree(block.getAST(), stmt), null);
                listRewrite.remove(stmt, null);
                /* create SAVE statement for variable */
                MethodInvocation methodInvocation = block.getAST().newMethodInvocation();
                methodInvocation.setName(block.getAST().newSimpleName("SAVE"));
                methodInvocation.arguments().add(ASTNode.copySubtree(block.getAST(), ((VariableDeclarationFragment) ((VariableDeclarationStatement) stmt).fragments().get(0)).getName()));
                ExpressionStatement methodStmt = block.getAST().newExpressionStatement(methodInvocation);

                /* label the statement for special handling in break statements */
                LabeledStatement labeledStatement = block.getAST().newLabeledStatement();
                labeledStatement.setLabel(block.getAST().newSimpleName("out_of_scope"));
                labeledStatement.setBody(methodStmt);

                listRewrite.insertLast(labeledStatement, null);
            }
        }

        return true;
    }

    /**********************************************************
     Save all variables before a destructive assignment is performed
     *********************************************************/
    @Override
    public boolean visit(Assignment assignment) {
        Block block = (Block)TranslationUtil.getContainingBlock(assignment).getParent();
        /* if the assignment is not part of a while loop or if statement */
        if (!(block.getLocationInParent() == LabeledStatement.BODY_PROPERTY && (((LabeledStatement)block.getParent()).getLabel().getFullyQualifiedName().equals("while_loop")
                || ((LabeledStatement)block.getParent()).getLabel().getFullyQualifiedName().equals("if_statement")))) {
            /* if the assignment is a destructive method */
            if (TranslationUtil.isDestructive(assignment.getOperator())) {
                /* create and insert a SAVE for it */
                ASTNode containingBlock = TranslationUtil.getContainingBlock(assignment);
                MethodInvocation methodInvocation = assignment.getAST().newMethodInvocation();
                methodInvocation.setName(assignment.getAST().newSimpleName("SAVE"));
                methodInvocation.arguments().add(ASTNode.copySubtree(assignment.getAST(), assignment.getLeftHandSide()));
                ExpressionStatement stmt = assignment.getAST().newExpressionStatement(methodInvocation);

                ListRewrite listRewrite = rewriter.getListRewrite(containingBlock.getParent(), Block.STATEMENTS_PROPERTY);
                listRewrite.insertBefore(stmt, containingBlock, null);
            }
        }

        return true;
    }

    /**********************************************************
     Collects all variables that would drop out of scope if break
     were to execute and saves them
     *********************************************************/
    @Override
    public boolean visit(BreakStatement breakStatement) {
        ASTNode statementScope = TranslationUtil.getContainingBlock(breakStatement);
        ListRewrite listRewrite = rewriter.getListRewrite(statementScope.getParent(), Block.STATEMENTS_PROPERTY);

        List<VariableDeclarationStatement> declarationStatementList = new ArrayList<VariableDeclarationStatement>();

        /* while we have not reached the label which we break from */
        while (!(statementScope.getParent().getLocationInParent() == LabeledStatement.BODY_PROPERTY
                && ((LabeledStatement) statementScope.getParent().getParent()).getLabel().getFullyQualifiedName().equals(breakStatement.getLabel().getFullyQualifiedName()))) {
            /* add all variable declarations in this scope then move up one scope level */
            declarationStatementList.addAll(TranslationUtil.getVariableDeclarationStatements((Block) statementScope.getParent()));
            statementScope = TranslationUtil.getContainingBlock(statementScope.getParent());
        }

        /* add all the variable declarations from the top level */
        declarationStatementList.addAll(TranslationUtil.getVariableDeclarationStatements((Block) statementScope.getParent()));

        /* for each declaration, create a SAVE for it */
        for (VariableDeclarationStatement stmt: declarationStatementList) {
            MethodInvocation methodInvocation = breakStatement.getAST().newMethodInvocation();
            methodInvocation.setName(breakStatement.getAST().newSimpleName("SAVE"));
            methodInvocation.arguments().add(ASTNode.copySubtree(breakStatement.getAST(), ((VariableDeclarationFragment) stmt.fragments().get(0)).getName()));
            ExpressionStatement exp = breakStatement.getAST().newExpressionStatement(methodInvocation);

            /* label this SAVE as we need it later */
            LabeledStatement labeledStatement = breakStatement.getAST().newLabeledStatement();
            labeledStatement.setLabel(breakStatement.getAST().newSimpleName("break_save"));
            labeledStatement.setBody(exp);
            listRewrite.insertLast(labeledStatement, null);
        }

        listRewrite.remove(breakStatement, null);
        listRewrite.insertLast(ASTNode.copySubtree(breakStatement.getAST(), breakStatement), null);

        return super.visit(breakStatement);
    }

    /**********************************************************
     Moves return statements to the end of their blocks
     *********************************************************/
    @Override
    public void endVisit(ReturnStatement returnStatement) {
        ASTNode block = TranslationUtil.getContainingBlock(returnStatement);
        ListRewrite listRewrite = rewriter.getListRewrite(block.getParent(), Block.STATEMENTS_PROPERTY);

        listRewrite.remove(returnStatement, null);
        listRewrite.insertLast(ASTNode.copySubtree(returnStatement.getAST(), returnStatement), null);
    }
}
