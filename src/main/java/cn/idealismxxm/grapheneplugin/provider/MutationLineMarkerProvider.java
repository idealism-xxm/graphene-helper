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
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.stubs.PyClassNameIndex;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

public class MutationLineMarkerProvider extends RelatedItemLineMarkerProvider {
    private static final Icon NAVIGATE_TO_MUTATION_FIELD;

    static {
        NAVIGATE_TO_MUTATION_FIELD = AllIcons.General.ArrowLeft;
    }

    public MutationLineMarkerProvider() {
    }

    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        // 1. Filter mutation subclass
        Optional.of(element)
                .filter(psiElement -> "class".equals(psiElement.getText()))
                .map(PsiElement::getParent)
                .filter(psiElement -> psiElement instanceof PyClass)
                .map(psiElement -> (PyClass) psiElement)
                .map(pyClass -> pyClass.getAncestorClasses(TypeEvalContext.codeAnalysis(pyClass.getProject(), pyClass.getContainingFile())))
                .filter(superClasses -> superClasses.stream().anyMatch(superClass ->
                        "Mutation".equals(superClass.getName())
                                && superClass.getContainingFile().getVirtualFile().toString().endsWith("/graphene/types/mutation.py"))
                )
                .ifPresent(superClasses -> {
                    // 2. Filter all class attributes in classes which are in files named schema.py
                    PyClass mutationClass = (PyClass) element.getParent();
                    PsiElement mutation = mutationClass.getNameIdentifier();
                    if (mutation == null) {
                        return;
                    }
                    PsiFile[] psiFiles = FilenameIndex.getFilesByName(mutationClass.getProject(), "schema.py", GlobalSearchScope.projectScope(mutationClass.getProject()));
                    Arrays.stream(psiFiles)
                            .map(PsiFile::getChildren)
                            .flatMap(Arrays::stream)
                            .filter(psiElement -> psiElement instanceof PyClass)
                            .map(psiElement -> (PyClass) psiElement)
                            .map(PyClass::getClassAttributes)
                            .flatMap(Collection::parallelStream)
                            .map(pyTargetExpression -> PsiTreeUtil.getParentOfType(pyTargetExpression, PyAssignmentStatement.class))
                            .filter(Objects::nonNull)
                            .forEach(pyAssignmentStatement -> Optional.of(pyAssignmentStatement)
                                    .map(PyAssignmentStatement::getAssignedValue)
                                    // TODO pyExpression instanceof PyTupleExpression
                                    .filter(pyExpression -> pyExpression instanceof PyCallExpression)
                                    .map(pyExpression -> (PyCallExpression) pyExpression)
                                    .ifPresent(pyCallExpression -> {
                                        String[] names = pyCallExpression.toString().substring("PyCallExpression: ".length()).split("\\.");
                                        if (names.length >= 2
                                                && "Field".equals(names[names.length - 1])
                                                && PyClassNameIndex.find(names[names.length - 2], pyCallExpression.getProject(), false).stream().anyMatch(mutationClass::equals)) {
                                            PsiElement mutationDeclaration = pyAssignmentStatement.getTargets()[0];
                                            result.add(createLineMarkerInfo(mutation, mutationDeclaration, "Navigate to mutation field", NAVIGATE_TO_MUTATION_FIELD));
                                        }
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