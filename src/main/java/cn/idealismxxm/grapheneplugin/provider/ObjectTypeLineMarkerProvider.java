package cn.idealismxxm.grapheneplugin.provider;

import cn.idealismxxm.grapheneplugin.util.LineMarkerInfoUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyStatementList;
import com.jetbrains.python.psi.impl.PyFunctionImpl;
import com.jetbrains.python.psi.impl.PyTargetExpressionImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class ObjectTypeLineMarkerProvider extends RelatedItemLineMarkerProvider {
    private static final Icon JUMP_TO_RESOLVER;

    static {
        JUMP_TO_RESOLVER = AllIcons.General.ArrowRight;
    }

    public ObjectTypeLineMarkerProvider() {
    }

    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        // 1. Get the declaration's related resolver's name
        Stream.of(element)
                .filter(psiElement -> psiElement instanceof PyAssignmentStatement)
                .map(psiElement -> (PyAssignmentStatement) psiElement)
                .map(PyAssignmentStatement::getTargetsToValuesMapping)
                .flatMap(Collection::parallelStream)
                .filter(pair -> pair.getFirst() instanceof PyTargetExpressionImpl)
                // TODO support PyReferenceExpression
                .filter(pair -> pair.getSecond() instanceof PyCallExpression)
                // TODO filter List, Field, ..., types
                .map(pair -> (PyTargetExpressionImpl) pair.getFirst())
                .map(PyTargetExpressionImpl::getNameIdentifier)
                .filter(Objects::nonNull)
                .forEach(declaration -> {
                    String resolverName = "resolve_" + declaration.getText();
                    // 2. Find related resolvers in containing class and create line marker info
                    Stream.of(element)
                            .map(psiElement -> PsiTreeUtil.getParentOfType(psiElement, PyStatementList.class))
                            .filter(Objects::nonNull)
                            .map(PsiElement::getParent)
                            .filter(psiElement -> psiElement instanceof PyClass)
                            .map(psiElement -> (PyClass) psiElement)
                            .map(PyClass::getMethods)
                            .flatMap(Arrays::stream)
                            .map(pyFunction -> (PyFunctionImpl) pyFunction)
                            .map(PyFunctionImpl::getNameIdentifier)
                            .filter(Objects::nonNull)
                            .filter(resolver -> resolverName.equals(resolver.getText()))
                            .forEach(resolver -> {
                                // 3. Add to result
                                result.add(LineMarkerInfoUtil.createRelatedItemLineMarkerInfo(declaration, resolver, "Navigate to resolver: " + resolverName, JUMP_TO_RESOLVER));
                            });
                });
    }
}