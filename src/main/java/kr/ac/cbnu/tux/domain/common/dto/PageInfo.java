package kr.ac.cbnu.tux.domain.common.dto;

import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
public class PageInfo {
    private final int pageNumber;
    private final int size;

    public PageInfo(Pageable pageable) {
        this.pageNumber = pageable.getPageNumber();
        this.size = pageable.getPageSize();
    }
}
