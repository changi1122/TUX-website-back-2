package kr.ac.cbnu.tux.enums;

import kr.ac.cbnu.tux.domain.user.entity.User;

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

    // 카테고리별 읽기 권한
    public boolean cannotReadBy(User user) {
        if (this == GALLERY) {
            return false;
        }
        else {
            return (user == null);
        }
    }

    // 전체 목록 읽기 권한
    public static boolean cannotListBy(User user) {
        return (user == null);
    }
}