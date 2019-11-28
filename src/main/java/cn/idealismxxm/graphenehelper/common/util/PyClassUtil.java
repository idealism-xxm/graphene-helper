package cn.idealismxxm.graphenehelper.common.util;

import cn.idealismxxm.graphenehelper.common.enums.pyclass.PyClassInfo;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Nullable
    public static PyClass getContextClass(@NotNull Editor editor, @NotNull PsiFile file) {
        return Optional.of(editor)
                .filter($ -> file instanceof PyFile)
                .map(Editor::getCaretModel)
                .map(CaretModel::getOffset)
                .map(file::findElementAt)
                .map(psiElement -> Optional.of(psiElement)
                        .map(element -> PsiTreeUtil.getParentOfType(psiElement, PyClass.class))
                        .orElseGet(() -> (PyClass) Optional.of(psiElement)
                                .filter(element -> element instanceof PsiWhiteSpace)
                                .map(PsiElement::getPrevSibling)
                                .filter(element -> element instanceof PyClass)
                                .orElse(null)
                        ))
                .orElse(null);
    }

    /**
     * Get all function names
     * @param pyClass PyClass
     * @return set of all function names
     */
    @NotNull
    public static Set<String> getAllFunctionNames(@NotNull PyClass pyClass) {
        return Stream.of(pyClass)
                .map(PyClass::getMethods)
                .flatMap(Arrays::stream)
                .map(PyFunction::getName)
                .collect(Collectors.toSet());
    }
}
