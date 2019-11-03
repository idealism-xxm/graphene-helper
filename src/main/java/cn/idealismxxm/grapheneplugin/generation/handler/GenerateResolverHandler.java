package cn.idealismxxm.grapheneplugin.generation.handler;

import cn.idealismxxm.grapheneplugin.common.enums.pyclass.GrapheneTypeEnum;
import cn.idealismxxm.grapheneplugin.common.util.PyClassUtil;
import cn.idealismxxm.grapheneplugin.common.util.PyExpressionUtil;
import cn.idealismxxm.grapheneplugin.common.util.types.PyClassTypeUtil;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.PsiElementMemberChooserObject;
import com.jetbrains.python.codeInsight.override.PyMethodMember;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyKeywordArgument;
import com.jetbrains.python.psi.PyTargetExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
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
        Set<String> allFunctionNames = PyClassUtil.getAllFunctionNames(pyClass);
        return Stream.of(pyClass)
                .map(PyClass::getClassAttributes)
                .flatMap(Collection::parallelStream)
                .filter(pyTargetExpression -> PyClassTypeUtil.typeMatchesAnyClass(pyTargetExpression, PyClassTypeUtil::isNotDefinition, GrapheneTypeEnum.getResolvableGrapheneTypeEnums()))
                .filter(pyTargetExpression -> !allFunctionNames.contains("resolve_" + pyTargetExpression.getName()))
                .map(PyMethodMember::new)
                .collect(Collectors.toList())
                .toArray(ClassMember.EMPTY_ARRAY);
    }

    @Override
    protected String generateText(ClassMember member) {
        String extraArgs = this.generateExtraArgs(member);
        return TEMPLATE.replace("{resolvableFieldName}", member.getText())
                // TODO support root annotation and extra args' annotation
                .replace("{rootAnnotation}", "")
                .replace("{extraArgs}", extraArgs);
    }

    @NotNull
    private String generateExtraArgs(ClassMember member) {
        StringBuilder extraArgs = new StringBuilder();
        Stream.of(member)
                .filter(classMember -> classMember instanceof PsiElementMemberChooserObject)
                .map(classMember -> (PsiElementMemberChooserObject) classMember)
                .map(PsiElementMemberChooserObject::getPsiElement)
                .filter(psiElement -> psiElement instanceof PyTargetExpression)
                .map(psiElement -> (PyTargetExpression) psiElement)
                .map(PyExpressionUtil::getAssignedValue)
                // not support PyReferenceExpression because it's hard to get args
                .filter(pyExpression -> pyExpression instanceof PyCallExpression)
                .map(pyExpression -> (PyCallExpression) pyExpression)
                .map(PyCallExpression::getArguments)
                .flatMap(Arrays::stream)
                .filter(pyExpression -> pyExpression instanceof PyKeywordArgument)
                .map(pyExpression -> (PyKeywordArgument) pyExpression)
                .filter(pyKeywordArgument -> pyKeywordArgument.getValueExpression() != null)
                .filter(pyKeywordArgument -> PyClassTypeUtil.typeMatchesAnyClass(pyKeywordArgument.getValueExpression(), PyClassTypeUtil::isNotDefinition, GrapheneTypeEnum.getArgumentGrapheneTypeEnums()))
                // TODO support args annotation
                .map(PyKeywordArgument::getKeyword)
                .forEach(keyword -> extraArgs.append(", ").append(keyword));
        return extraArgs.toString();
    }
}
