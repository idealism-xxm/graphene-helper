package cn.idealismxxm.provider;

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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyStatementList;
import com.jetbrains.python.psi.impl.PyFunctionImpl;
import com.jetbrains.python.psi.impl.PyTargetExpressionImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class ObjectTypeLineMarkerProvider extends RelatedItemLineMarkerProvider {
    private static final Icon JUMP_TO_RESOLVER;
    private static final Icon JUMP_TO_DECLARATION;

    static {
        JUMP_TO_RESOLVER = AllIcons.General.ArrowRight;
        JUMP_TO_DECLARATION = AllIcons.General.ArrowLeft;
    }

    public ObjectTypeLineMarkerProvider() {
    }

    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        // 1. Return if element isn't instanceof PyAssignmentStatement,
        // neither it's parent of parent is instanceof PyClass,
        if (!(element instanceof PyAssignmentStatement)) {
            return;
        }
        Optional<PsiElement> pyClass = Optional.of(element).map(PsiElement::getParent).map(PsiElement::getParent);
        if (!pyClass.isPresent() || !(pyClass.get() instanceof PyClass)) {
            return;
        }

        // 2. Get the declaration's related resolver's name
        Optional<PsiElement> declaration = Optional.of(element)
                .map(psiElement -> PsiTreeUtil.getChildOfAnyType(psiElement, PyTargetExpressionImpl.class))
                .map(PyTargetExpressionImpl::getNameIdentifier);
        if (!declaration.isPresent()) {
            return;
        }
        String resolverName = "resolve_" + declaration.get().getText();

        // 3. Find related resolvers in this class and create line marker info
        pyClass.map(psiElement -> PsiTreeUtil.getChildOfType(psiElement, PyStatementList.class))
                .ifPresent(pyStatementList -> PsiTreeUtil.getChildrenOfTypeAsList(pyStatementList, PyFunction.class).forEach(pyFunction -> Optional.of((PyFunctionImpl) pyFunction)
                        .map(PyFunctionImpl::getNameIdentifier)
                        .ifPresent(resolver -> {
                            if (resolverName.equals(resolver.getText())) {
                                result.add(createLineMarkerInfo(declaration.get(), resolver, "Navigate to resolver", JUMP_TO_RESOLVER));
                                result.add(createLineMarkerInfo(resolver, declaration.get(), "Navigate to declaration", JUMP_TO_DECLARATION));
                            }
                        })));
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