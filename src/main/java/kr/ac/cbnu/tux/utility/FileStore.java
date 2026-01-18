package kr.ac.cbnu.tux.utility;

import kr.ac.cbnu.tux.entity.Attachment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    public void saveAttactment(String prefix, String id, MultipartFile file) throws IOException, IllegalStateException {
        String directoryPath = fileDir + "file/" + prefix + "/" + id;
        if (!new File(directoryPath).exists()) {
            new File(directoryPath).mkdirs();
        }

        String filePath = directoryPath + "/" + Objects.requireNonNull(file.getOriginalFilename())
                .replaceAll("[\\\\/:*?\"<>| ]", "_");

        File destFile = new File(filePath);
        file.transferTo(destFile);
    }

    public void deleteAttactment(String prefix, String id, Attachment file) throws IOException {
        String directoryPath = fileDir + "file/" + prefix + "/" + id;
        String filePath = directoryPath + "/" + file.getFilename().replaceAll("[\\\\/:*?\"<>| ]", "_");

        Files.deleteIfExists(Paths.get(filePath));
    }

    public void saveBannerImage(MultipartFile file) throws IOException {
        String directoryPath = fileDir + "banner/";
        if (!new File(directoryPath).exists()) {
            new File(directoryPath).mkdirs();
        }

        String filePath = directoryPath +
                file.getOriginalFilename().replaceAll("[\\\\/:*?\"<>| ]", "_");

        File destFile = new File(filePath);
        file.transferTo(destFile);
    }

    public void deleteBannerImage(String filename) throws IOException {
        String directoryPath = fileDir + "banner/";
        String filePath = directoryPath + filename;

        Files.deleteIfExists(Paths.get(filePath));
    }

    public List<String> listBannerImages() throws IOException {
        String directoryPath = fileDir + "banner/";
        Path dir = Paths.get(directoryPath);

        if (!new File(directoryPath).exists()) {
            new File(directoryPath).mkdirs();
        }

        File directory = new File(directoryPath);
        if (directory.listFiles().length == 0) {
            copyDefaultBanner(directoryPath);
        }

        return Files.list(dir)
                .filter(Files::isRegularFile) // 파일만 필터링
                .map(Path::getFileName)       // 파일 이름만 추출
                .map(Path::toString)          // 문자열 변환
                .filter(name -> name.matches(".*\\.(png|jpg|jpeg|gif|bmp|webp)$")) // 이미지 확장자 필터링
                .sorted()                      // 문자열 기준 정렬
                .collect(Collectors.toList());
    }

    public String getBannerPath(String filename) {
        return fileDir + "banner/" + filename;
    }

    
    // 배너 파일이 없을 시 기본 배너 복사
    private void copyDefaultBanner(String directoryPath) {
        String resourcePath = "/default/banner/01.jpg";
        Path targetPath = Paths.get(directoryPath, "01.jpg");

        if (Files.exists(targetPath)) {
            return;
        }

        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("리소스 파일을 찾을 수 없음: " + resourcePath);
            }
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
