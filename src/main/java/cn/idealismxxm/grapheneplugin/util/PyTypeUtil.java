package cn.idealismxxm.grapheneplugin.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyTypedElement;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PyTypeUtil {

    @Nullable
    public static PyType getType(@NotNull PsiElement element) {
        return Optional.of(element)
                .filter(psiElement -> psiElement instanceof PyTypedElement)
                .map(psiElement -> TypeEvalContext.userInitiated(psiElement.getProject(), psiElement.getContainingFile()))
                .map(typeEvalContext -> typeEvalContext.getType((PyTypedElement) element))
                .orElse(null);
    }
}
