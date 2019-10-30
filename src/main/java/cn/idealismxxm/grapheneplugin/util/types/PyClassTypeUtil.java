package cn.idealismxxm.grapheneplugin.util.types;

import cn.idealismxxm.grapheneplugin.enums.pyclass.PyClassInfo;
import cn.idealismxxm.grapheneplugin.util.PyClassUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyTypedElement;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PyClassTypeUtil extends PyTypeUtil {

    /**
     * get python class type of psiElement
     *
     * @param element PsiElement
     * @return null for Any type
     */
    @Nullable
    public static PyClassType getClassType(@NotNull PsiElement element) {
        return (PyClassType) Optional.of(element)
                .filter(psiElement -> psiElement instanceof PyTypedElement)
                .map(psiElement -> TypeEvalContext.userInitiated(psiElement.getProject(), psiElement.getContainingFile()))
                .map(typeEvalContext -> typeEvalContext.getType((PyTypedElement) element))
                .filter(pyType -> pyType instanceof PyClassType)
                .orElse(null);
    }

    /**
     * get python class type of targetToValue
     *
     * @param targetToValue target to value mapping
     * @return null for Any type (use annotation type first)
     */
    @Nullable
    public static PyClassType getClassType(@NotNull Pair<PyExpression, PyExpression> targetToValue) {
        return Optional.ofNullable(getClassType(targetToValue.getFirst()))
                .orElse(getClassType(targetToValue.getSecond()));
    }

    /**
     * judge whether the type of element matches exact class or subclass described in pyClassInfos
     *
     * @param element     PsiElement
     * @param pyClassInfo pyClassInfo
     * @return true / false
     */
    public static boolean typeMatchesClass(@NotNull PsiElement element, @NotNull PyClassInfo pyClassInfo) {
        return typeMatchesAnyClass(element, pyClassInfo);
    }

    /**
     * judge whether the type of element matches exact class or subclass of anyone described in pyClassInfos
     *
     * @param element      PsiElement
     * @param pyClassInfos pyClassInfo array
     * @return true / false
     */
    public static boolean typeMatchesAnyClass(@NotNull PsiElement element, @NotNull PyClassInfo... pyClassInfos) {
        // get pyClass first to avoid calling getClassType twice
        return Optional.ofNullable(getClassType(element))
                .map(PyClassType::getPyClass)
                .map(pyClass -> PyClassUtil.matchesAnyClass(pyClass, pyClassInfos))
                .orElse(false);
    }

    /**
     * judge whether the type of element matches subclass described in pyClassInfo
     *
     * @param element     PsiElement
     * @param pyClassInfo pyClassInfo
     * @return true / false
     */
    public static boolean typeMatchesSubclass(@NotNull PsiElement element, @NotNull PyClassInfo pyClassInfo) {
        return typeMatchesAnySubclass(element, pyClassInfo);
    }

    /**
     * judge whether the type of element matches subclass of anyone described in pyClassInfos
     *
     * @param element      PsiElement
     * @param pyClassInfos pyClassInfo array
     * @return true / false
     */
    public static boolean typeMatchesAnySubclass(@NotNull PsiElement element, @NotNull PyClassInfo... pyClassInfos) {
        return Optional.ofNullable(getClassType(element))
                .map(PyClassType::getPyClass)
                .map(pyClass -> PyClassUtil.matchesAnyClass(pyClass, pyClassInfos))
                .orElse(false);
    }

    /**
     * judge whether the type of element matches exact class described in pyClassInfo
     *
     * @param element     PsiElement
     * @param pyClassInfo pyClassInfo
     * @return true / false
     */
    public static boolean typeMatchesExactClass(@NotNull PsiElement element, @NotNull PyClassInfo pyClassInfo) {
        return typeMatchesAnyExactClass(element, pyClassInfo);
    }

    /**
     * judge whether the type of element matches exact class of anyone described in pyClassInfos
     *
     * @param element      PsiElement
     * @param pyClassInfos pyClassInfo array
     * @return true / false
     */
    public static boolean typeMatchesAnyExactClass(@NotNull PsiElement element, @NotNull PyClassInfo... pyClassInfos) {
        return Optional.ofNullable(getClassType(element))
                .map(PyClassType::getPyClass)
                .map(pyClass -> PyClassUtil.matchesAnyExactClass(pyClass, pyClassInfos))
                .orElse(false);
    }

    /**
     * judge whether the type of targetToValue matches exact class or subclass described in pyClassInfos
     *
     * @param targetToValue target to value mapping
     * @param pyClassInfo   pyClassInfo
     * @return true / false
     */
    public static boolean typeMatchesClass(@NotNull Pair<PyExpression, PyExpression> targetToValue, @NotNull PyClassInfo pyClassInfo) {
        return typeMatchesAnyClass(targetToValue, pyClassInfo);
    }

    /**
     * judge whether the type of targetToValue matches exact class or subclass of anyone described in pyClassInfos
     *
     * @param targetToValue target to value mapping
     * @param pyClassInfos  pyClassInfo array
     * @return true / false
     */
    public static boolean typeMatchesAnyClass(@NotNull Pair<PyExpression, PyExpression> targetToValue, @NotNull PyClassInfo... pyClassInfos) {
        // get pyClass first to avoid calling getClassType twice
        return Optional.ofNullable(getClassType(targetToValue))
                .map(PyClassType::getPyClass)
                .map(pyClass -> PyClassUtil.matchesAnyClass(pyClass, pyClassInfos))
                .orElse(false);
    }

    /**
     * judge whether the type of targetToValue matches subclass described in pyClassInfo
     *
     * @param targetToValue target to value mapping
     * @param pyClassInfo   pyClassInfo
     * @return true / false
     */
    public static boolean typeMatchesSubclass(@NotNull Pair<PyExpression, PyExpression> targetToValue, @NotNull PyClassInfo pyClassInfo) {
        return typeMatchesAnySubclass(targetToValue, pyClassInfo);
    }

    /**
     * judge whether the type of targetToValue matches subclass of anyone described in pyClassInfos
     *
     * @param targetToValue target to value mapping
     * @param pyClassInfos  pyClassInfo array
     * @return true / false
     */
    public static boolean typeMatchesAnySubclass(@NotNull Pair<PyExpression, PyExpression> targetToValue, @NotNull PyClassInfo... pyClassInfos) {
        return Optional.ofNullable(getClassType(targetToValue))
                .map(PyClassType::getPyClass)
                .map(pyClass -> PyClassUtil.matchesAnyClass(pyClass, pyClassInfos))
                .orElse(false);
    }

    /**
     * judge whether the type of targetToValue matches exact class described in pyClassInfo
     *
     * @param targetToValue target to value mapping
     * @param pyClassInfo   pyClassInfo
     * @return true / false
     */
    public static boolean typeMatchesExactClass(@NotNull Pair<PyExpression, PyExpression> targetToValue, @NotNull PyClassInfo pyClassInfo) {
        return typeMatchesAnyExactClass(targetToValue, pyClassInfo);
    }

    /**
     * judge whether the type of targetToValue matches exact class of anyone described in pyClassInfos
     *
     * @param targetToValue target to value mapping
     * @param pyClassInfos  pyClassInfo array
     * @return true / false
     */
    public static boolean typeMatchesAnyExactClass(@NotNull Pair<PyExpression, PyExpression> targetToValue, @NotNull PyClassInfo... pyClassInfos) {
        return Optional.ofNullable(getClassType(targetToValue))
                .map(PyClassType::getPyClass)
                .map(pyClass -> PyClassUtil.matchesAnyExactClass(pyClass, pyClassInfos))
                .orElse(false);
    }
}
