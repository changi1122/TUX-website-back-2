package kr.ac.cbnu.tux.domain.referenceroom.factory;

import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;

public class ReferenceRoomFactory {

    public static ReferenceRoomRequest createRequest(String title, String body, short editorVersion) {
        return ReferenceRoomRequest.builder()
                .title(title)
                .body(body)
                .editorVersion(editorVersion)
                .isAnonymized(false)
                .lecture("강의")
                .semester("학기")
                .professor("교수")
                .build();
    }

    public static ReferenceRoomRequest createUpdateRequest(String title, String body, short editorVersion) {
        return ReferenceRoomRequest.builder()
                .title(title)
                .body(body)
                .editorVersion(editorVersion)
                .isAnonymized(true)
                .lecture("강의수정")
                .semester("학기수정")
                .professor("교수수정")
                .build();
    }
}
