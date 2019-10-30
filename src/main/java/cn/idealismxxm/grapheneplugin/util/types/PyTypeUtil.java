package cn.idealismxxm.grapheneplugin.util.types;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyTypedElement;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PyTypeUtil {

    /**
     * get python type of psiElement
     *
     * @param element PsiElement
     * @return null for Any type
     */
    @Nullable
    public static PyType getType(@NotNull PsiElement element) {
        return Optional.of(element)
                .filter(psiElement -> psiElement instanceof PyTypedElement)
                .map(psiElement -> TypeEvalContext.userInitiated(psiElement.getProject(), psiElement.getContainingFile()))
                .map(typeEvalContext -> typeEvalContext.getType((PyTypedElement) element))
                .orElse(null);
    }

    /**
     * get python type of targetToValue
     *
     * @param targetToValue target to value mapping
     * @return null for Any type (use annotation type first)
     */
    @Nullable
    public static PyType getType(@NotNull Pair<PyExpression, PyExpression> targetToValue) {
        return Optional.ofNullable(getType(targetToValue.getFirst()))
                .orElse(getType(targetToValue.getSecond()));
    }
}
