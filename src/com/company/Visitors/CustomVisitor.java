package com.company.Visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Created by Andy on 29/08/2015.
 */
public abstract class CustomVisitor extends ASTVisitor {

    protected ASTRewrite rewriter;

    public CustomVisitor() {
        this.rewriter = null;
    }

    public void setRewriter(ASTRewrite rewriter) {
        this.rewriter = rewriter;
    }

    public void accept(MethodDeclaration declaration) { declaration.accept(this); }

}
