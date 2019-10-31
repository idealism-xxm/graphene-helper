package cn.idealismxxm.grapheneplugin.provider;

import cn.idealismxxm.grapheneplugin.enums.pyclass.GrapheneTypeEnum;
import cn.idealismxxm.grapheneplugin.util.DeclarationUtil;
import cn.idealismxxm.grapheneplugin.util.LineMarkerInfoUtil;
import cn.idealismxxm.grapheneplugin.util.PyClassUtil;
import cn.idealismxxm.grapheneplugin.util.types.PyClassTypeUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyTargetExpressionImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

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
                .filter(pyClass -> PyClassUtil.matchesClass(pyClass, GrapheneTypeEnum.MUTATION))
                .ifPresent(mutationClass -> {
                    // 2. Filter all class attributes in classes which are in files named schema.py
                    // TODO support custom filenames
                    PsiFile[] psiFiles = FilenameIndex.getFilesByName(mutationClass.getProject(), "schema.py", GlobalSearchScope.projectScope(mutationClass.getProject()));
                    // TODO Now: multiple related items display same text when click label(NAVIGATE_TO_MUTATION_FIELD)
                    Arrays.stream(psiFiles)
                            .map(PsiFile::getChildren)
                            .flatMap(Arrays::stream)
                            .filter(psiElement -> psiElement instanceof PyClass)
                            .map(psiElement -> (PyClass) psiElement)
                            .map(PyClass::getStatementList)
                            .map(pyStatementList -> PsiTreeUtil.getChildrenOfTypeAsList(pyStatementList, PyAssignmentStatement.class))
                            .flatMap(Collection::parallelStream)
                            .filter(Objects::nonNull)
                            .forEach(pyAssignmentStatement -> handleForPyAssignmentStatement(mutationClass, pyAssignmentStatement, result));
                });
    }

    private static void handleForPyAssignmentStatement(
            @NotNull PyClass mutationClass,
            @NotNull PyAssignmentStatement pyAssignmentStatement,
            @NotNull Collection<? super RelatedItemLineMarkerInfo> result
    ) {
        PsiElement mutation = Objects.requireNonNull(mutationClass.getNameIdentifier());
        // 1. Filter mutation field
        pyAssignmentStatement.getTargetsToValuesMapping().stream()
                // target only support PyTargetExpressionImpl, not support PySubscriptionExpression
                .filter(pair -> pair.getFirst() instanceof PyTargetExpressionImpl)
                // not support PyReferenceExpression because it's hard to get it's reference of mutation class
                .filter(pair -> pair.getSecond() instanceof PyCallExpression)
                .filter(pair -> PyClassTypeUtil.typeMatchesClass(pair.getFirst(), pyClassType -> !pyClassType.isDefinition(), GrapheneTypeEnum.FIELD))
                .filter(pair -> Optional.of(pair.getSecond())
                        .map(PsiElement::getFirstChild)
                        .filter(psiElement -> psiElement instanceof PyReferenceExpression)
                        .map(PsiElement::getFirstChild)
                        .filter(psiElement -> psiElement instanceof PyReferenceExpression)
                        .filter(psiElement -> mutationClass.equals(DeclarationUtil.getDeclaration(psiElement)))
                        .isPresent()
                )
                .map(pair -> (PyTargetExpression) pair.getFirst())
                .map(PsiNameIdentifierOwner::getNameIdentifier)
                .filter(Objects::nonNull)
                .forEach(mutationField -> {
                    // 2. Add to result
                    result.add(LineMarkerInfoUtil.createRelatedItemLineMarkerInfo(mutation, mutationField, "Navigate to mutation field: " + mutationField.getText(), NAVIGATE_TO_MUTATION_FIELD));
                });
    }
}
