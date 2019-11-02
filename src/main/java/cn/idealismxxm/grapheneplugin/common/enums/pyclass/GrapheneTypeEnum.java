package cn.idealismxxm.grapheneplugin.common.enums.pyclass;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public enum GrapheneTypeEnum implements PyClassInfo {
    FIELD("Field", "field.py"),
    LIST("List", "structures.py"),
    MOUNTED_TYPE("MountedType", "mountedtype.py"),
    MUTATION("Mutation", "mutation.py"),
    NON_NULL("NonNull", "structures.py"),
    OBJECT_TYPE("ObjectType", "objecttype.py"),
    SCALAR("Scalar", "scalars.py"),
    STRUCTURE("Structure", "structures.py"),
    UNMOUNTED_TYPE("UnmountedType", "unmountedtype.py"),
    ENUM("Enum", "enum.py"),
    INPUT_OBJECT_TYPE("InputObjectType", "inputobjecttype.py"),
    INPUT_FIELD("InputField", "inputfield.py"),
    ;

    GrapheneTypeEnum(String className, String filename) {
        this.className = className;
        this.filepathSuffix = "/graphene/types/" + filename;
    }

    private String className;
    private String filepathSuffix;

    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public String getFilepathSuffix() {
        return this.filepathSuffix;
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    public static GrapheneTypeEnum[] getResolvableGrapheneTypeEnums() {
        return new GrapheneTypeEnum[]{
                FIELD,
                LIST,
                NON_NULL,
                SCALAR,
                ENUM,
        };
    }

    @NotNull
    public static GrapheneTypeEnum[] getArgumentGrapheneTypeEnums() {
        GrapheneTypeEnum[] argumentGrapheneTypeEnums = getResolvableGrapheneTypeEnums();
        List<GrapheneTypeEnum> list = new LinkedList<>();
        Collections.addAll(list, argumentGrapheneTypeEnums);

        list.add(INPUT_OBJECT_TYPE);
        list.add(INPUT_FIELD);

        return list.toArray(new GrapheneTypeEnum[0]);
    }
}
