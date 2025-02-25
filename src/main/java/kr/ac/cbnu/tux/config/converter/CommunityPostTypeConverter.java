package kr.ac.cbnu.tux.config.converter;

import kr.ac.cbnu.tux.enums.CommunityPostType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CommunityPostTypeConverter implements Converter<String, CommunityPostType> {
    @Override
    public CommunityPostType convert(String source) {
        return CommunityPostType.fromString(source);
    }
}
