import "../../../assets/css/projectBasicInfo.css";
import Button from "@mui/joy/Button";
import FormControl from "@mui/joy/FormControl";
import FormLabel from "@mui/joy/FormLabel";
import Textarea from "@mui/joy/Textarea";
import Input from "@mui/joy/Input";
import FormHelperText from "@mui/joy/FormHelperText";
import { useEffect, useState } from "react";
import axios from "axios";
import { useDispatch, useSelector } from "react-redux";
import {
    clearSelectedProject,
    setSelectedProject,
    incrementProjectListVersion,
    setBasicInfoDirty,
} from "../../store/projectSlice.jsx";
import UnitSelector from "./UnitSelector.jsx";
import useUnits from "../../hooks/useUnits";

export default function ProjectBasicInfo(props) {
    // redux store =============================================
    const selectedProject = useSelector(
        (state) => state.project?.selectedProject
    );
    const { isLogin } = useSelector((state) => state.admin);
    const dispatch = useDispatch();

    // 단위 그룹/상세 단위 선택 관련 상태 =============================================
    const { units } = useUnits();

    // 폼에 바인딩되는 값들(프로젝트 명, 설명, 예산, 부서) =================
    const [form, setForm] = useState({
        pjname: "",
        pjdesc: "",
        pjamount: "",
        uno: null,
    });

    // 인라인 검증용 에러 메시지 =============================================
    const [errors, setErrors] = useState({
        pjname: "",
        pjamount: "",
        uno: "",
    });

    // 선택된 프로젝트가 바뀌면 폼 초기화 / 세팅 =============================
    useEffect(() => {
        if (!selectedProject) {
            setForm({
                pjname: "",
                pjdesc: "",
                pjamount: "",
                uno: null,
            });
            setErrors({
                pjname: "",
                pjamount: "",
                uno: "",
            });
            return;
        }

        setForm({
            pjname: selectedProject.pjname ?? "",
            pjdesc: selectedProject.pjdesc ?? "",
            pjamount: selectedProject.pjamount ?? "",
            uno: selectedProject.uno ?? null,
        });
        setErrors({
            pjname: "",
            pjamount: "",
            uno: "",
        });
    }, [selectedProject]);


    // Input type = datetime-local 에 맞게 변환 ==============================
    const formatDate = (value) => {
        if (!value) return "";
        if (typeof value === "string") {
            // 서버: "2025-11-19T10:55:10.984161" -> "2025-11-19T10:55"
            return value.slice(0, 16);
        }
        return "";
    };

    // 저장 버튼: 신규/수정 저장 후 다시 조회 ================================
    const saveProjectInfo = async () => {
        const pjno = selectedProject?.pjno ?? null;

        // 인라인 검증 --------------------------------------------------------
        const nextErrors = {
            pjname: "",
            pjamount: "",
            uno: "",
        };

        if (!form.pjname || !form.pjname.trim()) {
            nextErrors.pjname = "프로젝트 명을 입력해 주세요.";
        }

        const amountNumber = Number(form.pjamount);
        if (!form.pjamount || Number.isNaN(amountNumber) || amountNumber <= 0) {
            nextErrors.pjamount = "제품 생산량을 0보다 큰 숫자로 입력해 주세요.";
        }

        if (!form.uno) {
            nextErrors.uno = "상세 단위를 선택해 주세요.";
        }

        const hasError = Object.values(nextErrors).some((msg) => !!msg);
        if (hasError) {
            setErrors(nextErrors);
            return;
        }

        // 검증 통과 시 기존 에러 초기화
        setErrors({
            pjname: "",
            pjamount: "",
            uno: "",
        });

        // 공통 payload (신규/수정 모두 사용)
        const basePayload = {
            pjname: form.pjname,
            pjdesc: form.pjdesc,
            uno: form.uno,
            pjamount: form.pjamount,
        };

        try {
            let response;

            // pjno 가 null 또는 0 이면 신규 -> POST =========================
            if (!pjno || pjno === 0) {
                response = await axios.post(
                    "http://localhost:8080/api/project",
                    basePayload,
                    { withCredentials: true }
                );
            } else {
                // pjno 가 있으면 수정 -> PUT ================================
                response = await axios.put(
                    "http://localhost:8080/api/project",
                    { pjno, ...basePayload },
                    { withCredentials: true }
                );
            }

            // 저장 성공 여부 확인 ===========================================
            const saved = response?.data;
            const savedPjno = saved?.pjno;
            if (!savedPjno) {
                console.error(
                    "[saveProjectInfo] 저장 결과에 pjno 가 없습니다.",
                    saved
                );
                alert("저장에 실패했습니다.");
                return;
            }
            alert("저장이 완료되었습니다.");

            // 저장된 pjno 로 다시 프로젝트 조회 + 리스트 갱신 트리거 ========
            try {
                const r = await axios.get(
                    `http://localhost:8080/api/project?pjno=${savedPjno}`,
                    { withCredentials: true }
                );
                // 기본정보 재세팅
                dispatch(setSelectedProject(r.data));
                // 프로젝트 목록 재조회용 버전 증가
                dispatch(incrementProjectListVersion());
            } catch (e) {
                console.error("[saveProjectInfo/readProject error]", e);
            }
        } catch (e) {
            console.error("[saveProjectInfo error]", e);
        }
    };

    // 초기화 버튼: 선택된 프로젝트 해제 -> 폼/에러 초기화 ====================
    const resetBasicInfo = () => {
        dispatch(clearSelectedProject());
        setErrors({
            pjname: "",
            pjamount: "",
            uno: "",
        });
    };

    // 폼 변경 시 basicInfoDirty 플래그 설정을 위한 헬퍼 ======================
    const markDirtyAndSetForm = (updater) => {
        dispatch(setBasicInfoDirty(true));
        setForm((prev) => updater(prev));
    };

    const handleUnitChange = (uno) => {
        if (errors.uno) {
            setErrors((prev) => ({
                ...prev,
                uno: "",
            }));
        }
        markDirtyAndSetForm((prev) => ({
            ...prev,
            uno,
        }));
    };

    // return =================================================================
    return (
        <>
            <div>
                <div className="headButton">
                    <Button variant="outlined" onClick={saveProjectInfo}>
                        저장
                    </Button>
                    <Button variant="outlined" onClick={resetBasicInfo}>
                        초기화
                    </Button>
                </div>
                <FormControl
                    className="bottomMargin"
                    error={!!errors.pjname}
                >
                    <FormLabel className="projectLabel">프로젝트 명</FormLabel>
                    <Input
                        value={form.pjname}
                        onChange={(e) => {
                            if (errors.pjname) {
                                setErrors((prev) => ({
                                    ...prev,
                                    pjname: "",
                                }));
                            }
                            markDirtyAndSetForm((prev) => ({
                                ...prev,
                                pjname: e.target.value,
                            }));
                        }}
                    />
                    {errors.pjname && (
                        <FormHelperText>{errors.pjname}</FormHelperText>
                    )}
                </FormControl>
                <FormControl className="bottomMargin">
                    <FormLabel className="projectLabel">
                        프로젝트 설명
                    </FormLabel>
                    <Textarea
                        minRows={3}
                        maxRows={5}
                        value={form.pjdesc}
                        onChange={(e) =>
                            markDirtyAndSetForm((prev) => ({
                                ...prev,
                                pjdesc: e.target.value,
                            }))
                        }
                    />
                </FormControl>
                <FormControl className="bottomMargin">
                    <FormLabel className="projectLabel">작성자</FormLabel>
                    <Input
                        value={selectedProject?.mname || isLogin?.mname || ""}
                        readOnly
                    />
                </FormControl>
                <FormControl
                    className="bottomMargin"
                    error={!!errors.pjamount}
                >
                    <FormLabel className="projectLabel">제품 생산량</FormLabel>
                    <Input
                        type="number"
                        value={form.pjamount}
                        onChange={(e) => {
                            if (errors.pjamount) {
                                setErrors((prev) => ({
                                    ...prev,
                                    pjamount: "",
                                }));
                            }
                            markDirtyAndSetForm((prev) => ({
                                ...prev,
                                pjamount: e.target.value,
                            }));
                        }}
                    />
                    {errors.pjamount && (
                        <FormHelperText>{errors.pjamount}</FormHelperText>
                    )}
                </FormControl>
                <div className="bottomMargin">
                    <UnitSelector
                        units={units}
                        value={form.uno}
                        onChange={handleUnitChange}
                        error={errors.uno}
                        onErrorClear={() =>
                            setErrors((prev) => ({
                                ...prev,
                                uno: "",
                            }))
                        }
                    />
                </div>
                <div className="unitSelectArea bottomMargin">
                    <FormControl>
                        <FormLabel className="projectLabel">등록일</FormLabel>
                        <Input
                            type="datetime-local"
                            readOnly
                            value={formatDate(selectedProject?.createdate)}
                        />

                    </FormControl>
                    <FormControl>
                        <FormLabel className="projectLabel">수정일</FormLabel>
                        <Input
                            type="datetime-local"
                            readOnly
                            value={formatDate(selectedProject?.updatedate)}
                        />
                    </FormControl>

                </div>
                <FormHelperText>
                    ※ 등록일/수정일은 자동으로 입력되며, 수정할 수 없습니다.
                </FormHelperText>
            </div>
        </>
    ); // return end
} // component end