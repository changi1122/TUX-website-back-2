package kr.ac.cbnu.tux.domain.user.enums;

public enum UserRole {
    GUEST("GUEST"),
    USER("USER"),
    MANAGER("MANAGER"),
    ADMIN("ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public static UserRole fromString(String value) {
        for (UserRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        return GUEST;
    }
}
