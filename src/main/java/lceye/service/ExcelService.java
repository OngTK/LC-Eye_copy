package lceye.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import lceye.model.dto.ExcelProjectDto;
import lceye.model.mapper.ExcelProjectMapper;
import lceye.model.repository.ProjectRepository;
import lceye.model.repository.ProjectResultFileRepository;
import lceye.aop.DistributedLock;
import lceye.util.file.FileUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ExcelService {

    private final JwtService jwtService;
    private final ExcelProjectMapper excelProjectMapper;
    private final ProjectRepository projectRepository;
    private final FileUtil fileUtil;
    private final ProjectResultFileRepository projectResultFileRepository;

    /**
     * [Excel-01] 엑셀 다운로드
     *
     * @param token    로그인 토큰
     * @param pjno     프로젝트 번호
     * @param response 엑셀 다운로드용 응답
     * @author OngTK
     */
    @DistributedLock(lockKey = "#pjno")
    public boolean downloadExcel(String token, int pjno, HttpServletResponse response) {
        System.out.println("ExcelService.downloadExcel");
        System.out.println("token = " + token + ", pjno = " + pjno + ", response = " + response);
        // [1] 로그인 토큰 확인
        // [1.1] 로그인 토큰이 비어있으면 false
        if (!jwtService.validateToken(token)) return false;
        // [1.2] 토큰이 존재한다면, 토큰에서 mno(작성자) 정보 추출
        int mno = jwtService.getMnoFromClaims(token);
        int cno = jwtService.getCnoFromClaims(token);
        String mrole = jwtService.getRoleFromClaims(token);
        System.out.println("[Excel-01 Token Check] " + mno + " / " + cno + " / " + mrole);
        // [2] pjno 유효성 검사·존재여부 확인
        // [2.1] pjno 유효성 검사
        if (pjno == 0) return false;
        if (!projectRepository.existsById(pjno)) return false;
        // [2.2] pjno가 존재하면 cno와 pjno 기반 project 정보 조회
        ExcelProjectDto excelProjectDto = new ExcelProjectDto();
        // [2.3] mrole에 따른 서로 다른 조회
        if (mrole.equals("WORKER")) {
            // [2.3.1] WORKER 는 본인 mno에 한정하여 조회되어야 함
            excelProjectDto = excelProjectMapper.readByMnoAndPjno(mno, pjno);
            System.out.println(excelProjectDto);
        } else if (mrole.equals("ADMIN") || mrole.equals("MANAGER")) {
            // [2.3.2] ADMIN, MANAGER 는 본인과 작성자가 아니더라도 cno가 일치하면 조회 가능
            excelProjectDto = excelProjectMapper.readByCnoAndPjno(cno, pjno);
            System.out.println(excelProjectDto);
        }

        // [3] pjno 존재시 excel 생성 및 첫번째 시트 작성
        // [3.1] excelProjectDto가 null 이면 false 리턴
        if (excelProjectDto == null) return false;
        // [3.2] Excel 작성에 필요한 객체 생성 [helper-01 실행]
        try (Workbook workbook = createExcelFile(excelProjectDto, response)) {
            // [3.3] 첫번째 시트 작성 [helper-02 실행]
            writeProjectInfo(workbook, excelProjectDto);

            // [4] pjno로 project 테이블의 pjfilename 컬럼 존재여부 확인
            String pjfilename = excelProjectDto.getPjfilename();
            if (pjfilename == null || pjfilename.isEmpty() || pjfilename.isBlank()) {
                // [4.1] pjfilename 이 empty면 현재 상태의 엑셀 출력
                workbook.write(response.getOutputStream());
                response.flushBuffer();
                return true;
            } else {
                // [4.2] pjfilename 이 있으면, s3에서 데이터를 가져오고, 엑셀을 작성
                Map<String, Object> projectJson = fileUtil.readFile("exchange", pjfilename);
                List<Map<String, Object>> exchanges = (List<Map<String, Object>>) projectJson.get("exchanges");
                writeProjectExchange(workbook, exchanges);
            }

            // [5] pjno로 projcetResultfile 테이블의 가장 최근의 레코드 존재여부 확인
            String projectResultFileName = projectResultFileRepository.returnFilename(pjno);
            // [5.1] 조회되는 파일이 없다면, 현재 상태의 엑셀 출력
            if (projectResultFileName == null || projectResultFileName.isEmpty() || projectResultFileName.isBlank()) {
                workbook.write(response.getOutputStream());
                response.flushBuffer();
                return true;
            }
            // [5.2] 조회되는 파일이 있다면, prfname 컬럼의 값으로 s3에서 데이터를 가져오고, 엑셀을 작성
            Map<String, Object> resultJson = fileUtil.readFile("result", projectResultFileName);

            // [5.3] inputList와 outputList 구별
            List<Map<String, Object>> inputList = new ArrayList<>();
            List<Map<String, Object>> outputList = new ArrayList<>();
            List<Map<String, Object>> results = (List<Map<String, Object>>) resultJson.get("results");
            for (Map<String, Object> map : results) {
                if ((boolean) map.get("isInput")) {
                    inputList.add(map);
                } else {
                    outputList.add(map);
                }
            }
            writeProjectResult(workbook, inputList, outputList);

            // [6] 최종 결과 반환
            workbook.write(response.getOutputStream());
            response.flushBuffer();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } // try-catch end
    } // func end


    /**
     * [Helper 01] Excel 파일 생성 + 파일명 생성
     *
     * @author OngTK
     */
    private Workbook createExcelFile(ExcelProjectDto excelProjectDto, HttpServletResponse response) throws IOException {
        // [1] workbook 객체 생성 (엑셀파일 생성)
        Workbook workbook = new XSSFWorkbook();
        // [2] 파일명 작성 : "프로젝트명_yyyyMMdd_hhmmss.xlsx"
        // [2.1] 프로젝트 명 추출
        String projectName = excelProjectDto.getPjname();
        System.out.println("projectName 1 = " + projectName);
        if (projectName == null || projectName.isBlank()) {
            projectName = "project";
        }
        System.out.println("projectName 2 = " + projectName);
        // [2.2] timestamp 작성
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        // [2.3] 파일명 작성 + 확장자 작성
        String fileName = projectName + "_" + timestamp + ".xlsx";
        System.out.println("fileName = " + fileName);
        // [2.4] 한글을 UTF_8 인코딩
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        System.out.println("encodedFileName = " + encodedFileName);
        // [3] 다운로드를 위해 response 객체에 response 설정
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        // [4] 반환
        return workbook;
    } // func end

    /**
     * [Helper 02] Excel 첫번째 시트 - 프로젝트 정보 작성
     *
     * @author OngTK
     */
    private void writeProjectInfo(Workbook workbook, ExcelProjectDto excelProjectDto) {
        // [1] 시트 생성 + 시트명 지정
        Sheet sheet = workbook.createSheet("프로젝트 정보");
        // [2] 작성 대상에 대해서 excelProjectDto를 Object 배열로 변환
        Object[][] projectInfo = {
                {"프로젝트 번호", excelProjectDto.getPjno()},
                {"프로젝트 명", defaultString(excelProjectDto.getPjname())},
                {"프로젝트 설명", defaultString(excelProjectDto.getPjdesc())},
                {"생산량", excelProjectDto.getPjamount()},
                {"단위 그룹 이름", defaultString(excelProjectDto.getUgname())},
                {"상세 단위 이름", defaultString(excelProjectDto.getUnit())},
                {"작성자 이름", defaultString(excelProjectDto.getMname())},
                {"작성자 이메일", defaultString(excelProjectDto.getMemail())},
                {"사명", defaultString(excelProjectDto.getCnmae())},
                {"수정일", defaultString(excelProjectDto.getUpdatedate())},
                {"생성일", defaultString(excelProjectDto.getCreatedate())}
        };

        // [3] 2행부터 작성하기
        int startRow = 1;
        // [4] 반복문
        for (int i = 0; i < projectInfo.length; i++) {
            // [4.1] 작성할 행 생성 ( 1+i ) : 순서대로 내려가면서 작성
            Row row = sheet.createRow(startRow + i);
            // [4.2] 셀 활성화
            // [4.2.1] B열 : 컬럼명 한글로 입력할 셀
            Cell keyCell = row.createCell(1);
            // [4.2.2] C열 : 값 입력할 셀
            Cell valueCell = row.createCell(2);
            // [4.3] 데이터 입력
            // [4.3.1] B열에 데이터 입력
            keyCell.setCellValue(projectInfo[i][0].toString());
            // [4.3.2] C열에 데이터를 변수에 담은 후, Object 데이터를 숫자면 숫자로, 아니면 텍스트 형식으로 입력
            Object value = projectInfo[i][1];
            // 데이터가 Number 타입이면
            if (value instanceof Number) {
                // Double 타입으로 변환하여 입력
                valueCell.setCellValue(Double.parseDouble(value.toString()));
            } else {
                // 데이터가 null 이면 공백, 아니면 텍스트 형식으로 입력
                valueCell.setCellValue(value == null ? "" : value.toString());
            }
        }

        // [4.4] 열 크기 자동 조절
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
    } // func end

    /**
     * [Helper 03] Excel 두번째 시트 - 프로젝트 교환물 정보 작성
     *
     * @author OngTK
     */
    private void writeProjectExchange(Workbook workbook, List<?> exchanges) {
        // [1] 시트 생성 + 시트명 지정
        Sheet sheet = workbook.createSheet("투입물·산출물");
        // [2] 컬럼명 작성 (B2 시작)
        Row header = sheet.createRow(1);
        String[] headers = {"방향", "명칭", "양", "단위", "프로세스명", "프로세스uuid"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(1 + i);
            cell.setCellValue(headers[i]);
        }
        // [3] 데이터 입력 (B3 시작)
        int rowIndex = 2;
        // [3.1] 반복문 obj(map)를 하나씩 꺼냄
        for (Object obj : exchanges) {
            if (!(obj instanceof Map<?, ?> map)) continue;
            // [3.1.1] 셀 활성화 + 작성할 행 생성
            Row row = sheet.createRow(rowIndex++);
            // [3.1.2] 방향
            boolean isInput = false;
            Object isInputObj = map.get("isInput");
            if (isInputObj instanceof Boolean boolVal) {
                isInput = boolVal;
            } else if (isInputObj instanceof Number numVal) {
                isInput = numVal.intValue() != 0;
            }
            String direction = isInput ? "투입물" : "산출물";
            // [3.1.3] 명칭
            String pjename = defaultString(map.get("pjename") == null ? null : map.get("pjename").toString());
            // [3.1.4] 양
            String amountObj = defaultString(map.get("pjeamount") == null ? null : map.get("pjeamount").toString());
            double amount = Double.parseDouble(amountObj);
            System.out.println("amount = " + amount);
            // [3.1.5] 단위
            String uname = defaultString(map.get("uname") == null ? null : map.get("uname").toString());
            // [3.1.6] 프로세스명
            String pname = defaultString(map.get("pname") == null ? null : map.get("pname").toString());
            // [3.1.7] 프로세스 uuid
            String puuid = defaultString(map.get("puuid") == null ? null : map.get("puuid").toString());

            // [3.1.8] 데이터 입력
            row.createCell(1).setCellValue(direction);
            row.createCell(2).setCellValue(pjename);
            row.createCell(3).setCellValue(amount);
            row.createCell(4).setCellValue(uname);
            row.createCell(5).setCellValue(pname);
            row.createCell(6).setCellValue(puuid);
        }

        // [3.2] 열 크기 자동 조절 (B~G)
        for (int col = 1; col <= 6; col++) {
            sheet.autoSizeColumn(col);
        }
    } // func end

    /**
     * [Helper 04] Excel 세번째 시트 - 프로젝트 결과 정보 작성
     *
     * @author OngTK
     */
    private void writeProjectResult(Workbook workbook,
                                    List<Map<String, Object>> inputList,
                                    List<Map<String, Object>> outputList) {
        System.out.println("ExcelService.writeProjectResult");

        // [1] 시트 생성 + 시트명 지정
        Sheet sheet = workbook.createSheet("LCI결과");

        // [2] 제목 설정 (B2 INPUT, G2 OUTPUT)
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);
        Row typeRow = sheet.createRow(1);
        Cell inputTitle = typeRow.createCell(1);
        inputTitle.setCellValue("INPUT");
        inputTitle.setCellStyle(boldStyle);
        Cell outputTitle = typeRow.createCell(6);
        outputTitle.setCellValue("OUTPUT");
        outputTitle.setCellStyle(boldStyle);

        // [3] 컬럼명 작성 (B3, G3)
        Row headerRow = sheet.createRow(2);
        String[] headers = {"흐름명", "양", "단위", "UUID"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(1 + i).setCellValue(headers[i]);
            headerRow.createCell(6 + i).setCellValue(headers[i]);
        }

        // [4] INPUT 데이터 입력 (B4 시작)
        int inputRowIndex = 3;
        for (Map<String, Object> map : inputList) {
            Row row = sheet.createRow(inputRowIndex++);
            row.createCell(1).setCellValue(defaultString(map.get("fname") == null ? null : map.get("fname").toString()));
            Object amountObj = map.get("amount");
            double amount = 0;
            if (amountObj instanceof Number num) {
                amount = num.doubleValue();
            }
            row.createCell(2).setCellValue(amount);
            row.createCell(3).setCellValue(defaultString(map.get("uname") == null ? null : map.get("uname").toString()));
            row.createCell(4).setCellValue(defaultString(map.get("fuuid") == null ? null : map.get("fuuid").toString()));
        }

        // [6] OUTPUT 데이터 입력 (G4 시작)
        int outputRowIndex = 3;
        for (Map<String, Object> map : outputList) {
            Row row = sheet.getRow(outputRowIndex);
            if (row == null) {
                row = sheet.createRow(outputRowIndex);
            }
            outputRowIndex++;
            row.createCell(6).setCellValue(defaultString(map.get("fname") == null ? null : map.get("fname").toString()));
            Object amountObj = map.get("amount");
            double amount = 0;
            if (amountObj instanceof Number num) {
                amount = num.doubleValue();
            }
            row.createCell(7).setCellValue(amount);
            row.createCell(8).setCellValue(defaultString(map.get("uname") == null ? null : map.get("uname").toString()));
            row.createCell(9).setCellValue(defaultString(map.get("fuuid") == null ? null : map.get("fuuid").toString()));
        }

        // [7] 열 크기 자동 조절 (B~J 까지)
        for (int col = 1; col <= 9; col++) {
            sheet.autoSizeColumn(col);
        }

    } // func end

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
} // class end