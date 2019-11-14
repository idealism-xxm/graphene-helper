package cn.idealismxxm.grapheneplugin.generation.handler;

import cn.idealismxxm.grapheneplugin.common.enums.pyclass.GrapheneTypeEnum;
import cn.idealismxxm.grapheneplugin.common.util.PyClassUtil;
import cn.idealismxxm.grapheneplugin.common.util.types.PyClassTypeUtil;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.codeInsight.override.PyMethodMember;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateMutationHandler extends GenerateMembersHandlerBase {

    private final static String TEMPLATE = "\n" +
            "    @staticmethod\n" +
            "    def mutate(root, info{extraArgs}):\n" +
            "        pass\n";

    public GenerateMutationHandler() {
        super("Select Arguments to Generate Mutation");
    }

    @Override
    public boolean isAvailableForQuickList(@NotNull Editor editor, @NotNull PsiFile file, @NotNull DataContext dataContext) {
        return super.isAvailableForQuickList(editor, file, dataContext)
                && Optional.ofNullable(PyClassUtil.getContextClass(editor, file))
                .filter(pyClass -> !PyClassUtil.getAllFunctionNames(pyClass).contains("mutation"))
                .filter(pyClass -> PyClassUtil.matchesSubclass(pyClass, GrapheneTypeEnum.MUTATION)).isPresent();
    }

    /**
     * Override to allow empty selection
     *
     * @param pyClass PyClass
     * @param project Project
     * @return ClassMember[]
     */
    @Nullable
    @Override
    protected ClassMember[] chooseOriginalMembers(PyClass pyClass, Project project) {
        ClassMember[] allMembers = getAllOriginalMembers(pyClass);
        return chooseMembers(allMembers, true, project, null);
    }

    @Override
    protected ClassMember[] getAllOriginalMembers(@NotNull PyClass pyClass) {
        return Stream.of(pyClass)
                .filter(_pyClass -> PyClassUtil.matchesSubclass(_pyClass, GrapheneTypeEnum.MUTATION))
                .map(PyClass::getNestedClasses)
                .flatMap(Arrays::stream)
                .filter(nestedClass -> "Arguments".equals(nestedClass.getName()))
                .map(PyClass::getClassAttributes)
                .flatMap(Collection::parallelStream)
                .filter(pyTargetExpression -> PyClassTypeUtil.typeMatchesAnyClass(pyTargetExpression, PyClassTypeUtil::isNotDefinition, GrapheneTypeEnum.getArgumentGrapheneTypeEnums()))
                .map(PyMethodMember::new)
                .collect(Collectors.toList())
                .toArray(ClassMember.EMPTY_ARRAY);
    }

    @Override
    protected String generateText(@NotNull ClassMember[] members) {
        StringBuilder extraArgs = new StringBuilder();
        for (ClassMember member : members) {
            // TODO support annotation
            extraArgs.append(", ").append(member.getText());
        }
        return TEMPLATE.replace("{extraArgs}", extraArgs.toString());
    }

    /**
     * Useless because of overriding method {@link #generateText(ClassMember[])}
     *
     * @param member ClassMember
     * @return ""
     */
    @Override
    protected String generateText(@NotNull ClassMember member) {
        return "";
    }
}
