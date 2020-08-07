package cn.idealismxxm.graphenehelper.navigation.provider;

import cn.idealismxxm.graphenehelper.common.enums.pyclass.GrapheneTypeEnum;
import cn.idealismxxm.graphenehelper.common.util.LineMarkerInfoUtil;
import cn.idealismxxm.graphenehelper.common.util.types.PyClassTypeUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.impl.PyFunctionImpl;
import com.jetbrains.python.psi.impl.PyTargetExpressionImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ResolverLineMarkerProvider extends RelatedItemLineMarkerProvider {
    private static final Icon JUMP_TO_DECLARATION;

    static {
        JUMP_TO_DECLARATION = AllIcons.General.ArrowLeft;
    }

    public ResolverLineMarkerProvider() {
    }

    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // 1. Get the resolver
        Optional.of(element)
                .filter(psiElement -> psiElement instanceof LeafPsiElement)
                .filter(psiElement -> "def".equals(psiElement.getText()))
                .map(PsiElement::getParent)
                .filter(psiElement -> psiElement instanceof PyFunctionImpl)
                .map(psiElement -> ((PyFunctionImpl) psiElement).getNameIdentifier())
                .filter(resolver -> resolver.getText().startsWith("resolve_"))
                .ifPresent(resolver -> {
                    // 2. Get the related declaration
                    Stream.of(element)
                            .map(psiElement -> PsiTreeUtil.getParentOfType(psiElement, PyClass.class))
                            .filter(Objects::nonNull)
                            .map(PyClass::getClassAttributes)
                            .flatMap(Collection::parallelStream)
                            .filter(pyTargetExpression -> PyClassTypeUtil.typeMatchesAnyClass(pyTargetExpression, PyClassTypeUtil::isNotDefinition, GrapheneTypeEnum.getResolvableGrapheneTypeEnums()))
                            .map(pyTargetExpression -> (PyTargetExpressionImpl) pyTargetExpression)
                            .map(PyTargetExpressionImpl::getNameIdentifier)
                            .filter(Objects::nonNull)
                            .filter(declaration -> resolver.getText().equals("resolve_" + declaration.getText()))
                            .forEach(declaration -> {
                                // 3. Add to result
                                result.add(LineMarkerInfoUtil.createRelatedItemLineMarkerInfo(resolver, declaration, "Navigate to declaration: " + declaration.getText(), JUMP_TO_DECLARATION));
                            });
                });
    }
}
