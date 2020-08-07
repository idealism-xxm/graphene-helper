package cn.idealismxxm.graphenehelper.navigation.provider;

import cn.idealismxxm.graphenehelper.common.enums.pyclass.GrapheneTypeEnum;
import cn.idealismxxm.graphenehelper.common.util.LineMarkerInfoUtil;
import cn.idealismxxm.graphenehelper.common.util.types.PyClassTypeUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyAssignmentStatement;
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

public class ResolvableTypeLineMarkerProvider extends RelatedItemLineMarkerProvider {
    private static final Icon JUMP_TO_RESOLVER;

    static {
        JUMP_TO_RESOLVER = AllIcons.General.ArrowRight;
    }

    public ResolvableTypeLineMarkerProvider() {
    }

    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // 1. Get the declaration's related resolver's name
        Stream.of(element)
                .filter(psiElement -> psiElement instanceof PyAssignmentStatement)
                .map(psiElement -> (PyAssignmentStatement) psiElement)
                .map(PyAssignmentStatement::getTargetsToValuesMapping)
                .flatMap(Collection::parallelStream)
                // target only support PyTargetExpressionImpl, not support PySubscriptionExpression
                .filter(pair -> pair.getFirst() instanceof PyTargetExpressionImpl)
                .filter(pair -> PyClassTypeUtil.typeMatchesAnyClass(pair.getFirst(), pyClassType -> !pyClassType.isDefinition(), GrapheneTypeEnum.getResolvableGrapheneTypeEnums()))
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
