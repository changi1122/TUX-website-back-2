package kr.ac.cbnu.tux.domain.referenceroom.factory;

import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;

public class ReferenceRoomFactory {

    public static ReferenceRoomRequest createRequest(String title, String body, short editorVersion) {
        return ReferenceRoomRequest.builder()
                .title(title)
                .body(body)
                .editorVersion(editorVersion)
                .isAnonymized(false)
                .lecture("")
                .semester("")
                .professor("")
                .build();
    }
}
