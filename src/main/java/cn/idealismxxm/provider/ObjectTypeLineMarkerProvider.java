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
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyTargetExpressionImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

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
        if (!(element instanceof PyClass)) {
            return;
        }

        PyClass pyClass = (PyClass) element;
        PyStatementList pyStatementList = PsiTreeUtil.getChildOfType(pyClass, PyStatementList.class);

        // 1. Find all resolvers in this class
        Map<String, PsiElement> resolverName2Resolver = new HashMap<>();
        PsiTreeUtil.getChildrenOfTypeAsList(pyStatementList, PyFunction.class).forEach(pyFunction -> {
            String functionName = pyFunction.getName();
            if (functionName == null || !functionName.startsWith("resolve_")) {
                return;
            }

            resolverName2Resolver.put(functionName, pyFunction.getNameIdentifier());
        });

        // 2. Iterate identifier in this class and create line marker info if it has related resolver
        PsiTreeUtil.getChildrenOfTypeAsList(pyStatementList, PyAssignmentStatement.class).forEach(pyAssignmentStatement -> {
            Arrays.asList(pyAssignmentStatement.getTargets()).forEach(pyExpression -> {
                PsiElement declaration = ((PyTargetExpressionImpl) pyExpression).getNameIdentifier();
                if (declaration == null) {
                    return;
                }

                String fieldResolverName = "resolve_" + declaration.getText();
                PsiElement resolver = resolverName2Resolver.get(fieldResolverName);
                if (resolver == null) {
                    return;
                }

                result.add(createLineMarkerInfo(declaration, resolver, "Navigate to resolver", JUMP_TO_RESOLVER));
                result.add(createLineMarkerInfo(resolver, declaration, "Navigate to declaration", JUMP_TO_DECLARATION));
            });
        });


    }

    @NotNull
    private static RelatedItemLineMarkerInfo<PsiElement> createLineMarkerInfo(@NotNull PsiElement element, @NotNull PsiElement relatedElement, @NotNull String itemTitle, @NotNull Icon icon) {
        SmartPointerManager pointerManager = SmartPointerManager.getInstance(element.getProject());
        SmartPsiElementPointer<PsiElement> relatedElementPointer = pointerManager.createSmartPsiElementPointer(relatedElement);
        String stubFileName = relatedElement.getContainingFile().getName();

        return new RelatedItemLineMarkerInfo<>(element, element.getTextRange(), icon, 11, (element1) -> {
            return itemTitle + " in " + stubFileName;
        }, (e, elt) -> {
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