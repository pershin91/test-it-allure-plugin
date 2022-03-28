package com.github.clientcyc.testit.plugin.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.Optional;

public class PsiUtils {

    public static PsiAnnotation createAnnotation(final String annotation, final PsiElement context) {
        final PsiElementFactory factory = PsiElementFactory.getInstance(context.getProject());
        return factory.createAnnotationFromText(annotation, context);
    }

    public static void addImport(final PsiFile file, final String qualifiedName) {
        if (file instanceof PsiJavaFile) {
            PsiJavaFile javaFile = (PsiJavaFile) file;

            final Project project = javaFile.getProject();
            Optional<PsiClass> possibleClass = Optional.ofNullable(JavaPsiFacade.getInstance(project)
                    .findClass(qualifiedName, GlobalSearchScope.everythingScope(project)));
            possibleClass.ifPresent(psiClass -> JavaCodeStyleManager.getInstance(project).addImport(javaFile, psiClass));
        }
    }
}
