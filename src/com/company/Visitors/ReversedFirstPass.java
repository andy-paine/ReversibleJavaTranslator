package com.company.Visitors;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Andy on 12/08/2015.
 */
public class ReversedFirstPass extends CustomVisitor {

    public ReversedFirstPass() {
        super();
    }

    /**********************************************************
     Rename all methods declarations to rev_*methodname* and set their
     return type to void. Get reverse of all statements in the body
     *********************************************************/
    @Override
    public boolean visit (MethodDeclaration methodDeclaration) {
        /* get the reversed order of statements from the body */
        Block body = (Block) ASTNode.copySubtree(methodDeclaration.getAST(), getReverse(methodDeclaration.getBody()));

        /* set new body, name and return type */
        SimpleName name = methodDeclaration.getAST().newSimpleName("rev_" + methodDeclaration.getName().getFullyQualifiedName());
        rewriter.set(methodDeclaration, MethodDeclaration.BODY_PROPERTY, body, null);
        rewriter.set(methodDeclaration, MethodDeclaration.NAME_PROPERTY, name, null);
        rewriter.set(methodDeclaration, MethodDeclaration.RETURN_TYPE2_PROPERTY, methodDeclaration.getAST().newPrimitiveType(PrimitiveType.VOID), null);

        return true;
    }

    public ASTNode getReverse(Block block) {
        /* create a replacement block */
        Block ret = block.getAST().newBlock();
        /* create a list to hold the reversed statements */
        List retStatements = new ArrayList();

        /* starting from the end, get each statement in the block */
        ListIterator it = block.statements().listIterator(block.statements().size());
        while (it.hasPrevious()) {
            Statement stmt = (Statement)it.previous();
            /* uses reflection to get the relevant getReverse call for the type of stmt */
            try {
                Method getReverse = ReversedFirstPass.class.getMethod("getReverse", stmt.getClass());
                ASTNode node = (ASTNode)getReverse.invoke(this, stmt);
                /* add the reverse to the list */
                if (node!=null)
                    retStatements.add(node);
            } catch (Exception ex) {
                /* if no getReverse method exists for this type, just copy the statement verbose */
                retStatements.add(ASTNode.copySubtree(block.getAST(), stmt));
            }
        }
        /* add all the reversed statements to the list then return it */
        ret.statements().addAll(retStatements);
        return ret;
    }

    public ASTNode getReverse(ExpressionStatement node) {
        ExpressionStatement ret = ((ExpressionStatement) ASTNode.copySubtree(node.getAST(), node));
        try {
            /* uses reflection to get the reverse of the node's expression*/
            Expression expression = node.getExpression();
            Method getReverse = ReversedFirstPass.class.getMethod("getReverse", expression.getClass());
            ASTNode rev = (ASTNode)getReverse.invoke(this, expression);
            if (node!=null)
                ret.setExpression((Expression)rev);
        } catch (Exception ex) { }

        return ret;
    }

    public ASTNode getReverse(VariableDeclarationStatement node) {
        return ASTNode.copySubtree(node.getAST(), node);
    }

    public ASTNode getReverse(LabeledStatement node) {
        LabeledStatement ret = (LabeledStatement)ASTNode.copySubtree(node.getAST(), node);
        if (node.getLabel().getFullyQualifiedName().equals("while_loop")) {
            /* if this is a l;oop containing a while statement */
            Block body = (Block)node.getBody();
            Block newBody = node.getAST().newBlock();
            /* add the counter declaration */
            newBody.statements().add(ASTNode.copySubtree(node.getAST(), (ASTNode) body.statements().get(0)));
            /* save the name of the counter variable */
            SimpleName name = ((VariableDeclarationFragment) ((VariableDeclarationStatement) body.statements().get(0)).fragments().get(0)).getName();

            /* SAVE the counter variable */
            MethodInvocation methodInvoc = node.getAST().newMethodInvocation();
            methodInvoc.arguments().add(ASTNode.copySubtree(node.getAST(), name));
            methodInvoc.setName(node.getAST().newSimpleName("SAVE"));
            ExpressionStatement methodInvocExpression = node.getAST().newExpressionStatement(methodInvoc);
            newBody.statements().add(methodInvocExpression);

            /* change the while statement condition to be counter variable > 0 */
            InfixExpression infixExpression = node.getAST().newInfixExpression();
            infixExpression.setLeftOperand((Expression) ASTNode.copySubtree(node.getAST(), name));
            infixExpression.setOperator(InfixExpression.Operator.GREATER);
            infixExpression.setRightOperand(node.getAST().newNumberLiteral("0"));

            WhileStatement whileStatement = (WhileStatement)body.statements().get(2);
            whileStatement.setExpression(infixExpression);
            whileStatement.setBody((Block) getReverse((Block) whileStatement.getBody()));

            newBody.statements().add(ASTNode.copySubtree(node.getAST(), whileStatement));

            ret.setBody(newBody);
        } else if (node.getBody() instanceof Block)
            /* otherwise, just get the reverse of the body */
            ret.setBody((Block) getReverse((Block) node.getBody()));
        return ret;
    }

    public ASTNode getReverse(IfStatement node) {
        /* get the reverse of the then and else statement (if else is not null) */
        IfStatement ret = (IfStatement)ASTNode.copySubtree(node.getAST(), node);
        ret.setThenStatement((Statement) getReverse((Block) node.getThenStatement()));
        if (node.getElseStatement() != null)
            ret.setElseStatement((Statement) getReverse((Block) node.getElseStatement()));
        return ret;
    }

    public ASTNode getReverse(ReturnStatement node) {
        return ASTNode.copySubtree(node.getAST(), node);
    }

    public ASTNode getReverse(Assignment node) {
        if (!(node.getOperator() == Assignment.Operator.ASSIGN)) {
            /* if the assignment uses an operator other than = */
            if (node.getRightHandSide() instanceof MethodInvocation) {
                /* if the assignment RHS is a method invocation, return it's reverse
                * this is due to reverse methods always being void */
                return getReverse(((MethodInvocation) node.getRightHandSide()));
            } else {
                /* otherwise return */
                Assignment ret = (Assignment)ASTNode.copySubtree(node.getAST(), node);
                ret.setOperator(getReverse(node.getOperator()));
                return ret;
            }
        } else {
            if (node.getRightHandSide() instanceof MethodInvocation) {
                /* return the node's right hand side */
                return ASTNode.copySubtree(node.getAST(), node.getRightHandSide());
            }
            return ASTNode.copySubtree(node.getAST(), node);
        }

    }

    public ASTNode getReverse(MethodInvocation node) {
        MethodInvocation ret = (MethodInvocation)ASTNode.copySubtree(node.getAST(), node);

        if (!node.getName().getFullyQualifiedName().equals("SAVE")) {
            /* return the same method with the prefix "rev_" */
            SimpleName name = node.getAST().newSimpleName("rev_" + node.getName().getFullyQualifiedName());
            ret.setName(name);
        }
        return ret;
    }

    public ASTNode getReverse(PostfixExpression node) {
        PrefixExpression ret = node.getAST().newPrefixExpression();
        ret.setOperand((Expression)ASTNode.copySubtree(node.getAST(), node.getOperand()));
        ret.setOperator(getReverse(node.getOperator()));
        return  ret;
    }

    /* Returns the reverse of each prefix operator */
    public PrefixExpression.Operator getReverse(PostfixExpression.Operator op) {
        if (op == PostfixExpression.Operator.DECREMENT)
            return PrefixExpression.Operator.INCREMENT;
        else if (op == PostfixExpression.Operator.INCREMENT)
            return PrefixExpression.Operator.DECREMENT;
        else
            return null;
    }

    public ASTNode getReverse(PrefixExpression node) {
        PostfixExpression ret = node.getAST().newPostfixExpression();
        ret.setOperand((Expression)ASTNode.copySubtree(node.getAST(), node.getOperand()));
        ret.setOperator(getReverse(node.getOperator()));
        return  ret;
    }

    /* Returns the reverse of each postfix operator */
    public PostfixExpression.Operator getReverse(PrefixExpression.Operator op) {
        if (op == PrefixExpression.Operator.DECREMENT)
            return PostfixExpression.Operator.INCREMENT;
        else if (op == PrefixExpression.Operator.INCREMENT)
            return PostfixExpression.Operator.DECREMENT;
        else
            return null;
    }

    /* Returns the reverse of each assignment operator */
    public Assignment.Operator getReverse(Assignment.Operator op) {
        if (op == Assignment.Operator.PLUS_ASSIGN)
            return Assignment.Operator.MINUS_ASSIGN;
        else if (op == Assignment.Operator.MINUS_ASSIGN)
            return Assignment.Operator.PLUS_ASSIGN;
        else if (op == Assignment.Operator.TIMES_ASSIGN)
            return Assignment.Operator.DIVIDE_ASSIGN;
        else if (op == Assignment.Operator.DIVIDE_ASSIGN)
            return Assignment.Operator.TIMES_ASSIGN;
        else
            return op;
    }

}
