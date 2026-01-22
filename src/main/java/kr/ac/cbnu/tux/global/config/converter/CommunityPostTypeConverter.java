package kr.ac.cbnu.tux.global.config.converter;

import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CommunityPostTypeConverter implements Converter<String, CommunityPostType> {
    @Override
    public CommunityPostType convert(String source) {
        return CommunityPostType.of(source);
    }
}
