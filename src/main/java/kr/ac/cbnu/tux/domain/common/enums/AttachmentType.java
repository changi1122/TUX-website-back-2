package kr.ac.cbnu.tux.domain.common.enums;

public enum AttachmentType {
    COMMUNITY("community"),
    REFERENCEROOM("referenceroom");

    private final String value;

    AttachmentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AttachmentType of(String type) {
        for (AttachmentType attachmentType : values()) {
            if (attachmentType.value.equalsIgnoreCase(type)) {
                return attachmentType;
            }
        }
        return COMMUNITY;
    }
}
