package kr.ac.cbnu.tux.global.config.converter;

import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ReferenceRoomPostTypeConverter implements Converter<String, ReferenceRoomPostType> {
    @Override
    public ReferenceRoomPostType convert(String source) {
        return ReferenceRoomPostType.of(source);
    }
}
