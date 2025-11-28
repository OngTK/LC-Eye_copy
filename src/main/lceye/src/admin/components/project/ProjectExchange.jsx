
import { useEffect, useState } from "react";
import axios from "axios";
import Checkbox from "@mui/joy/Checkbox";
import Modal from "@mui/joy/Modal";
import Sheet from "@mui/joy/Sheet";
import Typography from "@mui/joy/Typography";
import Button from "@mui/joy/Button";
import Box from "@mui/joy/Box";
import Select from "@mui/joy/Select";
import Option from "@mui/joy/Option";
import { useSelector } from "react-redux";
import { useQuery } from "@tanstack/react-query";
import ProjectListTable from "./ProjectListTable.jsx";
import useUnits from "../../hooks/useUnits";
import "../../../assets/css/ProjectExchange.css";
import { useLoading } from "../../contexts/LoadingContext.jsx";

export default function ProjectExchange(props) {
    // 부모 컴포넌트로부터 전달받은 props
    // - pjno: 특정 프로젝트 번호 (없으면 Redux의 selectedProject에서 가져옴)
    // - isOpen: 모달 열림 여부
    // - onCalcSuccess: 계산 완료 후 부모에 알려주기 위한 콜백
    const { pjno, isOpen, onCalcSuccess } = props;

    // Redux에서 현재 선택된 프로젝트 정보 가져오기
    const selectedProject = useSelector(
        (state) => state.project?.selectedProject
    );

    // Redux에서 mno 값 가져오기
    const mno = useSelector((state) => state.admin.isLogin.mno);

    // 프로젝트 번호 결정: props → Redux → null 순으로 우선순위 적용
    const effectivePjno = pjno ?? selectedProject?.pjno ?? null;

    // 커스텀 hook: DB에서 단위(unit) 목록 가져오기
    const { units } = useUnits();

    // 전역 로딩 UI를 제어하는 커스텀 hook
    const { showLoading, hideLoading } = useLoading();


    // ■ 최초 Input 행 1개 생성하는 함수
    //   - 화면 초기 렌더링 시 기본 한 줄을 제공하기 위함
    const createInitialInputRows = () => [
        {
            id: 1,          // 로컬 UI에서 사용되는 key 값
            pjename: "",    // 물질명
            pjeamount: "",  // 양
            uname: "",      // 단위명
            uno: null,      // 단위 번호
            pname: "",      // 자동 매핑된 프로세스명
            isInput: true,  // input 구분
        },
    ];

    // ■ 최초 Output 행 생성
    const createInitialOutputRows = () => [
        {
            id: 1,
            pjename: "",
            pjeamount: "",
            uname: "",
            uno: null,
            pname: "",
            isInput: false, // output 구분
        },
    ];

    // 제미나이 데이터 상태관리
    const [isMatch, setIsMatch] = useState(false);

    // WebSocket 상태관리
    const [socket, setSocket] = useState(null);

    // 화면에서 실시간 편집되고 있는 Input 행들
    const [inputRows, setInputRows] = useState(createInitialInputRows);

    // 화면에서 실시간 편집되고 있는 Output 행들
    const [outputRows, setOutputRows] = useState(createInitialOutputRows);

    // 서버에서 가져온 원본 Input 행 (되돌리기, 비교용)
    const [originalInputRows, setOriginalInputRows] = useState(
        createInitialInputRows
    );

    // 서버에서 가져온 원본 Output 행 (되돌리기, 비교용)
    const [originalOutputRows, setOriginalOutputRows] = useState(
        createInitialOutputRows
    );

    // 자동 매핑 결과 선택 모달 열림 여부
    const [openModal, setOpenModal] = useState(false);

    // 자동 매핑된 후보 리스트(JSON)
    // 예: 입력한 물질명에 대해 90% 이상 유사한 프로세스들
    const [matchData, setMatchData] = useState([]);

    // 체크박스 선택 상태 저장용 객체
    // { 1: true, 3: false, ... }
    const [checkedItems, setCheckedItems] = useState({});

    // 로컬 로딩 스피너 상태
    const [loading, setLoading] = useState(false);

    // Input 영역 체크박스 선택 리스트
    const [inputCheckedList, setInputCheckedList] = useState([]);

    // Output 영역 체크박스 선택 리스트
    const [outputCheckedList, setOutputCheckedList] = useState([]);

    // Input 전체선택 상태 계산
    // 모든 행의 체크박스가 선택되어 있어야 true
    const inputChecked =
        inputRows.length > 0 && inputCheckedList.length === inputRows.length;

    // Output 전체선택 상태 계산
    const outputChecked =
        outputRows.length > 0 && outputCheckedList.length === outputRows.length;

    // Select(단위 선택 드롭다운)에 사용할 옵션 목록 생성
    // units 배열을 UI에서 필요한 형태로 변환
    // 예: { value: 1, label: "kg", group: "Mass" }
    const unitOptions = units.map((u) => ({
        value: u.uno,        // DB의 unit 번호
        label: u.unit,       // 표시 이름
        group: u.ugname,     // 단위 그룹명 (예: Length, Mass)
    }));

    const openWebSocket = (mno) => {
        if (!mno) {
            console.error("mno 없어 소켓을 열 수 없습니다.");
            return;
        }// if end
        if (socket) {
            console.log("이미 소켓이 연결되어 있습니다.");
            return;
        }// if end
        const ws = new WebSocket('ws://localhost:8080/ws/socket');
        ws.onopen = () => {
            console.log(`소켓 오픈: ${mno}`);
            const message = JSON.stringify({
                type: "message",
                mno: mno
            });
            ws.send(message);
            console.log("소켓오픈 메시지 전송");
        };

        ws.onmessage = (event) => {
            console.log("메시지 수신", event.data);
            try {
                const data = JSON.parse(event.data);
                console.log(data);
                if (data.type === 'gemini') {
                    const geminiData = data.data;
                    // Gemini가 처리한 항목 Key Set

                    if(geminiData && typeof geminiData === 'object' && !Array.isArray(geminiData)){
                        const formetted = new Map(
                            Object.entries(geminiData).map( ([key,value]) => [
                                key,
                                Array.isArray(value) ? value : [value]
                            ])
                        )
                    setMatchData(prevMatchData => {
                        const nextMatchData = new Map();
                        let pendingCount = 0; // 아직 매칭 중인 항목 카운트

                        // 기존 matchData 순회
                        prevMatchData.forEach(item => {
                            const key = item.key;
                            let currentItem = item;

                            if (formetted.has(key)) {
                                // Gemini 결과가 있으면 덮어쓰기
                                currentItem = { key, value: formetted.get(key) };
                            } // if end
                            nextMatchData.set(key, currentItem);

                            // 업데이트된 항목이 여전히 "매칭 중..."인지 확인
                            if (currentItem.value && currentItem.value[0] === "AI 매칭 중...") {
                                pendingCount++;
                            }// if end
                        });

                        // 아직 매칭 중인 항목이 남아있는지 확인하여 isMatch 상태 업데이트
                        if (pendingCount === 0) {
                            setIsMatch(false);
                            // 모든 작업 완료 시 로딩 UI 해제
                            // hideLoading(loadingId);
                        }// if end

                        return Array.from(nextMatchData.values());
                    }); // setMatchData end
                    }// if end
                }// if end
            } catch (e) {
                console.log(e);
            }// try end
        };

        ws.onclose = () => {
            console.log(`소켓 종료: ${pjno}`);
            setSocket(null); // 연결이 닫히면 상태 초기화
        };

        ws.onerror = (error) => {
            console.error("소켓 에러:", error);
        };

        setSocket(ws);
    }// f end

    // WebSocket 연결 해제 함수 =============================================================
    const closeWebSocket = () => {
        if (socket) {
            socket.close();
        }
    };

    // 행 데이터 정규화 함수 =============================================================
    const normalizeRows = (rows = []) =>
        rows.map((r) => ({
            id: r.id,
            pjename: r.pjename ?? "",
            pjeamount: String(r.pjeamount ?? ""),
            uname: r.uname ?? "",
            uno: r.uno ?? null,
            pname: r.pname ?? "",
            isInput: !!r.isInput,
        }));

    // 변경 여부 확인 함수 =============================================================
    const isDirty = () => {
        const currInput = JSON.stringify(normalizeRows(inputRows));
        const currOutput = JSON.stringify(normalizeRows(outputRows));
        const originalInput = JSON.stringify(normalizeRows(originalInputRows));
        const originalOutput = JSON.stringify(
            normalizeRows(originalOutputRows)
        );
        return currInput !== originalInput || currOutput !== originalOutput;
    };

    // Input 전체선택 처리 함수 =============================================================
    const handleCheckAllInput = (checked) => {
        if (checked) {
            setInputCheckedList(inputRows.map((row) => row.id));
        } else {
            setInputCheckedList([]);
        }
    };

    // Output 전체선택 처리 함수 =============================================================
    const handleCheckAllOutput = (checked) => {
        if (checked) {
            setOutputCheckedList(outputRows.map((row) => row.id));
        } else {
            setOutputCheckedList([]);
        }
    };
    // 개별 행 선택 처리 함수 =============================================================
    const handleCheckInput = (id) => {
        setInputCheckedList((prev) =>
            prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
        );
    };

    const handleCheckOutput = (id) => {
        setOutputCheckedList((prev) =>
            prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
        );
    };

    // 단위 적용 함수 =============================================================
    const applyUnitToRow = (isInput, rowId, newUno) => {
        const picked = units.find((u) => u.uno === newUno);
        const updater = (prev) =>
            prev.map((row) =>
                row.id === rowId
                    ? {
                        ...row,
                        uno: newUno,
                        uname: picked?.unit ?? "",
                    }
                    : row
            );
        (isInput ? setInputRows : setOutputRows)(updater);
    };

    // 행 추가 함수 =============================================================
    const addInputRow = () => {
        const newId =
            inputRows.length > 0
                ? Math.max(...inputRows.map((r) => r.id)) + 1
                : 1;
        setInputRows((prev) => [
            ...prev,
            {
                id: newId,
                pjename: "",
                pjeamount: "",
                uname: "",
                uno: null,
                pname: "",
                isInput: true,
            },
        ]);
    };

    const addOutputRow = () => {
        const newId =
            outputRows.length > 0
                ? Math.max(...outputRows.map((r) => r.id)) + 1
                : 1;
        setOutputRows((prev) => [
            ...prev,
            {
                id: newId,
                pjename: "",
                pjeamount: "",
                uname: "",
                uno: null,
                pname: "",
                isInput: false,
            },
        ]);
    };

    // 행 데이터 변경 처리 함수 =============================================================
    const inputHandleChange = (id, field, value) => {
        setInputRows((prev) =>
            prev.map((row) => (row.id === id ? { ...row, [field]: value } : row))
        );
    };

    const outputHandleChange = (id, field, value) => {
        setOutputRows((prev) =>
            prev.map((row) => (row.id === id ? { ...row, [field]: value } : row))
        );
    };

    // 매칭 처리 함수 =============================================================
    const matchIO = async (pjenames) => {
        try {
            const response = await axios.post(
                "http://localhost:8080/api/inout/auto",
                pjenames,
                { withCredentials: true }
            );

            const data = response.data;

            if (data && typeof data === "object" && !Array.isArray(data)) {
                const formatted = Object.entries(data).map(([key, value]) => ({
                    key,
                    value,
                }));
                setMatchData(formatted);
                setCheckedItems({});
                setOpenModal(true);
            } else if (Array.isArray(data)) {
                setMatchData(data);
            } else {
                setMatchData([]);
            }
        } catch (e) {
            console.error("[matchIO error]", e);
        } finally {
            setLoading(false);
        }
    };

    // 매칭 전체 처리 함수 =============================================================
    const matchAllIO = async () => {
        // 소켓 열기
        openWebSocket(mno);
        const loadingId = showLoading("최적화 매칭중입니다...");
        setIsMatch(true);
        setOpenModal(true);
        setLoading(true);
        const allPjenames = [...inputRows, ...outputRows].map(
            (row) => row.pjename
        );

        if (allPjenames.length === 0 || allPjenames.includes("")) {
            alert("명칭을 모두 입력해주세요.");
            setLoading(false);
            hideLoading(loadingId);
            return;
        }

        await matchIO(allPjenames);
        setLoading(false);
        hideLoading(loadingId);
    };

    // 매칭 선택 처리 함수 =============================================================
    const matchSelectedIO = async () => {
        // 소켓 열기
        openWebSocket(mno);
        const loadingId = showLoading("선택된 항목 매칭중입니다...");
        setIsMatch(true);
        setOpenModal(true);
        setLoading(true);

        const selectedInputs = inputRows.filter((r) =>
            inputCheckedList.includes(r.id)
        );
        const selectedOutputs = outputRows.filter((r) =>
            outputCheckedList.includes(r.id)
        );
        const selected = [...selectedInputs, ...selectedOutputs];

        if (selected.length === 0) {
            alert("매칭할 항목을 선택해주세요.");
            setLoading(false);
            hideLoading(loadingId);
            return;
        }

        const pjenames = selected.map((s) => s.pjename);
        if (pjenames.includes("")) {
            alert("명칭을 모두 입력해주세요.");
            setLoading(false);
            hideLoading(loadingId);
            return;
        }

        await matchIO(pjenames);
        setLoading(false);
        hideLoading(loadingId);
    };

    // 개별 행 선택 처리 함수 ========================================================
    const handleCheckValue = (key, value) => {
        setCheckedItems((prev) => ({
            ...prev,
            [key]: value,
        }));
    };

    const handleSaveMatch = () => {
        setIsMatch(false);
        closeWebSocket();
        Object.entries(checkedItems).forEach(([key, value]) => {
            setInputRows((prev) =>
                prev.map((row) =>
                    row.pjename === key ? { ...row, pname: value } : row
                )
            );
            setOutputRows((prev) =>
                prev.map((row) =>
                    row.pjename === key ? { ...row, pname: value } : row
                )
            );
        });
        setOpenModal(false);
    };

    // 정보 초기화 함수 =========================================================
    const clearIOInfo = async () => {
        if (!effectivePjno) {
            alert("프로젝트 번호를 선택해주세요.");
            return;
        }
        try {
            const response = await axios.delete(
                "http://localhost:8080/api/inout",
                {
                    params: { pjno: effectivePjno },
                    withCredentials: true,
                }
            );
            const data = response.data;
            if (data) {
                alert("정보가 초기화되었습니다.");
                setInputRows([]);
                setOutputRows([]);
                setInputCheckedList([]);
                setOutputCheckedList([]);
            } else {
                alert("정보 초기화에 실패했습니다.");
            }
        } catch (e) {
            console.error("[clearIOInfo error]", e);
        }
    };

    // 행 삭제 처리 함수 =========================================================
    const deleteInputRows = () => {
        if (inputCheckedList.length === 0) {
            alert("삭제할 항목을 선택해주세요.");
            return;
        }
        setInputRows((prev) =>
            prev.filter((row) => !inputCheckedList.includes(row.id))
        );
        setInputCheckedList([]);
    };

    const deleteOutputRows = () => {
        if (outputCheckedList.length === 0) {
            alert("삭제할 항목을 선택해주세요.");
            return;
        }
        setOutputRows((prev) =>
            prev.filter((row) => !outputCheckedList.includes(row.id))
        );
        setOutputCheckedList([]);
    };

    const handleDelete = () => {
        const hasInput = inputCheckedList.length > 0;
        const hasOutput = outputCheckedList.length > 0;

        if (!hasInput && !hasOutput) {
            alert("삭제할 항목을 선택해주세요.");
            return;
        }

        if (hasInput) deleteInputRows();
        if (hasOutput) deleteOutputRows();
    };

    // 정보 저장 함수 =========================================================
    const saveIOInfo = async () => {
        if (!effectivePjno) {
            alert("프로젝트 번호를 선택해주세요.");
            return;
        }// if end

        const allRows = [...inputRows, ...outputRows];
        const checkFields = ['pjename', 'pjeamount', 'uname', 'uno'];

        const resultFields = allRows.some( row =>
            checkFields.some( field =>
                !row[field] || String(row[field]).trim() === "" )
        );

        if(resultFields){
            alert("모든 항목의 필수 정보(투입·산출물명, 양, 단위)를 입력해주세요.");
            return;
        }// if end

        const outputCheck = outputRows.some( row => !row.pname || row.pname.trim() === "");
        if(!outputCheck){
            alert("산출물: 프로세스명 공란 항목 필수");
            return;
        }// if end

        const payload = {
            pjno: effectivePjno,
            exchanges: [...inputRows, ...outputRows],
        };

        try {
            const response = await axios.post(
                "http://localhost:8080/api/inout",
                payload,
                { withCredentials: true }
            );
            const data = response.data;
            if (data) {
                alert("정보가 저장되었습니다.");
                setOriginalInputRows(inputRows);
                setOriginalOutputRows(outputRows);
            } else {
                alert("정보 저장에 실패했습니다.");
            }
        } catch (e) {
            console.error("[saveIOInfo error]", e);
        }
    };

    const fetchProjectExchange = async (pjnoParam) => {
        const res = await axios.get("http://localhost:8080/api/inout", {
            params: { pjno: pjnoParam },
            withCredentials: true,
        });

        const inputList = Array.isArray(res?.data?.inputList)
            ? res.data.inputList
            : [];
        const outputList = Array.isArray(res?.data?.outputList)
            ? res.data.outputList
            : [];
        return { inputList, outputList };
    };

    const { data: exchangeData } = useQuery({
        queryKey: ["project", effectivePjno, "exchange"],
        queryFn: () => fetchProjectExchange(effectivePjno),
        enabled: isOpen && !!effectivePjno,
        staleTime: 1000 * 30,
        refetchOnWindowFocus: false,
        retry: false
    });

    useEffect(() => {
        if (!isOpen || !effectivePjno) return;
        if (!exchangeData) return;
        if (isDirty()) return;

        const unitNameToUno = new Map(units.map((u) => [u.unit, u.uno]));

        const mappedInput = exchangeData.inputList.map((item, index) => ({
            id: index + 1,
            pjename: item.pjename ?? "",
            pjeamount: item.pjeamount ?? "",
            uname: item.uname ?? "",
            uno: unitNameToUno.get(item.uname ?? "") ?? null,
            pname: item.pname ?? "",
            isInput: item.isInput ?? true,
        }));

        const mappedOutput = exchangeData.outputList.map((item, index) => ({
            id: index + 1,
            pjename: item.pjename ?? "",
            pjeamount: item.pjeamount ?? "",
            uname: item.uname ?? "",
            uno: unitNameToUno.get(item.uname ?? "") ?? null,
            pname: item.pname ?? "",
            isInput: item.isInput ?? false,
        }));

        setInputRows(mappedInput);
        setOutputRows(mappedOutput);
        setOriginalInputRows(mappedInput);
        setOriginalOutputRows(mappedOutput);
        setInputCheckedList([]);
        setOutputCheckedList([]);
    }, [exchangeData, isOpen, effectivePjno, units]);

    // 정보 초기화 함수 =========================================================
    useEffect(() => {
        if (!isOpen) {
            setInputRows(createInitialInputRows());
            setOutputRows(createInitialOutputRows());
            setOriginalInputRows(createInitialInputRows());
            setOriginalOutputRows(createInitialOutputRows());
            setInputCheckedList([]);
            setOutputCheckedList([]);
        }
    }, [isOpen]);

    useEffect(() => {
        if (!units.length) return;
        const map = new Map(units.map((u) => [u.unit, u.uno]));
        setInputRows((prev) =>
            prev.map((row) =>
                row.uno || !row.uname || !map.has(row.uname)
                    ? row
                    : { ...row, uno: map.get(row.uname) }
            )
        );
        setOutputRows((prev) =>
            prev.map((row) =>
                row.uno || !row.uname || !map.has(row.uname)
                    ? row
                    : { ...row, uno: map.get(row.uname) }
            )
        );
    }, [units]);

    // LCI 계산 함수 =========================================================
    const calcLCI = async () => {
        if (!effectivePjno) {
            alert("프로젝트 번호를 선택해주세요.");
            return;
        }
        if (isDirty()) {
            alert("정보가 변경되었습니다. 저장 후 다시 시도해주세요.");
            return;
        }
        const loadingId = showLoading("LCI 계산 중입니다.");
        try {
            const res = await axios.get(
                `http://localhost:8080/api/lci/calc`,
                {
                    params: { pjno: effectivePjno },
                    withCredentials: true,
                }
            );
            const ok = res?.data === true;
            if (ok) {
                onCalcSuccess?.();
            } else {
                alert("LCI 계산에 실패했습니다.");
            }
        } catch (e) {
            console.error("[calcLCI error]", e);
            alert("LCI 계산에 실패했습니다.");
        } finally {
            hideLoading(loadingId);
        }
    };

    // 입력 컬럼 ===================================================
    const inputColumns = [
        {
            id: "_select",
            title: (
                <Checkbox
                    checked={inputChecked}
                    indeterminate={
                        !inputChecked && inputCheckedList.length > 0
                    }
                    onChange={(e) => handleCheckAllInput(e.target.checked)}
                />
            ),
            width: 40,
        },
        { id: "no", title: "No", width: 30 },
        { id: "pjename", title: "투입물명", width: 100 },
        { id: "pjeamount", title: "투입량", width: 100 },
        { id: "uname", title: "단위", width: 100 },
        { id: "pname", title: "프로세스명", width: 200 },
    ];

    const outputColumns = [
        {
            id: "_select",
            title: (
                <Checkbox
                    checked={outputChecked}
                    indeterminate={
                        !outputChecked && outputCheckedList.length > 0
                    }
                    onChange={(e) => handleCheckAllOutput(e.target.checked)}
                />
            ),
            width: 40,
        },
        { id: "no", title: "No", width: 30 },
        { id: "pjename", title: "산출물명", width: 100 },
        { id: "pjeamount", title: "산출량", width: 100 },
        { id: "uname", title: "단위", width: 100 },
        { id: "pname", title: "프로세스명", width: 200 },
    ];

    // 투입물 입력 데이터 ===================================================
    const inputTableData =
        inputRows.length > 0
            ? inputRows.map((row, index) => ({
                ...row,
                _select: (
                    <Checkbox
                        checked={inputCheckedList.includes(row.id)}
                        onChange={() => handleCheckInput(row.id)}
                    />
                ),
                no: index + 1,
                pjename: (
                    <input
                        type="text"
                        value={row.pjename}
                        onChange={(e) =>
                            inputHandleChange(
                                row.id,
                                "pjename",
                                e.target.value
                            )
                        }
                        className="projectExchangeCellInput"
                    />
                ),
                pjeamount: (
                    <input
                        type="number"
                        value={row.pjeamount}
                        onChange={(e) =>
                            inputHandleChange(
                                row.id,
                                "pjeamount",
                                e.target.value
                            )
                        }
                        className="projectExchangeCellInput"
                    />
                ),
                uname: (
                    <Select
                        placeholder={row.uname || "단위 선택"}
                        value={row.uno ?? null}
                        onChange={(_, newUno) =>
                            applyUnitToRow(true, row.id, newUno)
                        }
                        disabled={!unitOptions.length}
                        size="sm"
                    >
                        {unitOptions.map((u) => (
                            <Option key={u.value} value={u.value}>
                                {u.group ? `${u.group} / ${u.label}` : u.label}
                            </Option>
                        ))}
                    </Select>
                ),
                pname: (
                    <input
                        type="text"
                        value={row.pname}
                        onChange={(e) =>
                            inputHandleChange(
                                row.id,
                                "pname",
                                e.target.value
                            )
                        }
                        className="projectExchangeCellInput"
                    />
                ),
            }))
            : [{ __empty: true }];

    // 산출물 입력 데이터 ==================================================
    const outputTableData =
        outputRows.length > 0
            ? outputRows.map((row, index) => ({
                ...row,
                _select: (
                    <Checkbox
                        checked={outputCheckedList.includes(row.id)}
                        onChange={() => handleCheckOutput(row.id)}
                    />
                ),
                no: index + 1,
                pjename: (
                    <input
                        type="text"
                        value={row.pjename}
                        onChange={(e) =>
                            outputHandleChange(
                                row.id,
                                "pjename",
                                e.target.value
                            )
                        }
                        className="projectExchangeCellInput"
                    />
                ),
                pjeamount: (
                    <input
                        type="number"
                        value={row.pjeamount}
                        onChange={(e) =>
                            outputHandleChange(
                                row.id,
                                "pjeamount",
                                e.target.value
                            )
                        }
                        className="projectExchangeCellInput"
                    />
                ),
                uname: (
                    <Select
                        placeholder={row.uname || "단위 선택"}
                        value={row.uno ?? null}
                        onChange={(_, newUno) =>
                            applyUnitToRow(false, row.id, newUno)
                        }
                        disabled={!unitOptions.length}
                        size="sm"
                    >
                        {unitOptions.map((u) => (
                            <Option key={u.value} value={u.value}>
                                {u.group ? `${u.group} / ${u.label}` : u.label}
                            </Option>
                        ))}
                    </Select>
                ),
                pname: (
                    <input
                        type="text"
                        value={row.pname}
                        onChange={(e) =>
                            outputHandleChange(
                                row.id,
                                "pname",
                                e.target.value
                            )
                        }
                        className="projectExchangeCellInput"
                    />
                ),
            }))
            : [{ __empty: true }];

    // return ==================================================================
    return (
        <>
            <Modal
                open={openModal}
                onClose={() => {
                    setOpenModal(false);
                    closeWebSocket();
                }}
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                }}
            >
                <Sheet
                    sx={{
                        padding: 2,
                        width: "50%",
                        maxHeight: "50vh",
                        overflowY: "auto",
                        border: "2px solid #334080",
                        borderRadius: 2,
                        backgroundColor: "#fff",
                        boxShadow: "0px 4px 16px rgba(0, 0, 0, 0.2)",
                    }}
                >
                    <Typography
                        level="h6"
                        sx={{ marginBottom: 2, textAlign: "center" }}
                    >
                        매칭 추천
                    </Typography>
                    {Array.isArray(matchData) &&
                        matchData.map((item) => (
                            <Box
                                key={item.key}
                                sx={{
                                    borderBottom: "1px solid #ccc",
                                    marginBottom: 2,
                                    paddingBottom: 1,
                                }}
                            >
                                <Typography
                                    level="body1"
                                    sx={{ fontWeight: "bold" }}
                                >
                                    {item.key}
                                </Typography>

                                <Box
                                    sx={{
                                        marginTop: 1,
                                        maxHeight:
                                            item.value.length > 3
                                                ? 100
                                                : "auto",
                                        overflowY:
                                            item.value.length > 3
                                                ? "auto"
                                                : "visible",
                                        border:
                                            item.value.length > 3
                                                ? "1px solid #ddd"
                                                : "none",
                                        padding: 1,
                                        borderRadius: 1,
                                    }}
                                >
                                    {item.value.map((val, index) => (
                                        <Box
                                            key={index}
                                            sx={{
                                                display: "flex",
                                                alignItems: "center",
                                                marginBottom: 0.5,
                                            }}
                                            className="dividerLineForMatching"
                                        >
                                            <Checkbox
                                                checked={
                                                    checkedItems[item.key] ===
                                                    val
                                                }
                                                onChange={() =>
                                                    handleCheckValue(
                                                        item.key,
                                                        val
                                                    )
                                                }
                                            />
                                            <Typography
                                                level="body2"
                                                sx={{ marginLeft: 1 }}
                                            >
                                                {val}
                                            </Typography>
                                            <br />
                                        </Box>
                                    ))}
                                </Box>
                            </Box>
                        ))}

                    <Box
                        sx={{
                            display: "flex",
                            justifyContent: "space-between",
                            marginTop: 2,
                        }}
                    >
                        <Button
                            variant="solid"
                            color="primary"
                            onClick={handleSaveMatch}
                        >
                            매칭 저장
                        </Button>
                        <Button
                            variant="outlined"
                            color="neutral"
                            onClick={() => {
                                setOpenModal(false)
                                closeWebSocket();
                            }}
                        >
                            닫기
                        </Button>
                    </Box>
                </Sheet>
            </Modal>
            <div className="projectExchangeToolbar">
                <Button variant="outlined"
                    onClick={matchSelectedIO}
                    disabled={loading}
                >
                    {loading ? "매칭 추천 중.." : "선택 추천"}
                </Button>
                <Button variant="outlined"
                    onClick={matchAllIO}
                    disabled={loading}
                >
                    {loading ? "매칭 추천 중.." : "자동 추천"}
                </Button>
                <Button variant="outlined"
                    onClick={saveIOInfo}
                >
                    저장
                </Button>
                <Button variant="outlined"
                    onClick={calcLCI}
                >
                    LCI 계산
                </Button>
                <Button variant="outlined"
                    onClick={handleDelete}
                >
                    삭제
                </Button>
                <Button variant="outlined"
                    onClick={clearIOInfo}
                >
                    초기화
                </Button>
            </div>
            <div className="projectExchangeTable">
                <div className="projectExchangeSectionHeader">
                    <div className="resultTitle">투입물</div>
                    <Button variant="outlined"
                        onClick={addInputRow}
                    >
                        행추가
                    </Button>
                </div>
                <ProjectListTable
                    columns={inputColumns}
                    data={inputTableData}
                    rememberKey="ProjectExchangeInputTable"
                    sortable={false}
                    stickyFirst={false}
                />
                <div className="divisionArea"></div>

                <div className="projectExchangeSectionHeader">
                    <div className="resultTitle">산출물</div>
                    <Button variant="outlined"
                        onClick={addOutputRow}
                    >
                        행추가
                    </Button>
                </div>
                <ProjectListTable
                    columns={outputColumns}
                    data={outputTableData}
                    rememberKey="ProjectExchangeOutputTable"
                    sortable={false}
                    stickyFirst={false}
                />
            </div>
        </>
    ); // return end
} // func end
