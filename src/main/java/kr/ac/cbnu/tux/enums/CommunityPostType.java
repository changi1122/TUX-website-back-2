package kr.ac.cbnu.tux.enums;

public enum CommunityPostType {
    NOTICE("notice"),                   // 공지사항
    TEAMRECRUITMENT("teamrecruitment"), // 팀원 모집
    CONTEST("contest"),                 // 대회/공모전
    JOB("job"),                         // 채용/취업
    FREE("free");                       // 자유

    private final String value;

    CommunityPostType(String value) {
        this.value = value;
    }

    public static CommunityPostType fromString(String type) {
        for (CommunityPostType postType : values()) {
            if (postType.value.equalsIgnoreCase(type)) {
                return postType;
            }
        }
        return FREE;
    }
}