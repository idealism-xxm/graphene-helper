/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.idealismxxm.grapheneplugin.generation.handler;

import cn.idealismxxm.grapheneplugin.common.util.PyClassUtil;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.util.MemberChooser;
import com.intellij.lang.ContextAwareActionHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public abstract class GenerateMembersHandlerBase implements CodeInsightActionHandler, ContextAwareActionHandler {
    private final String myChooserTitle;

    public GenerateMembersHandlerBase(String chooserTitle) {
        myChooserTitle = chooserTitle;
    }

    @Override
    public boolean isAvailableForQuickList(@NotNull Editor editor, @NotNull PsiFile file, @NotNull DataContext dataContext) {
        return PyClassUtil.getContextClass(editor, file) != null;
    }

    @Override
    public final void invoke(@NotNull final Project project, @NotNull final Editor editor, @NotNull PsiFile file) {
        if (!EditorModificationUtil.checkModificationAllowed(editor)) {
            return;
        }
        if (!FileDocumentManager.getInstance().requestWriting(editor.getDocument(), project)) {
            return;
        }
        final PyClass pyClass = PyClassUtil.getContextClass(editor, file);

        try {
            final ClassMember[] members = chooseOriginalMembers(pyClass, project, editor);
            if (members == null) {
                return;
            }

            CommandProcessor.getInstance().executeCommand(project, () -> doGenerate(project, editor, pyClass, members), null, null);
        } finally {
            cleanup();
        }
    }

    protected void cleanup() {
    }

    private void doGenerate(final Project project, final Editor editor, PyClass pyClass, ClassMember[] members) {
        int col = editor.getCaretModel().getLogicalPosition().column;
        int line = editor.getCaretModel().getLogicalPosition().line;
        final Document document = editor.getDocument();
        int lineEndOffset = document.getLineEndOffset(line);

        String text = this.generateText(members);

        if (StringUtil.isEmptyOrSpaces(text)) {
            if (!ApplicationManager.getApplication().isUnitTestMode()) {
                HintManager.getInstance().showErrorHint(editor, getNothingFoundMessage());
            }
        } else {
            editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(0, 0));

            WriteAction.run(() -> editor.getDocument().insertString(lineEndOffset, text));

            editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(line, col));
        }
    }

    protected String generateText(@NotNull ClassMember[] members) {
        StringBuilder stringBuffer = new StringBuilder();
        for (ClassMember member : members) {
            stringBuffer.append(this.generateText(member));
        }
        return stringBuffer.toString();
    }

    protected String getNothingFoundMessage() {
        return "Nothing found to insert";
    }

    @Nullable
    protected ClassMember[] chooseOriginalMembers(PyClass pyClass, Project project) {
        ClassMember[] allMembers = getAllOriginalMembers(pyClass);
        ClassMember[] chosenMembers = chooseMembers(allMembers, false, project, null);
        // not allow empty selection default
        return ClassMember.EMPTY_ARRAY == chosenMembers ? null : chosenMembers;
    }

    @Nullable
    protected ClassMember[] chooseOriginalMembers(PyClass pyClass, Project project, Editor editor) {
        return chooseOriginalMembers(pyClass, project);
    }

    /**
     * choose class members
     *
     * @param members             class member array
     * @param allowEmptySelection allow empty selection
     * @param project             Project
     * @param editor              Editor
     * @return null for cancel; empty array for selection none when allowed
     */
    @Nullable
    protected ClassMember[] chooseMembers(ClassMember[] members,
                                          boolean allowEmptySelection,
                                          Project project,
                                          @Nullable Editor editor) {
        MemberChooser<ClassMember> chooser = createMembersChooser(members, allowEmptySelection, project);
        chooser.show();
        if (chooser.getExitCode() == MemberChooser.CANCEL_EXIT_CODE) {
            return null;
        }

        final List<ClassMember> list = chooser.getSelectedElements();
        return list == null ? ClassMember.EMPTY_ARRAY : list.toArray(ClassMember.EMPTY_ARRAY);
    }

    protected MemberChooser<ClassMember> createMembersChooser(ClassMember[] members,
                                                              boolean allowEmptySelection,
                                                              Project project) {
        MemberChooser<ClassMember> chooser = new MemberChooser<ClassMember>(members, allowEmptySelection, true, project, getHeaderPanel(project), getOptionControls()) {
            @Nullable
            @Override
            protected String getHelpId() {
                return GenerateMembersHandlerBase.this.getHelpId();
            }
        };
        chooser.setTitle(myChooserTitle);
        chooser.setCopyJavadocVisible(false);
        return chooser;
    }

    @Nullable
    protected JComponent getHeaderPanel(Project project) {
        return null;
    }

    @Nullable
    protected JComponent[] getOptionControls() {
        return null;
    }

    protected String getHelpId() {
        return null;
    }

    /**
     * get all available class members
     *
     * @param pyClass PyClass
     * @return class member array
     */
    protected abstract ClassMember[] getAllOriginalMembers(PyClass pyClass);

    /**
     * generate text which will be inserted
     *
     * @param member class member
     * @return text which will be inserted
     */
    protected abstract String generateText(ClassMember member);

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
