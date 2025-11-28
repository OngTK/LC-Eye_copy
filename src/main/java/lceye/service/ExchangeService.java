package lceye.service;

import lceye.handler.WebSocketHandler;
import lceye.model.dto.ProcessInfoDto;
import lceye.model.dto.ProjectDto;
import lceye.model.dto.UnitsDto;
import lceye.model.repository.ProjectRepository;
import lceye.aop.DistributedLock;
import lceye.util.file.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ExchangeService {

    private final FileUtil fileUtil;
    private final JwtService jwtService;
    private final ProjectService projectService;
    private final UnitsService unitsService;
    private final ProcessInfoService processInfoService;
    private final ProjectRepository projectRepository;
    private final GeminiService geminiService;
    private final WebSocketHandler socketHandler;
    private final ProjectResultFileService projectResultFileService;

    /**
     * 투입물·산출물 저장/수정
     *
     * @param exchangeList 투입물·산출물
     * @param token        작성자 토큰
     * @return boolean
     * @author 민성호
     */
    @DistributedLock(lockKey = "#pjno")
    public boolean saveInfo(Map<String, Object> exchangeList, String token, int pjno) {
        System.out.println("exchangeList = " + exchangeList + ", token = " + token);
        if (!jwtService.validateToken(token)) return false;
        int cno = jwtService.getCnoFromClaims(token);
        int mno = jwtService.getMnoFromClaims(token);

        System.out.println("pjno = " + pjno);
        ProjectDto pjDto = projectService.findByPjno(pjno);
        if (pjDto == null) return false;
        UnitsDto unitsDto = unitsService.findByUno(pjDto.getUno());
        Object exchange = exchangeList.get("exchanges");
        if (exchange instanceof List<?> exList) {
            for (Object inout : exList) {
                if (inout instanceof Map io) {

                    if (io.get("pname") != null) {
                        ProcessInfoDto pcDto = processInfoService.findByPcname(String.valueOf(io.get("pname")));
                        if(pcDto != null && pcDto.getPcuuid() != null){
                            io.put("puuid", pcDto.getPcuuid());
                        }else {
                            io.put("puuid", null);
                        } // if end
                    }
                }// if end
            }// for end
        }// if end
        exchangeList.put("pjname", pjDto.getPjname());
        exchangeList.put("mno", mno);
        exchangeList.put("pjamount", pjDto.getPjamount());
        exchangeList.put("uno", pjDto.getUno());
        exchangeList.put("pjdesc", pjDto.getPjdesc());
        exchangeList.put("uname", unitsDto.getUnit());
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
        // 파일 이름 형식 , cno_pjno_type_datetime(20251113_1600)
        String projectNumber = String.valueOf(pjno);
        String name = cno + "_" + projectNumber + "_exchange_";
        String fileName;
        if (pjDto.getPjfilename() != null && !pjDto.getPjfilename().isEmpty()) { // json 파일명 존재할때
            Map<String, Object> oldJsonFile = fileUtil.readFile("exchange", pjDto.getPjfilename());
            exchangeList.put("createdate", oldJsonFile.get("createdate"));
            exchangeList.put("updatedate", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            DateTimeFormatter change = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdate = String.valueOf(oldJsonFile.get("createdate"));
            LocalDateTime dateTime = LocalDateTime.parse(createdate, change);
            fileName = name + dateTime.format(formatter);
        } else {
            exchangeList.put("createdate", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            fileName = name + now.format(formatter);
        }// if end
        boolean result = fileUtil.uploadFile("exchange", fileName, exchangeList);
        if (result) {
            boolean results = projectService.updatePjfilename(fileName, pjno);
            if (results) return true;
        }// if end
        return false;
    }// func end

    /**
     * 로그인한 회원의 회사파일에서 일치하는 데이터 찾기
     *
     * @param clientInput 클라이언트가 입력한 투입물·산출물
     * @param token       로그인한 회원의 토큰
     * @return Map<String, Set < String>>
     * @author 민성호
     */
    public Map<String, Set<String>> autoMatchCno(List<String> clientInput, String token) {
        if (!jwtService.validateToken(token)) return null;
        int cno = jwtService.getCnoFromClaims(token);
        int mno = jwtService.getMnoFromClaims(token);
        List<String> companyFileNames = projectService.findByCno(cno);
        Set<String> inputSet = new HashSet<>(clientInput);
        Map<String, Set<String>> requestMap = new HashMap<>();
        for (String company : companyFileNames) {
            List<Map<String, Object>> list = fileUtil.searchFile(company);
            for (Map<String, Object> map : list) {
                Object obj = map.get("exchanges");
                if (obj instanceof List<?> rawList) {
                    for (Object exchangeObj : rawList) {
                        if (exchangeObj instanceof Map exchange) {
                            Object pjeNameObj = exchange.get("pjename");
                            Object pNameObj = exchange.get("pname");
                            // pjeName이 String 타입이고, Set에 포함되는지 확인
                            if (pjeNameObj instanceof String pjeName && inputSet.contains(pjeName)) {
                                String pName = pNameObj != null ? pNameObj.toString() : "N/A";
                                // 매칭된 결과를 해당 Key의 리스트에 추가 (덮어쓰기 방지)
                                requestMap.computeIfAbsent(pjeName, k -> new HashSet<>()).add(pName);
                            }// if end
                        }// if end
                    }// for end
                }// if end
            }// for end
        }// for end
        Set<String> returnSet = new HashSet<>(inputSet);
        returnSet.removeAll(requestMap.keySet());
        if (returnSet != null && !returnSet.isEmpty()) {
            // 미매칭 항목을 '매칭 중' 상태로 requestMap에 추가
            Set<String> matchingInProgressValue = Set.of("AI 매칭 중...");
            List<String> returnList = new ArrayList<>(returnSet);
            for (String pjeName : returnList) {
                requestMap.put(pjeName, matchingInProgressValue);
            }// for end
            // Gemini 호출 및 WebSocket 전송 로직
            geminiService.similarity(returnList)
                    .subscribe(resultMap -> {
                        socketHandler.sendMessage(mno,resultMap);
                    });
        }// if end
        return new HashMap<>(requestMap);
    }// func end

    /**
     * 로그인한 회원의 작성파일에서 일치하는 데이터 찾기
     *
     * @param clientInput 클라이언트가 입력한 투입물·산출물
     * @param token       로그인한 회원의 토큰
     * @return Map<String, Set < String>>
     * @author 민성호
     */
    public Map<String, Set<String>> autoMatchPjno(List<String> clientInput, String token) {
        if (!jwtService.validateToken(token)) return null;
        int mno = jwtService.getMnoFromClaims(token);
        List<ProjectDto> projectDtos = projectService.findByMno(mno);
        Set<String> inputSet = new HashSet<>(clientInput);
        Map<String, Set<String>> requestMap = new HashMap<>();
        List<String> pjnoList = projectDtos.stream().map(ProjectDto::getPjfilename).toList();
        for (String fileName : pjnoList) {
            List<Map<String, Object>> list = fileUtil.searchFile(fileName);
            for (Map<String, Object> map : list) {
                Object obj = map.get("exchanges");
                if (obj instanceof List<?> rawList) {
                    for (Object exchangeObj : rawList) {
                        if (exchangeObj instanceof Map exchange) {
                            Object pjeNameObj = exchange.get("pjename");
                            Object pNameObj = exchange.get("pname");

                            // pjeName이 문자열이고, clientInput Set에 포함되는지 확인
                            if (pjeNameObj instanceof String pjeName && inputSet.contains(pjeName)) {
                                String pName = pNameObj != null ? pNameObj.toString() : "N/A";

                                // 매칭된 결과를 리스트에 추가 (덮어쓰기 방지)
                                requestMap.computeIfAbsent(pjeName, k -> new HashSet<>()).add(pName);
                            }// if end
                        }// if end
                    }// for end
                }// if end
            }// for end
        }// for end
        Set<String> returnSet = new HashSet<>(inputSet);
        returnSet.removeAll(requestMap.keySet());
        if (returnSet != null && !returnSet.isEmpty()) {
            List<String> returnList = new ArrayList<>(returnSet);
            Map<String, Set<String>> cnoMap = autoMatchCno(returnList, token);
            requestMap.putAll(cnoMap);
        }// if end
        // 최종 반환 타입에 맞게 Map<String, Object>로 반환
        return new HashMap<>(requestMap);
    }// func end

    /**
     * json 파일 삭제
     *
     * @param token 로그인한 회원의 토큰
     * @param pjno  삭제하는 프로젝트 번호
     * @return boolean
     * @author 민성호
     */
    @DistributedLock(lockKey = "#pjno")
    public boolean clearIOInfo(String token, int pjno) {
        if (!jwtService.validateToken(token)) return false;
        ProjectDto dto = projectService.findByPjno(pjno);
        String resultFileName = projectResultFileService.getResultFileName(pjno);
        System.out.println("resultFileName = " + resultFileName);
        if (dto != null) {
            boolean result = fileUtil.deleteFile("exchange", dto.getPjfilename());
            boolean result2 = fileUtil.deleteFile("result", resultFileName);
            System.out.println("result2 = " + result2);
            // 여기서 파일 삭제 진행
            if (result) {
                boolean results = projectService.deletePjfilename(pjno);
                projectResultFileService.deleteResultFileName(pjno);
                // 여기서 DB 파일명 null 처리
                if (results) return true;
            }// if end
        }// if end
        return false;
    }// func end

    /**
     * [IO-03] 투입물·산출물 정보 조회
     * <p>
     * 프로젝트 번호를 매개변수로 받아 투입물·산출물 json파일에서 exchanges 정보를 반환한다.
     *
     * @param pjno - 조회할 프로젝트 번호
     * @return List<Map < Strig, Object>>
     * @author OngTK
     */
    @DistributedLock(lockKey = "#pjno")
    public Map<String, Object> readIOInfo(int pjno) {
        // [1] pjno로 project 테이블에 pjfile이 존재하는지 확인
        if (projectRepository.existsById(pjno)) {
            // [2] 존재하면 파일명을 받아옴
            String filename = projectRepository.findById(pjno).get().getPjfilename();
            // [3] filename으로 json 파일 불러오기
            Map<String, Object> inOutInfo = fileUtil.readFile("exchange", filename);
            System.out.println("inOutInfo = " + inOutInfo);
            if (inOutInfo == null || inOutInfo.isEmpty()) return null;
            // [4] exchanges list 가져오기
            List<Map<String, Object>> exchanges = (List<Map<String, Object>>) inOutInfo.get("exchanges");
            // [5] exchanges 에서 input과 output 리스트를 각각 만들기
            List<Map<String, Object>> InputList = new ArrayList<>();
            List<Map<String, Object>> OutputList = new ArrayList<>();
            for (Map<String, Object> map : exchanges) {
                if ((boolean) map.get("isInput")) {
                    InputList.add(map);
                } else {
                    OutputList.add(map);
                } // if end
            } // for end
            // [6] 결과 반환
            Map<String, Object> result = new HashMap<>();
            result.put("inputList", InputList);
            result.put("outputList", OutputList);

            return result;
        } // if end
        return null;
    } // func end
}// class end