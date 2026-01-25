package kr.ac.cbnu.tux.domain.community.factory;

import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;

public class CommunityFactory {

    public static CommunityRequest createRequest(String title, String body, short editorVersion) {
        return CommunityRequest.builder()
                .title(title)
                .body(body)
                .editorVersion(editorVersion)
                .build();
    }
}
