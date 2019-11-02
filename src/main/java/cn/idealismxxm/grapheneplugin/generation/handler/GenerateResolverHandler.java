package cn.idealismxxm.grapheneplugin.generation.handler;

import cn.idealismxxm.grapheneplugin.common.enums.pyclass.GrapheneTypeEnum;
import cn.idealismxxm.grapheneplugin.common.util.types.PyClassTypeUtil;
import com.intellij.codeInsight.generation.ClassMember;
import com.jetbrains.python.codeInsight.override.PyMethodMember;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateResolverHandler extends GenerateMembersHandlerBase {

    private final static String TEMPLATE = "\n" +
            "    @staticmethod\n" +
            "    def resolve_{resolvableFieldName}(root{rootAnnotation}, info{extraArgs}):\n" +
            "        pass\n";

    public GenerateResolverHandler() {
        super("Select Fields to Generate Resolvers");
    }

    @Override
    protected ClassMember[] getAllOriginalMembers(@NotNull PyClass pyClass) {
        return Stream.of(pyClass)
                .map(PyClass::getClassAttributes)
                .flatMap(Collection::parallelStream)
                .filter(pyTargetExpression -> PyClassTypeUtil.typeMatchesAnyClass(pyTargetExpression, PyClassTypeUtil::isNotDefinition, GrapheneTypeEnum.getResolvableGrapheneTypeEnums()))
                // TODO filter field without resolver
                .map(PyMethodMember::new)
                .collect(Collectors.toList())
                .toArray(ClassMember.EMPTY_ARRAY);
    }

    @Override
    protected String generateText(ClassMember member) {
        return TEMPLATE.replace("{resolvableFieldName}", member.getText())
                // TODO support root annotation and extra args
                .replace("{rootAnnotation}", "")
                .replace("{extraArgs}", "");
    }
}
