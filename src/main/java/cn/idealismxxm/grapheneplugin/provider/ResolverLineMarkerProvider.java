package cn.idealismxxm.grapheneplugin.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.impl.PyFunctionImpl;
import com.jetbrains.python.psi.impl.PyTargetExpressionImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class ResolverLineMarkerProvider extends RelatedItemLineMarkerProvider {
    private static final Icon JUMP_TO_DECLARATION;

    static {
        JUMP_TO_DECLARATION = AllIcons.General.ArrowLeft;
    }

    public ResolverLineMarkerProvider() {
    }

    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
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
                    Optional.of(element)
                            .map(psiElement -> PsiTreeUtil.getParentOfType(psiElement, PyClass.class))
                            .map(PyClass::getClassAttributes)
                            .ifPresent(pyTargetExpressions -> pyTargetExpressions.forEach(pyTargetExpression -> {
                                Optional.ofNullable((PyTargetExpressionImpl) pyTargetExpression)
                                        .map(PyTargetExpressionImpl::getNameIdentifier)
                                        .filter(declaration -> resolver.getText().equals("resolve_" + declaration.getText()))
                                        .ifPresent(declaration -> {
                                            // 3. Add to result
                                            result.add(createLineMarkerInfo(resolver, declaration, "Navigate to declaration", JUMP_TO_DECLARATION));
                                        });

                            }));
                });
    }

    @NotNull
    private static RelatedItemLineMarkerInfo<PsiElement> createLineMarkerInfo(@NotNull PsiElement element, @NotNull PsiElement relatedElement, @NotNull String itemTitle, @NotNull Icon icon) {
        SmartPointerManager pointerManager = SmartPointerManager.getInstance(element.getProject());
        SmartPsiElementPointer<PsiElement> relatedElementPointer = pointerManager.createSmartPsiElementPointer(relatedElement);
        String stubFileName = relatedElement.getContainingFile().getName();

        return new RelatedItemLineMarkerInfo<>(element, element.getTextRange(), icon, 11, (element1) -> itemTitle + " in " + stubFileName, (e, elt) -> {
            PsiElement restoredRelatedElement = relatedElementPointer.getElement();
            if (restoredRelatedElement != null) {
                int offset = restoredRelatedElement instanceof PsiFile ? -1 : restoredRelatedElement.getTextOffset();
                VirtualFile virtualFile = PsiUtilCore.getVirtualFile(restoredRelatedElement);
                if (virtualFile != null && virtualFile.isValid()) {
                    PsiNavigationSupport.getInstance().createNavigatable(restoredRelatedElement.getProject(), virtualFile, offset).navigate(true);
                }

            }
        }, Alignment.RIGHT, GotoRelatedItem.createItems(Collections.singletonList(relatedElement)));
    }
}