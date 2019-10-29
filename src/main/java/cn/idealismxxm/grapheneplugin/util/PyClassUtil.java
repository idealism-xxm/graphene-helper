package cn.idealismxxm.grapheneplugin.util;

import cn.idealismxxm.grapheneplugin.enums.pyclass.PyClassInfo;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PyClassUtil {

    /**
     * judge whether the pyClass matches exact class or subclass described in pyClassInfos
     *
     * @param pyClass     pyClass
     * @param pyClassInfo pyClassInfo
     * @return true / false
     */
    public static boolean matchesClass(@NotNull PyClass pyClass, @NotNull PyClassInfo pyClassInfo) {
        return matchesAnyClass(pyClass, pyClassInfo);
    }

    /**
     * judge whether the pyClass matches exact class or subclass of anyone described in pyClassInfos
     *
     * @param pyClass      pyClass
     * @param pyClassInfos pyClassInfo array
     * @return true / false
     */
    public static boolean matchesAnyClass(@NotNull PyClass pyClass, @NotNull PyClassInfo... pyClassInfos) {
        return matchesAnyExactClass(pyClass, pyClassInfos) || matchesAnySubclass(pyClass, pyClassInfos);
    }

    /**
     * judge whether the pyClass matches subclass described in pyClassInfo
     *
     * @param pyClass     pyClass
     * @param pyClassInfo pyClassInfo
     * @return true / false
     */
    public static boolean matchesSubclass(@NotNull PyClass pyClass, @NotNull PyClassInfo pyClassInfo) {
        return matchesAnySubclass(pyClass, pyClassInfo);
    }

    /**
     * judge whether the pyClass matches subclass of anyone described in pyClassInfos
     *
     * @param pyClass      pyClass
     * @param pyClassInfos pyClassInfo array
     * @return true / false
     */
    public static boolean matchesAnySubclass(@NotNull PyClass pyClass, @NotNull PyClassInfo... pyClassInfos) {
        return pyClass.getAncestorClasses(TypeEvalContext.codeAnalysis(pyClass.getProject(), pyClass.getContainingFile()))
                .stream()
                .anyMatch(superClass -> Arrays.stream(pyClassInfos)
                        .anyMatch(pyClassInfo -> pyClassInfo.getClassName().equals(superClass.getName())
                                && superClass.getContainingFile().getVirtualFile().toString().endsWith(pyClassInfo.getFilepathSuffix()))
                );
    }

    /**
     * judge whether the pyClass matches exact class described in pyClassInfo
     *
     * @param pyClass     pyClass
     * @param pyClassInfo pyClassInfo
     * @return true / false
     */
    public static boolean matchesExactClass(@NotNull PyClass pyClass, @NotNull PyClassInfo pyClassInfo) {
        return matchesAnyExactClass(pyClass, pyClassInfo);
    }

    /**
     * judge whether the pyClass matches exact class of anyone described in pyClassInfos
     *
     * @param pyClass      pyClass
     * @param pyClassInfos pyClassInfo array
     * @return true / false
     */
    public static boolean matchesAnyExactClass(@NotNull PyClass pyClass, @NotNull PyClassInfo... pyClassInfos) {
        return Arrays.stream(pyClassInfos)
                .anyMatch(pyClassInfo -> pyClassInfo.getClassName().equals(pyClass.getName())
                        && pyClass.getContainingFile().getVirtualFile().toString().endsWith(pyClassInfo.getFilepathSuffix()));
    }
}
