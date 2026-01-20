package kr.ac.cbnu.tux.utility;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class BannerListStore {

    private final FileStore fileStore;
    private static volatile List<String> bannerFileList; // 매번 파일시스템 조회를 막기 위해 배너 파일 목록을 유지

    @PostConstruct
    public void loadBannerFiles() throws IOException {
        updateBannerFileList();
    }

    public List<String> getList() {
        return bannerFileList;
    }

    public void updateList() throws IOException {
        updateBannerFileList();
    }

    // 파일시스템에서 배너 파일 목록을 조회하고 bannerFileList 업데이트
    private synchronized void updateBannerFileList() throws IOException {
        bannerFileList = fileStore.listBannerImages();
    }
}
