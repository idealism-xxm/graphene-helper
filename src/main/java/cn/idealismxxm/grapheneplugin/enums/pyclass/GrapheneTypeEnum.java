package cn.idealismxxm.grapheneplugin.enums.pyclass;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    ENUM("Enum", "enum.py")
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
        return new GrapheneTypeEnum[] {
                FIELD,
                LIST,
                NON_NULL,
                SCALAR,
                ENUM,
        };
    }
}
