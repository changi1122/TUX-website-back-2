package kr.ac.cbnu.tux.enums;

import kr.ac.cbnu.tux.domain.User;

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

    // TODO 카테고리별 읽기 권한
    public boolean canRead(User user) throws Exception {
        throw new Exception("not implemented yeu");
    }
}