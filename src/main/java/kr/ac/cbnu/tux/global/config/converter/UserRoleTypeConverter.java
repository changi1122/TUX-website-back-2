package kr.ac.cbnu.tux.global.config.converter;

import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserRoleTypeConverter implements Converter<String, UserRole> {
    @Override
    public UserRole convert(String source) {
        return UserRole.of(source);
    }
}
