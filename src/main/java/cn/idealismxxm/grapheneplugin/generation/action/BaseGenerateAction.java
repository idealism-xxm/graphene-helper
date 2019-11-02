package cn.idealismxxm.grapheneplugin.generation.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.codeInsight.generation.actions.GenerateActionPopupTemplateInjector;
import com.intellij.lang.ContextAwareActionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseGenerateAction extends CodeInsightAction implements GenerateActionPopupTemplateInjector {
    private final CodeInsightActionHandler myHandler;

    protected BaseGenerateAction(CodeInsightActionHandler handler) {
        myHandler = handler;
    }

    @Override
    protected void update(@NotNull Presentation presentation,
                          @NotNull Project project,
                          @NotNull Editor editor,
                          @NotNull PsiFile file,
                          @NotNull DataContext dataContext,
                          @Nullable String actionPlace) {
        super.update(presentation, project, editor, file, dataContext, actionPlace);
        if (myHandler instanceof ContextAwareActionHandler && presentation.isEnabled()) {
            presentation.setEnabled(((ContextAwareActionHandler) myHandler).isAvailableForQuickList(editor, file, dataContext));
        }
    }

    @Override
    @Nullable
    public AnAction createEditTemplateAction(DataContext dataContext) {
        return null;
    }

    @NotNull
    @Override
    protected final CodeInsightActionHandler getHandler() {
        return myHandler;
    }


    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return file instanceof PyFile;
    }
}
