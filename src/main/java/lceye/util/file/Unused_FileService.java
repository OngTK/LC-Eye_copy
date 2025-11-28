package lceye.util.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

@Service
@Deprecated
public class Unused_FileService {

    private static final String path = "./src/main/resources/json/";

    /**
     * json 파일 작성
     *
     * @param type exchange/result
     * @param name json 파일 이름
     * @param data json 파일에 저장할 데이터
     * @author 민성호
     */
    public boolean writeFile(String type, String name, Map<String, Object> data) {
        String fileName = type + "/" + name + ".json";
        // 파일 경로
        String filePath = path + fileName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(filePath);

            // 상위 폴더가 존재하지 않으면 생성
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdir();
            }// if end
            mapper.writeValue(file, data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }// try end
    }// func end

    /**
     * json 파일 Map 타입으로 반환
     *
     * @param type exchange/result
     * @param name json 파일 이름
     * @return Map<String, Object>
     * @author 민성호
     */
    public Map<String, Object> readFile(String type, String name) {
        String fileName = type + "/" + name + ".json";
        Map<String, Object> map = new HashMap<>();
        // 파일 경로
        String filePath = path + fileName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(filePath);
            map = mapper.readValue(file, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }// try end
        return map;
    }// func end

    /**
     * 파일 검색기능
     *
     * @param name 찾는파일명의 포함되는 문자열
     * @return List<Map < String, Object>>
     * @author 민성호
     */
    public List<Map<String, Object>> filterFile(String name) {
        File filterDir = new File(path+"exchange");
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> list = new ArrayList();
        File[] fileNameList = filterDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        });
        System.out.println("fileNameList = " + Arrays.toString(fileNameList));
        try {
            if (fileNameList != null) {
                for (File file : fileNameList) {
                    Map<String, Object> map = mapper.readValue(file, Map.class);
                    list.add(map);
                }// for end
            }// if end
        } catch (Exception e) {
            e.printStackTrace();
        }// try end
        return list;
    }// func end

    /**
     * json 파일 삭제
     *
     * @param name 파일 이름
     * @param type 파일저장된 폴더명
     * @return boolean
     * @author 민성호
     */
    public boolean deleteFile(String name, String type) {
        System.out.println("FileService.deleteFile");
        String fileName = type + "/" + name + ".json";
        String filePath = path + fileName;
        try {
            File file = new File(filePath);
            System.out.println("file = " + file);
            if (file.exists()) {
                System.out.println("파일존재 :" + file.exists());
                if (file.delete()) {
                    System.out.println("파일삭제 : " + file.delete());
                    return true;
                } else {
                    return false;
                }// if end
            } else {
                return true; // 삭제할 파일이 존재하지않으면 원하는 상태이므로 true
            }// if end
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }// try end
    }// func end

    /**
     * 프로젝트 외부 경로에 저장되어있는 Process 별 JSON 파일을 읽어와서 Map 형태로 반환
     *
     * @param uuid : 프로세스에 해당하는 uuid (컬럼명 fuuid)
     * @return Map<String, Object>
     * @author OngTK
     */
    public Map<String, Object> searchProcessJson(String uuid) {
        // [1] JSON 파일 경로와 파일명 결합
        String ProcessPath = "D:/LCA/lci_process/" + uuid + ".json";
        // [2] 반환용 map 객체 생성
        Map<String, Object> map = new HashMap<>();
        try {
            // [2] file용 mapper 객체 만들기
            ObjectMapper mapper = new ObjectMapper();
            // [3] JSON 파일 가져오기
            File file = new File(ProcessPath);
            // [4] map으로 변환
            map = mapper.readValue(file, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    } // func end
}// class end
