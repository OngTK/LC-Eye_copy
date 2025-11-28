package lceye.util.file;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * S3 File 처리 Class
 * @author OngTK
 */
@Service
@RequiredArgsConstructor
public class FileUtil {

    // AWS S3 접근 객체
    private final AmazonS3Client amazonS3Client;
    // Jackson 라이브러리 ObjectMapper = json ↔ JAVA 객체 변환
    private final ObjectMapper objectMapper;
    // AWS S3 버킷명
    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;

    /**
     * [1] Map객체 → JSON 변환 + JSON 파일 업로드
     *
     * @param type exchange/result
     * @param name json 파일 이름
     * @param data json 파일에 저장할 데이터
     * @author OngTK
     */
    public boolean uploadFile(String type, String name, Map<String, Object> data) {
        try {
            // [1] 매개변수 중 하나라도 null 이면 false
            if (type == null || name == null || data == null) {
                return false;
            }
            // [2] type에 대한 유효성 검사
            if (!"exchange".equals(type) && !"result".equals(type)) {
                return false;
            }
            // [3] 접근 경로 지정 (buildFileUrl = helper method)
            String fileUrl = buildFileUrl(type, name);
            // [4] 매개변수 map을 jackson 라이브러리를 이용하여 json 형태로 변환
            String json = objectMapper.writeValueAsString(data);
            // [4.1] String json의 byte 길이를 확인 → 5.1 메타데이터에 사용
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            // [5] 업로드 파일의 메타데이터 기재
            // ObjectMetadata : AWS S3에 업로드하는 파일의 메타데이터를 관리하는 속성
            ObjectMetadata metadata = new ObjectMetadata();
            // [5.1] 콘텐츠 타입 선언
            metadata.setContentType("application/json");
            // [5.2] 파일 크기 선언
            metadata.setContentLength(bytes.length);
            // [6] 파일 업로드
            // inputStream = 실제 파일 데이터
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
                amazonS3Client.putObject(bucket, fileUrl, inputStream, metadata);
            }
            return true;
            // 예외처리
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    } // func end

    /**
     * [2] JSON 파일 다운로드 + JSON → Map 객체 변화
     * @param type exchange/result
     * @param name json 파일 이름
     * @return json을 Map으로 변환하여 반환
     * @author OngTK
     */
    public Map<String, Object> readFile(String type, String name) {
        try {
            // [1] 매개변수에 null 여부 확인
            if (type == null || name == null) {
                // null인 map 반환 - 수정불가
                return Map.of();
            }
            // [2] type에 대한 유효성 검사
            if (!"exchange".equals(type) && !"result".equals(type)) {
                return Map.of();
            }
            // [3] 접근 경로 지정 (buildFileUrl = helper method)
            String fileUrl = buildFileUrl(type, name);
            // [4] S3Object AWS S3 파일 객체 = S3에 저장된 파일과 그 메타 데이터를 담고 있음
            // 버킷명과 url을 매개변수로 함
            S3Object s3Object = amazonS3Client.getObject(bucket, fileUrl);
            // [5] 파일 다운로드
            // inputStream = 실제 파일 데이터
            try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
                // [6] 실제 파일 데이터(json)을 read 하여 Map Class로 변환하여 반환
                return objectMapper.readValue(inputStream, Map.class);
            }
            // 예외처리
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    /**
     * [3] exchange 내에서 파일 검색
     * @param name 찾고자하는 문자열 → 파일명의 일부
     * @return name이 포함된 파일명 리스트
     * @author OngTK
     */
    public List<Map<String, Object>> searchFile(String name){
        // [0] 반환할 List 객체 선언
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            // [1] exchange에서 파일 목록 조회하기
            String type = "exchange/";
            // ListObjectsV2Request : AWS S3 버킷 안의 객체 목록을 조회할때 사용하는 요청 객체
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucket)
                    .withPrefix(type);
            ListObjectsV2Result result;
            // [2] while 조건문을 충족하는 동안 do 블럭 내의 코드를 반복 수행 (do 실행 후 while 조건 확인)
            do {
                // [3] S3에서 request 조건대로 버킷의 객체 목록을 가져옴
                result = amazonS3Client.listObjectsV2(request);
                // 반복문
                for (S3ObjectSummary summary : result.getObjectSummaries()) {
                    // getObjectSummaries() : 파일(경로+파일명), 파일 크기, 마지막 수정일, Etag 등을 반환

                    // [4] 파일명 가져오기
                    String key = summary.getKey();
                    // [5] 확장자가 json이 아니면 continue
                    if (!key.endsWith(".json")) continue;
                    // [6] 검색어가 null이 아니고, 파일명에 name이 포함되지 않으면 continue
                    if (name != null && !name.isBlank() && !key.contains(name)) continue;

                    // [7] 위에서 통과 = 검색어가 있고, 파일명에 검색어가 포함되어 있다면
                    // s3에서 json 파일을 불러오고, map으로 변환하여 list에 삽입
                    try (S3ObjectInputStream inputStream = amazonS3Client.getObject(bucket, key).getObjectContent()) {
                        Map<String, Object> map = objectMapper.readValue(inputStream, Map.class);
                        list.add(map);
                    }
                }
                // [8] NextContinuationToken : [Present 다음 자료가 있음 / Empty 다음 자료가 없음]
                // S3는 한 번에 최대 1,000개만 반환 가능.
                // 따라서 자료가 1,000 개 이상이라면 ContinueToken 을 재설정한 후 do 반복문이 실행
                request.setContinuationToken(result.getNextContinuationToken());
                
                // [9] result.isTruncated() == 파일이 잘렸음 == 아직 읽어야할 파일이 존재함을 의미
            } while (result.isTruncated());
            // 예외처리
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 결과 반환
        return list;
    } // func end

    /**
     * [4] 파일 삭제
     * @param type exchange/result
     * @param name 파일명
     * @return ture : 파일 존재 / false : 파일 존재 X
     * @author OngTK
     */
    public boolean deleteFile(String type, String name) {
        try {
            // [1] 매개변수 유효성 검사
            if (type == null || name == null) {
                return false;
            }
            // [2] type에 유효성 검사
            if (!"exchange".equals(type) && !"result".equals(type)) {
                return false;
            }
            // [3] 접근 경로 지정
            String fileUrl = buildFileUrl(type, name);
            // [4] 해당 버킷에 파일이 존재하지 않는가? true : 존재하지 않음 / false : 존재함
            if (!amazonS3Client.doesObjectExist(bucket, fileUrl)) {
                return true; // 존재하지 않은 경우에도 true로 반환
            }
            // [5] 해당 경로의 파일이 존재하므로, 삭제
            amazonS3Client.deleteObject(bucket, fileUrl);
            // [6] true 반환
            return true;
            // 예외처리
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    } // func end

    /**
     * [5] S3 Process 폴더 내의 uuid.JSON 파일을 읽어와서 Map 형태로 반환
     * @param uuid : 프로세스에 해당하는 uuid (컬럼명 fuuid)
     * @return Map<String, Object>
     * @author OngTK
     */
    public Map<String, Object> searchProcessJson(String uuid) {
        try {
            // [1] 매개변수 유효성 검사
            if (uuid == null || uuid.isBlank()) {
                return Map.of();
            }
            // [2] 접근 경로 지정
            String fileName = uuid.endsWith(".json") ? uuid : uuid + ".json";
            fileName = "processJSON/" + fileName;
            // [3] S3에서 해당 객체 가져오기
            S3Object s3Object = amazonS3Client.getObject(bucket, fileName);
            // [4] JSON 파일을 Map으로 변환한 후 반환
            try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
                return objectMapper.readValue(inputStream, Map.class);
            }
            // 예외처리
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    } // func end

    /**
     * [Hepler 1] 파일명 생성 method
     * @param type exchange/result
     * @param name json 파일 이름
     * @author OngTK
     */
    private String buildFileUrl(String type, String name) {
        // [1] 접근 파일명(name)에 확장자(json)이 포함여부를 확인하여, .json을 붙음
        String fileName = name.endsWith(".json") ? name : name + ".json";
        // [2] type/filename.json 으로 이루어진 경로를 반환
        return type + "/" + fileName;
    }
} // class end

