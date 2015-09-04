package com.company.Visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy on 20/08/2015.
 */
public class MethodVisitor extends ASTVisitor {

    private List<MethodDeclaration> methodDeclarations = new ArrayList<MethodDeclaration>();
    private List<String> methods;

    public MethodVisitor(List<String> methods) {
        this.methods = methods;
    }

    public List<MethodDeclaration> getMethodDeclarations(CompilationUnit cu) {
        cu.accept(this);
        return methodDeclarations;
    }

    @Override
    public boolean visit(MethodDeclaration methodDeclaration) {
        if (methods.contains(methodDeclaration.getName().getFullyQualifiedName()) || methods.isEmpty())
            methodDeclarations.add(methodDeclaration);

        return super.visit(methodDeclaration);
    }
}
