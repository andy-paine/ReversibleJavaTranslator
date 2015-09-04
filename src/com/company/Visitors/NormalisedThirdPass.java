package com.company.Visitors;

import com.company.TranslationUtil;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andy on 13/08/2015.
 */
public class NormalisedThirdPass extends CustomVisitor {

    public NormalisedThirdPass () {
        super();
    }

    /**********************************************************
     Adds a new variable to assign to each method invocation so that
     all method invocations are of the form lvalue = methodInvoc();
     *********************************************************/
    @Override
    public boolean visit(MethodInvocation methodInvocation) {
        /* if the method is not in a singular assignment or a standalone invocation */
        if (methodInvocation.getLocationInParent() != Assignment.RIGHT_HAND_SIDE_PROPERTY && methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
            SimpleName name = methodInvocation.getAST().newSimpleName(methodInvocation.getName().getFullyQualifiedName() + "_var");

            /* create a variable of type object to hold the return value of the method */
            VariableDeclarationFragment frag = methodInvocation.getAST().newVariableDeclarationFragment();
            frag.setName(name);
            frag.setInitializer((Expression) ASTNode.copySubtree(methodInvocation.getAST(), methodInvocation));
            VariableDeclarationStatement statement = methodInvocation.getAST().newVariableDeclarationStatement(frag);
            statement.setType(methodInvocation.getAST().newSimpleType(methodInvocation.getAST().newName("Object")));

            ASTNode containingBlock = TranslationUtil.getContainingBlock(methodInvocation);

            /* insert the new variable declaration before the invocation and replace the invocation with the variable */
            ListRewrite listRewrite = rewriter.getListRewrite(containingBlock.getParent(), Block.STATEMENTS_PROPERTY);
            listRewrite.insertBefore(statement, containingBlock, null);
            if (methodInvocation.getLocationInParent().isChildListProperty()) {
                listRewrite = rewriter.getListRewrite(methodInvocation.getParent(), (ChildListPropertyDescriptor) methodInvocation.getLocationInParent());
                listRewrite.replace(methodInvocation, ASTNode.copySubtree(methodInvocation.getAST(), name), null);
            } else {
                SimpleName nameCopy = (SimpleName) ASTNode.copySubtree(methodInvocation.getAST(), name);
                rewriter.set(methodInvocation.getParent(), methodInvocation.getLocationInParent(), nameCopy, null);
            }
        }

        return true;
    }

    /**********************************************************
     Pulls out any postfix expressions that are not standalone
     and creates a new statement to increment them after they are used
     *********************************************************/
    @Override
    public boolean visit(PostfixExpression postfixExpression) {
        /* if the postfix expression is not standalone */
        if (postfixExpression.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
            Expression exp = (Expression)ASTNode.copySubtree(postfixExpression.getAST(), postfixExpression);
            ExpressionStatement statement = postfixExpression.getAST().newExpressionStatement(exp);

            /* insert the new expression after the variable is used */
            ASTNode block = TranslationUtil.getContainingBlock(postfixExpression);
            ListRewrite listRewrite = rewriter.getListRewrite(block.getParent(), Block.STATEMENTS_PROPERTY);
            listRewrite.insertAfter(statement, block, null);

            /* update the expression to only reference the variable */
            if (postfixExpression.getLocationInParent().isChildListProperty()) {
                listRewrite = rewriter.getListRewrite(postfixExpression.getParent(), (ChildListPropertyDescriptor) postfixExpression.getLocationInParent());
                listRewrite.replace(postfixExpression, postfixExpression.getOperand(), null);
            } else {
                rewriter.set(postfixExpression.getParent(), postfixExpression.getLocationInParent(), postfixExpression.getOperand(), null);
            }
        }

        return true;
    }

    /**********************************************************
     Pulls out any prefix expressions that are not standalone
     and creates a new statement to increment them before they are used
     *********************************************************/
    @Override
    public boolean visit(PrefixExpression prefixExpression) {
        /* if the prefix expression is not standalone */
        if (prefixExpression.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
            Expression exp = (Expression)ASTNode.copySubtree(prefixExpression.getAST(), prefixExpression);
            ExpressionStatement statement = prefixExpression.getAST().newExpressionStatement(exp);

            /* insert the new expression before the variable is used */
            ASTNode block = TranslationUtil.getContainingBlock(prefixExpression);
            ListRewrite listRewrite = rewriter.getListRewrite(block.getParent(), Block.STATEMENTS_PROPERTY);
            listRewrite.insertBefore(statement, block, null);

            /* update the expression to only reference the variable */
            if (prefixExpression.getLocationInParent().isChildListProperty()) {
                listRewrite = rewriter.getListRewrite(prefixExpression.getParent(), (ChildListPropertyDescriptor) prefixExpression.getLocationInParent());
                listRewrite.replace(prefixExpression, prefixExpression.getOperand(), null);
            } else {
                rewriter.set(prefixExpression.getParent(), prefixExpression.getLocationInParent(), prefixExpression.getOperand(), null);
            }
        }

        return true;
    }

    /**********************************************************
     Pulls out side-effects (post- and pre- fix) from any
     while statement conditions and inserts them before/after
     *********************************************************/
    @Override
    public boolean visit(WhileStatement whileStatement) {
        ASTNode block = TranslationUtil.getContainingBlock(whileStatement);
        ListRewrite listRewrite = rewriter.getListRewrite(block.getParent(), Block.STATEMENTS_PROPERTY);

        /* get all prefix conditions */
        List prefixExpressions = TranslationUtil.getPrefixConditions(whileStatement.getExpression(), new ArrayList<Expression>());
        Iterator it = prefixExpressions.iterator();
        /* for each condition, insert the standalone statement before and update the condition to use the variable */
        while (it.hasNext()) {
            PrefixExpression exp = ((PrefixExpression) it.next());
            ExpressionStatement stmt = whileStatement.getAST().newExpressionStatement((Expression) ASTNode.copySubtree(whileStatement.getAST(), exp));

            listRewrite.insertBefore(stmt, block, null);
            rewriter.set(exp.getParent(), exp.getLocationInParent(), ASTNode.copySubtree(exp.getAST(), exp.getOperand()), null);
        }

        listRewrite = rewriter.getListRewrite(whileStatement.getBody(), Block.STATEMENTS_PROPERTY);

        /* get all the postfix conditions */
        List postfixExpressions = TranslationUtil.getPostfixConditions(whileStatement.getExpression(), new ArrayList<Expression>());
        it = postfixExpressions.iterator();
        /* for each condition, insert the standalone statement after and update the condition to use the variable */
        while (it.hasNext()) {
            PostfixExpression exp = ((PostfixExpression) it.next());
            ExpressionStatement stmt = whileStatement.getAST().newExpressionStatement((Expression) ASTNode.copySubtree(whileStatement.getAST(), exp));

            listRewrite.insertFirst(stmt, null);
            rewriter.set(exp.getParent(), exp.getLocationInParent(), ASTNode.copySubtree(exp.getAST(), exp.getOperand()), null);
        }

        return true;
    }

    /**********************************************************
     Creates new variables for returning and tracking where
     the execution broke/jumped. Creates a block to break from
     *********************************************************/
    @Override
    public boolean visit(MethodDeclaration methodDeclaration) {
        if (methodDeclaration.getReturnType2() != null && (!methodDeclaration.getReturnType2().isPrimitiveType() || ((PrimitiveType)methodDeclaration.getReturnType2()).getPrimitiveTypeCode() != PrimitiveType.VOID)) {

            /* create variable declaration for return variable */
            SimpleName returnName = methodDeclaration.getAST().newSimpleName("return_var");
            VariableDeclarationFragment declarationFragment = methodDeclaration.getAST().newVariableDeclarationFragment();
            declarationFragment.setName(returnName);
            VariableDeclarationStatement variableDeclarationStatement = methodDeclaration.getAST().newVariableDeclarationStatement(declarationFragment);
            variableDeclarationStatement.setType(((Type) ASTNode.copySubtree(methodDeclaration.getAST(), methodDeclaration.getReturnType2())));

            /* create variable declaration to track where break occurs */
            VariableDeclarationFragment breakDeclarationFragment = methodDeclaration.getAST().newVariableDeclarationFragment();
            breakDeclarationFragment.setName(methodDeclaration.getAST().newSimpleName("broke_here"));
            VariableDeclarationStatement breakVariableDeclarationStatement = methodDeclaration.getAST().newVariableDeclarationStatement(breakDeclarationFragment);
            breakVariableDeclarationStatement.setType(methodDeclaration.getAST().newSimpleType(methodDeclaration.getAST().newName("Integer")));

            /* create a labelled block to break from when returning */
            Block methodBlock = methodDeclaration.getAST().newBlock();
            LabeledStatement labeledStatement = methodDeclaration.getAST().newLabeledStatement();
            SimpleName breakName = methodDeclaration.getAST().newSimpleName("return_label");
            labeledStatement.setLabel(breakName);

            labeledStatement.setBody((Block) ASTNode.copySubtree(methodDeclaration.getAST(), methodDeclaration.getBody()));

            /* create final return statement */
            ReturnStatement ret = methodDeclaration.getAST().newReturnStatement();
            ret.setExpression((Expression) ASTNode.copySubtree(methodDeclaration.getAST(), returnName));

            methodBlock.statements().add(variableDeclarationStatement);
            methodBlock.statements().add(breakVariableDeclarationStatement);
            methodBlock.statements().add(labeledStatement);
            methodBlock.statements().add(ret);
            rewriter.set(methodDeclaration, MethodDeclaration.BODY_PROPERTY, methodBlock, null);
        }
        return true;
    }
}
