package kr.ac.cbnu.tux.utility;

import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {


    /**
     * 폴더가 존재하지 않으면 폴더를 생성한다.
     *
     * @param folder 이 폴더가 존재하는지 확인하고 생성한다.
     */
    public static void createFolderIfNotExists(File folder) {
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * 특정 폴더 안의 모든 파일과 하위 폴더를 삭제합니다.
     *
     * @param folder 삭제할 폴더
     * @return 성공적으로 삭제되었는지 여부
     */
    public static boolean deleteFolderContents(File folder) {

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return false; // 유효하지 않은 폴더 처리
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 재귀적으로 하위 폴더 삭제
                    deleteFolderContents(file);
                }
                // 파일 또는 빈 폴더 삭제
                if (!file.delete()) {
                    System.err.println("Failed to delete: " + file.getAbsolutePath());
                }
            }
        }
        return true;
    }

    public static MockMultipartFile getUploadFile() throws IOException {
        final String filename = "sky.jpg";
        final String filePath = "src/test/resources/" + filename;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        return new MockMultipartFile(
                "images",
                filename,
                "image/jpeg",
                fileInputStream
        );
    }
}
