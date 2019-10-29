package cn.idealismxxm.grapheneplugin.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.impl.PyGotoDeclarationHandler;
import org.jetbrains.annotations.Nullable;

public class DeclarationUtil {

    private static final PyGotoDeclarationHandler PY_GOTO_DECLARATION_HANDLER;

    static {
        PY_GOTO_DECLARATION_HANDLER = new PyGotoDeclarationHandler();
    }

    public static PsiElement getDeclaration(@Nullable PsiElement sourceElement) {
        return PY_GOTO_DECLARATION_HANDLER.getGotoDeclarationTarget(sourceElement, null);
    }
}
