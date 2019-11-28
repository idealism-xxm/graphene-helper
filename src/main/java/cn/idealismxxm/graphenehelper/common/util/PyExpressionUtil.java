package cn.idealismxxm.graphenehelper.common.util;

import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PyExpressionUtil {

    @Nullable
    public static PyExpression getAssignedValue(@NotNull PyExpression expression) {
        List<PyExpression> assignedValue = Stream.of(expression)
                .map(pyExpression -> PsiTreeUtil.getParentOfType(pyExpression, PyAssignmentStatement.class))
                .filter(Objects::nonNull)
                .map(PyAssignmentStatement::getTargetsToValuesMapping)
                .flatMap(Collection::parallelStream)
                .filter(pair -> expression.equals(pair.getFirst()))
                .map(pair -> pair.getSecond())
                .collect(Collectors.toList());

        return assignedValue.isEmpty() ? null : assignedValue.get(0);
    }
}
