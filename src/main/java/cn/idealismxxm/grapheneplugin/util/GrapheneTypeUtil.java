package cn.idealismxxm.grapheneplugin.util;

import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

public class GrapheneTypeUtil {
    public static boolean isMutation(@NotNull PyClass pyClass) {
        return isSpecificClass(pyClass, "Mutation", "mutation.py");
    }

    public static boolean isObjectType(@NotNull PyClass pyClass) {
        return isSpecificClass(pyClass, "ObjectType", "objecttype.py");
    }

    private static boolean isSpecificClass(@NotNull PyClass pyClass, @NotNull String className, @NotNull String filename) {
        String filePathSuffix = "/graphene/types/" + filename;
        return pyClass.getAncestorClasses(TypeEvalContext.codeAnalysis(pyClass.getProject(), pyClass.getContainingFile()))
                .stream()
                .anyMatch(superClass -> className.equals(superClass.getName())
                        && superClass.getContainingFile().getVirtualFile().toString().endsWith(filePathSuffix));
    }
}
