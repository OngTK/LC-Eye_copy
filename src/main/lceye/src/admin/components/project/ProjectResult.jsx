
import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import { useQuery } from "@tanstack/react-query";
import "../../../assets/css/ProjectResult.css";
import { Pagination } from "@mui/material";
import { Select, Option } from "@mui/joy";
import ProjectListTable from "./ProjectListTable.jsx";
import Button from "@mui/joy/Button";

export default function ProjectResult(props) {
    const { pjno, isOpen } = props;

    const [rowsPerPage, setRowsPerPage] = useState(100);
    const [isDownloading, setIsDownloading] = useState(false);

    const [inputPage, setInputPage] = useState(1);
    const [outputPage, setOutputPage] = useState(1);
    const [inputPageInput, setInputPageInput] = useState("1");
    const [outputPageInput, setOutputPageInput] = useState("1");

    // pjnoParam(프로젝트 번호)을 받아서 서버에서 LCI input/output 리스트를 가져오는 함수
    const fetchLci = async (pjnoParam) => {
        try {
            // 1) 서버 호출
            const res = await axios.get("http://localhost:8080/api/lci", {
                params: { pjno: pjnoParam },
                withCredentials: true,
            });

            // 2) 서버 응답 데이터 안전 처리
            const inputListRaw = Array.isArray(res?.data?.inputList)
                ? res.data.inputList
                : [];

            const outputListRaw = Array.isArray(res?.data?.outputList)
                ? res.data.outputList
                : [];

            // 3) 정렬 유틸 함수
            const sortByFnameAsc = (arr) =>
                [...arr].sort((a, b) =>
                    String(a?.fname ?? "").localeCompare(String(b?.fname ?? ""))
                );

            // 4) 성공 시: 정렬된 데이터 반환
            return {
                inputList: sortByFnameAsc(inputListRaw),
                outputList: sortByFnameAsc(outputListRaw),
            };

        } catch (error) {
            // [핵심] 에러 발생 시(네트워크 오류, 404, 500 등)
            // 콘솔에 에러를 남기고, 빈 배열을 리턴하여 화면이 깨지지 않게 방어
            console.error("LCI 데이터 조회 실패:", error);
            console.log(error.status)

            return {
                inputList: [],
                outputList: [],
            };
        }
    };

    // React Query로 LCI 데이터 캐싱/조회 =============================
    const { data: lciData, refetch } = useQuery({
        // 1) 캐시 키
        //    - 프로젝트 번호(pjno)가 바뀌면 다른 캐시로 취급
        queryKey: ["project", pjno, "lci"],

        // 2) 실제 데이터를 가져오는 함수
        //    - fetchLci를 pjno로 실행
        queryFn: () => fetchLci(pjno),

        // 3) enabled가 true일 때만 query 실행
        //    - 모달이 열려 있고(isOpen)
        //    - pjno가 존재할 때만 조회
        enabled: isOpen && !!pjno,

        // 4) staleTime: 30초 동안은 "신선한 데이터"로 간주
        //    - 30초 내에는 refetch 안하고 캐시 재사용
        staleTime: 1000 * 30,

        // 5) 브라우저 포커스 될 때 자동 재조회 끄기
        refetchOnWindowFocus: false,
        retry: false
    });


    // 모달 열릴 때 페이지 관련 상태 초기화 =============================
    useEffect(() => {
        // 모달 열림 + 프로젝트 번호 있을 때만 초기화 수행
        if (isOpen && pjno) {
            // input/output 페이지를 1페이지로 리셋
            setInputPage(1);
            setOutputPage(1);

            // 페이지 입력창(텍스트)도 "1"로 리셋
            setInputPageInput("1");
            setOutputPageInput("1");

            refetch();
        }

        // 의존성 배열:
        // - isOpen, pjno가 변할 때(모달 열리거나 pjno 변경)
        // - lciData가 갱신될 때도 리셋하게 되어 있음
    }, [isOpen, pjno, lciData]);


    // 수치 포맷 처리 ===================================================
    // LCI amount 값을 지수 표기법(exponential)으로 통일해서 보여주기 위한 함수
    const formatAmount = (value) => {
        // 1) Number로 변환
        const num = Number(value);

        // 2) 숫자로 정상 변환 불가하면 원본(또는 빈문자열) 그대로 반환
        if (!Number.isFinite(num)) return value ?? "";

        // 3) 0은 e 표기 시 지수값이 애매할 수 있어 고정 포맷 사용
        if (num === 0) return "0.000e+00";

        // 4) 지수표기법, 소수점 3자리로 통일
        //    예: 12.3456 -> "1.235e+01"
        return num.toExponential(3);
    };

    // input 데이터 가공 ===============================================
    // lciData.inputList가 변경될 때만 다시 계산
    const lciInputData = useMemo(() => {
        // 1) lciData.inputList가 배열인지 확인, 아니면 빈 배열
        const arr = Array.isArray(lciData?.inputList) ? lciData.inputList : [];

        // 2) amount를 formatAmount로 변환한 새 배열 반환
        //    - 원본 객체는 유지하고 amount만 덮어씀
        return arr.map((item) => ({
            ...item,
            amount: formatAmount(item.amount),
        }));
    }, [lciData]);

    // output 데이터 가공 ==============================================
    // lciData.outputList가 변경될 때만 다시 계산
    const lciOutputData = useMemo(() => {
        const arr = Array.isArray(lciData?.outputList) ? lciData.outputList : [];
        return arr.map((item) => ({
            ...item,
            amount: formatAmount(item.amount),
        }));
    }, [lciData]);
    // rowsPerPage(페이지당 행 수)나 lciInputData(입력 데이터)가 바뀌었을 때,
    // 현재 inputPage가 "총 페이지 수"를 초과하면 마지막 페이지로 자동 보정하는 useEffect
    useEffect(() => {
        // 1) 현재 데이터 기준 input의 총 페이지 수 계산
        //    - rowsPerPage가 0 이하이면 페이지 의미가 없으므로 1로 처리
        //    - 데이터가 0개여도 최소 1페이지는 유지
        const totalPages =
            rowsPerPage > 0
                ? Math.max(1, Math.ceil(lciInputData.length / rowsPerPage))
                : 1;

        // 2) 현재 페이지가 총 페이지를 넘는지 검사
        //    - 예: 현재 5페이지인데 데이터가 줄어서 3페이지까지만 존재하는 경우
        if (inputPage > totalPages) {

            // 3) 이동해야 할 "다음 페이지(next)" 계산
            //    - rowsPerPage가 유효하고 데이터가 있으면 마지막 페이지로
            //    - 아니면 1페이지로
            const next =
                rowsPerPage > 0 && lciInputData.length > 0
                    ? Math.ceil(lciInputData.length / rowsPerPage)
                    : 1;

            // 4) 상태 보정: inputPage를 next로 이동
            setInputPage(next);

            // 5) 페이지 입력 창(텍스트)도 동일하게 보정
            setInputPageInput(String(next));
        }
    }, [rowsPerPage, lciInputData, inputPage]);

    // output 페이지도 위와 동일한 이유로 자동 보정하는 useEffect
    useEffect(() => {
        const totalPages =
            rowsPerPage > 0
                ? Math.max(1, Math.ceil(lciOutputData.length / rowsPerPage))
                : 1;
        if (outputPage > totalPages) {
            const next =
                rowsPerPage > 0 && lciOutputData.length > 0
                    ? Math.ceil(lciOutputData.length / rowsPerPage)
                    : 1;

            setOutputPage(next);
            setOutputPageInput(String(next));
        }
    }, [rowsPerPage, lciOutputData, outputPage]);

    // 현재 input 데이터 기준 "총 페이지 수"
    // - rowsPerPage가 유효하고 데이터가 있으면 ceil로 계산
    // - 그렇지 않으면 1페이지
    const totalInputPages =
        rowsPerPage > 0 && lciInputData.length > 0
            ? Math.ceil(lciInputData.length / rowsPerPage)
            : 1;

    // 현재 output 데이터 기준 "총 페이지 수"
    const totalOutputPages =
        rowsPerPage > 0 && lciOutputData.length > 0
            ? Math.ceil(lciOutputData.length / rowsPerPage)
            : 1;

    // input의 현재 페이지에서 slice할 시작/끝 인덱스 계산
    // - 예: rowsPerPage=10, inputPage=2면
    //   start=10, end=20 → 11~20번째 데이터
    const inputStartIndex = (inputPage - 1) * rowsPerPage;
    const inputEndIndex = inputStartIndex + rowsPerPage;

    // output의 현재 페이지 slice 시작/끝 인덱스
    const outputStartIndex = (outputPage - 1) * rowsPerPage;
    const outputEndIndex = outputStartIndex + rowsPerPage;

    // 실제 화면에 보여줄 input 페이지 데이터 잘라내기
    const paginatedInputData = lciInputData.slice(
        inputStartIndex,
        inputEndIndex
    );

    // 실제 화면에 보여줄 output 페이지 데이터 잘라내기
    const paginatedOutputData = lciOutputData.slice(
        outputStartIndex,
        outputEndIndex
    );

    // 페이지 번호가 범위를 벗어나지 않도록 보정(clamp)하는 유틸 함수
    // - total이 0이거나 없으면 무조건 1
    // - page가 NaN/undefined/null이면 1
    // - 그 외에는 [1 ~ total] 범위로 강제 맞춤
    const clampPage = (page, total) => {
        if (!total || total < 1) return 1;
        if (!page || Number.isNaN(page)) return 1;
        return Math.min(Math.max(page, 1), total);
    };
    // 페이지 점프(직접 입력한 페이지로 이동) 처리 =========================
    // target 값에 따라 input / output 중 어느 테이블의 페이지를 이동할지 결정
    const handleJumpPage = (target) => {

        // 1) Input 영역 페이지 점프
        if (target === "input") {

            // 1-1) input 페이지 입력창의 문자열 공백 제거
            const value = inputPageInput.trim();

            // 1-2) 아무것도 입력 안 했으면 이동하지 않음
            if (!value) return;

            // 1-3) 문자열 → 정수로 변환
            const num = parseInt(value, 10);

            // 1-4) 숫자로 변환 실패(NaN)면 이동하지 않음
            if (Number.isNaN(num)) return;

            // 1-5) clampPage로 페이지 범위를 보정
            //      - 1 미만이면 1로, 총 페이지 초과면 마지막 페이지로
            const page = clampPage(num, totalInputPages);

            // 1-6) 실제 페이지 상태 변경
            setInputPage(page);

            // 1-7) 입력창 값도 보정된 페이지로 동기화
            setInputPageInput(String(page));

            // 2) Output 영역 페이지 점프
        } else if (target === "output") {

            // 2-1) output 페이지 입력창의 문자열 공백 제거
            const value = outputPageInput.trim();
            if (!value) return;

            // 2-2) 문자열 → 정수 변환
            const num = parseInt(value, 10);
            if (Number.isNaN(num)) return;

            // 2-3) output 총 페이지 기준으로 clamp
            const page = clampPage(num, totalOutputPages);

            // 2-4) output 페이지/입력창 상태 갱신
            setOutputPage(page);
            setOutputPageInput(String(page));
        }
    };

    // 현재 페이지 데이터에 "no(순번)" 를 부여 ============================
    // paginatedInputData(현재 input 페이지에 해당하는 데이터) 를 기반으로
    // 화면에서 보여줄 절대 순번 no 를 만들어 추가하는 로직

    const inputRowsWithNo = paginatedInputData.map((item, idx) => ({
        ...item,                         // 기존 데이터 유지
        no: inputStartIndex + idx + 1,   // 전체 리스트 기준 절대 순번
        // inputStartIndex: 현재 페이지의 시작 인덱스(0부터)
        // idx: 페이지 내에서의 상대 인덱스(0부터)
        // +1: 사람이 보는 순번은 1부터 시작하므로 보정
    }));

    // output도 동일하게 no 부여
    const outputRowsWithNo = paginatedOutputData.map((item, idx) => ({
        ...item,
        no: outputStartIndex + idx + 1,
    }));
    // Excel download =============================================
    // 서버에서 엑셀 파일(blob)을 받아 브라우저 다운로드를 트리거하는 함수
    const downloadExcel = async () => {
        // 1) pjno가 없거나, 이미 다운로드 중이면 중복 요청 방지 차원에서 종료
        if (!pjno || isDownloading) return;

        // 2) 다운로드 시작 상태로 변경 (UI 버튼 비활성화/스피너 표시 등에 사용)
        setIsDownloading(true);

        try {
            // 3) 엑셀 다운로드 API 호출
            //    - params: pjno 전달
            //    - withCredentials: 세션/쿠키 인증 포함
            //    - responseType: "blob" => 파일(바이너리)을 Blob으로 받기 위함
            const res = await axios.get(
                "http://localhost:8080/api/excel/download",
                {
                    params: { pjno },
                    withCredentials: true,
                    responseType: "blob",
                }
            );

            // 4) 응답 헤더의 Content-Type 확인
            //    - 정상 파일이면 보통 xlsx MIME 타입
            //    - 실패/에러면 JSON 또는 text/* 로 내려오는 경우가 있음
            const contentType =
                (res.headers && res.headers["content-type"]) || "";
            const lowerContentType = contentType.toLowerCase();

            // 5) 응답이 Blob인데 content-type이 json/text면
            //    "실제 파일이 아니라 에러/상태값이 담긴 텍스트 응답"일 가능성이 높음
            const isTextResponse =
                res.data instanceof Blob &&
                (lowerContentType.includes("application/json") ||
                    lowerContentType.includes("text/"));

            // 6) 텍스트 응답 처리(=다운로드 실패/상태값 처리)
            if (isTextResponse) {
                try {
                    // 6-1) Blob을 문자열로 변환
                    const text = (await res.data.text()).trim();

                    // 6-2) 서버가 단순히 "true"를 보낸 경우
                    //      (로직상 성공 처리로 보고 그냥 종료)
                    if (text === "true") {
                        return;
                    }

                    // 6-3) 서버가 "false"를 보낸 경우 => 실패 알림 후 종료
                    if (text === "false") {
                        alert("다운로드를 실패하였습니다.");
                        return;
                    }

                    // 6-4) JSON 형태로 오류/상태가 올 수도 있어서 파싱 시도
                    const parsed = text ? JSON.parse(text) : null;

                    // 6-5) 파싱 결과가 true면 성공으로 간주하고 종료
                    if (parsed === true) {
                        return;
                    }
                } catch (err) {
                    // 6-6) 파싱 실패 등은 콘솔 로그만 남김
                    console.error("[downloadExcel json parse error]", err);
                }

                // 6-7) 결국 텍스트 응답이면 실패 처리
                alert("다운로드를 실패하였습니다.");
                return;
            }

            // 7) 정상 파일 응답 처리
            // 7-1) 응답 blob으로 새 Blob 생성(타입이 없으면 기본 엑셀 타입 적용)
            const blob = new Blob([res.data], {
                type: contentType || "application/vnd.ms-excel",
            });

            // 7-2) Blob을 다운로드 가능한 임시 URL로 변환
            const downloadUrl = window.URL.createObjectURL(blob);

            // 7-3) a 태그를 동적으로 만들어 다운로드 실행
            const link = document.createElement("a");
            link.href = downloadUrl;

            // 7-4) 파일 이름 설정
            //      - 응답 헤더의 content-disposition에서 파일명 추출 시도
            //      - 없으면 기본 파일명 사용
            const disposition = res.headers["content-disposition"] || "";
            const match = disposition.match(/filename\\*=UTF-8''(.+)|filename="?([^\";]+)"?/i);
            const encoded = match?.[1] || match?.[2];
            const fileName = encoded ? decodeURIComponent(encoded) : `project-${pjno}.xlsx`;
            link.setAttribute("download", fileName);

            // 7-5) DOM에 붙이고 강제 클릭으로 다운로드 트리거
            document.body.appendChild(link);
            link.click();

            // 7-6) 사용한 a 태그 제거
            link.remove();

            // 7-7) 임시 URL 해제(메모리 누수 방지)
            window.URL.revokeObjectURL(downloadUrl);

        } catch (error) {
            // 8) 요청 실패/예외 처리

            // 8-1) 권한 문제(403 Forbidden)일 경우
            if (error?.response?.status === 403) {
                alert("다운로드를 실패하였습니다.");
            } else {
                // 8-2) 그 외 네트워크/서버 오류 로그 출력 후 실패 알림
                console.error("[downloadExcel error]", error);
                alert("다운로드를 실패하였습니다.");
            }
        } finally {
            // 9) 성공/실패 상관없이 다운로드 상태 해제
            setIsDownloading(false);
        }
    };

    // return =======================================================
    return (
        <>
            <div className="projectResultTopBar">
                <Button
                    variant="outlined"
                    color="success"
                    loading={isDownloading}
                    onClick={downloadExcel}
                >
                    엑셀 다운로드
                </Button>
                <div>조회 행수</div>
                <div style={{ width: "10rem" }}>
                    <Select
                        value={String(rowsPerPage)}
                        onChange={(_, newValue) => {
                            if (!newValue) return;
                            const next = parseInt(newValue, 10);
                            if (Number.isNaN(next) || next <= 0) return;
                            setRowsPerPage(next);

                            setInputPage(1);
                            setOutputPage(1);
                            setInputPageInput("1");
                            setOutputPageInput("1");
                        }}
                    >
                        <Option value="100">100개</Option>
                        <Option value="150">150개</Option>
                        <Option value="200">200개</Option>
                    </Select>

                </div>
            </div>
            <div className="projectResultBox">
                <div className="inputResultBox">
                    <div className="resultTitle">Input</div>
                    <ProjectListTable
                        columns={[
                            { id: "no", title: "No", width: 60 },
                            { id: "fname", title: "흐름명", width: 100 },
                            { id: "amount", title: "양", width: 100 },
                            { id: "uname", title: "단위", width: 60 },
                        ]}
                        data={
                            inputRowsWithNo && inputRowsWithNo.length > 0
                                ? inputRowsWithNo
                                : [{ __empty: true }]
                        }
                        rememberKey="ProjectResultInputTable"
                        sortable={false}
                        stickyFirst={false}
                    />
                    {totalInputPages > 1 && (
                        <div className="projectResultPagination">
                            <Pagination
                                count={totalInputPages}
                                page={inputPage}
                                onChange={(_, page) => {
                                    setInputPage(page);
                                    setInputPageInput(String(page));
                                }}
                                siblingCount={1}
                                boundaryCount={1}
                                showFirstButton
                                showLastButton
                                size="small"
                            />
                            <div className="projectResultPaginationControl">
                                <input
                                    type="number"
                                    min={1}
                                    max={totalInputPages}
                                    value={inputPageInput}
                                    onChange={(e) => {
                                        const onlyNumber = e.target.value.replace(
                                            /[^0-9]/g,
                                            ""
                                        );
                                        setInputPageInput(onlyNumber);
                                    }}
                                    onKeyDown={(e) => {
                                        if (e.key === "Enter") {
                                            handleJumpPage("input");
                                        }
                                    }}
                                />
                                <button
                                    type="button"
                                    className="page-jump-button"
                                    onClick={() => handleJumpPage("input")}
                                >
                                    이동
                                </button>
                            </div>
                        </div>
                    )}
                </div>
                <div className="outputResultBox">
                    <div className="resultTitle">Output</div>
                    <ProjectListTable
                        columns={[
                            { id: "no", title: "No", width: 60 },
                            { id: "fname", title: "흐름명", width: 100 },
                            { id: "amount", title: "양", width: 100 },
                            { id: "uname", title: "단위", width: 60 },
                        ]}
                        data={
                            outputRowsWithNo && outputRowsWithNo.length > 0
                                ? outputRowsWithNo
                                : [{ __empty: true }]
                        }
                        rememberKey="ProjectResultOutputTable"
                        sortable={false}
                        stickyFirst={false}
                    />
                    {totalOutputPages > 1 && (
                        <div className="projectResultPagination">
                            <Pagination
                                count={totalOutputPages}
                                page={outputPage}
                                onChange={(_, page) => {
                                    setOutputPage(page);
                                    setOutputPageInput(String(page));
                                }}
                                siblingCount={1}
                                boundaryCount={1}
                                showFirstButton
                                showLastButton
                                size="small"
                            />
                            <div className="projectResultPaginationControl">
                                <input
                                    type="number"
                                    min={1}
                                    max={totalOutputPages}
                                    value={outputPageInput}
                                    onChange={(e) => {
                                        const onlyNumber = e.target.value.replace(
                                            /[^0-9]/g,
                                            ""
                                        );
                                        setOutputPageInput(onlyNumber);
                                    }}
                                    onKeyDown={(e) => {
                                        if (e.key === "Enter") {
                                            handleJumpPage("output");
                                        }
                                    }}
                                />
                                <button
                                    type="button"
                                    className="page-jump-button"
                                    onClick={() => handleJumpPage("output")}
                                >
                                    이동
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </>
    ); // return end
} // func end
