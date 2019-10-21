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
import com.intellij.psi.util.PsiUtilCore;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.impl.PyFunctionImpl;
import com.jetbrains.python.psi.impl.PyTargetExpressionImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class ObjectTypeLineMarkerProvider extends RelatedItemLineMarkerProvider {
    private static final Icon JUMP_TO_RESOLVER;

    static {
        JUMP_TO_RESOLVER = AllIcons.General.ArrowRight;
    }

    public ObjectTypeLineMarkerProvider() {
    }

    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        // 1. Get containing class
        Optional.of(element)
                .filter(psiElement -> psiElement instanceof PyTargetExpressionImpl)
                .map(psiElement -> (PyTargetExpressionImpl) psiElement)
                .map(PyTargetExpressionImpl::getContainingClass)
                .ifPresent(pyClass -> {
                    // 2. Get the declaration's related resolver's name
                    Optional.of((PyTargetExpressionImpl) element)
                            .map(PyTargetExpressionImpl::getNameIdentifier)
                            .ifPresent(declaration -> {
                                String resolverName = "resolve_" + declaration.getText();
                                // 3. Find related resolvers in this class and create line marker info
                                for (PyFunction pyFunction : pyClass.getMethods()) {
                                    Optional.of((PyFunctionImpl) pyFunction)
                                            .map(PyFunctionImpl::getNameIdentifier)
                                            .filter(resolver -> resolverName.equals(resolver.getText()))
                                            .ifPresent(resolver -> result.add(createLineMarkerInfo(declaration, resolver, "Navigate to resolver", JUMP_TO_RESOLVER)));
                                }
                            });
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