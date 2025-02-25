package kr.ac.cbnu.tux.enums;

public enum ReferenceRoomPostType {
    STUDY("study"),         // 강의/스터디
    EXAM("exam"),           // 시험 정보
    GALLERY("gallery");     // 갤러리

    private final String value;

    ReferenceRoomPostType(String value) {
        this.value = value;
    }

    public static ReferenceRoomPostType fromString(String value) {
        for (ReferenceRoomPostType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return STUDY;
    }
}