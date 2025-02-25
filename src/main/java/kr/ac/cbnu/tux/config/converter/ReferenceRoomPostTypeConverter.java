package kr.ac.cbnu.tux.config.converter;

import kr.ac.cbnu.tux.enums.ReferenceRoomPostType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ReferenceRoomPostTypeConverter implements Converter<String, ReferenceRoomPostType> {
    @Override
    public ReferenceRoomPostType convert(String source) {
        return ReferenceRoomPostType.fromString(source);
    }
}
