package cn.idealismxxm.grapheneplugin.common.util;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;

public class LineMarkerInfoUtil {
    /**
     * @param element        the element for which the line marker is created
     * @param relatedElement the element related to element
     * @param tooltipPrefix  the tooltip prefix for the gutter icon
     *                       format: {tooltipPrefix} in file {relatedElement.getContainingFile().getName()}
     * @param icon           the icon to show in the gutter for the line marker
     * @return RelatedItemLineMarkerInfo<T>
     */
    @NotNull
    public static <T extends PsiElement> RelatedItemLineMarkerInfo<T> createRelatedItemLineMarkerInfo(
            @NotNull T element,
            @NotNull T relatedElement,
            @NotNull String tooltipPrefix,
            @NotNull Icon icon
    ) {
        String stubFileName = relatedElement.getContainingFile().getName();
        return createRelatedItemLineMarkerInfo(element, relatedElement, (elt) -> tooltipPrefix + " in " + stubFileName, icon);
    }

    /**
     * @param element         the element for which the line marker is created
     * @param relatedElement  the element related to element
     * @param tooltipProvider the tooltip prefix for the gutter icon
     *                        format: {tooltipPrefix} in file {relatedElement.getContainingFile().getName()}
     * @param icon            the icon to show in the gutter for the line marker
     * @return RelatedItemLineMarkerInfo<T>
     */
    @NotNull
    public static <T extends PsiElement> RelatedItemLineMarkerInfo<T> createRelatedItemLineMarkerInfo(
            @NotNull T element,
            @NotNull T relatedElement,
            @Nullable Function<? super T, String> tooltipProvider,
            @NotNull Icon icon
    ) {
        SmartPointerManager pointerManager = SmartPointerManager.getInstance(element.getProject());
        SmartPsiElementPointer<T> relatedElementPointer = pointerManager.createSmartPsiElementPointer(relatedElement);

        // argument updatePass is useless
        return new RelatedItemLineMarkerInfo<>(
                element, element.getTextRange(), icon, Pass.LINE_MARKERS,
                tooltipProvider,
                (e, elt) -> {
                    PsiElement restoredRelatedElement = relatedElementPointer.getElement();
                    if (restoredRelatedElement != null) {
                        int offset = restoredRelatedElement instanceof PsiFile ? -1 : restoredRelatedElement.getTextOffset();
                        VirtualFile virtualFile = PsiUtilCore.getVirtualFile(restoredRelatedElement);
                        if (virtualFile != null && virtualFile.isValid()) {
                            PsiNavigationSupport.getInstance().createNavigatable(restoredRelatedElement.getProject(), virtualFile, offset).navigate(true);
                        }
                    }
                },
                GutterIconRenderer.Alignment.RIGHT, GotoRelatedItem.createItems(Collections.singletonList(relatedElement))
        );
    }
}
