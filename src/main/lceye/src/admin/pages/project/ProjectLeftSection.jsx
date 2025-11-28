import { useCallback } from "react";
import axios from "axios";
import { useDispatch, useSelector } from "react-redux";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import ProjectListTable from "../../components/project/ProjectListTable.jsx";
import { setSelectedProject } from "../../store/projectSlice.jsx";
import "../../../assets/css/projectLeftSessionBox.css";
import { useLoading } from "../../contexts/LoadingContext.jsx";

export default function ProjectLeftSection(props) {
    const dispatch = useDispatch();
    const projectListVersion = useSelector(
        (state) => state.project?.projectListVersion
    );
    const queryClient = useQueryClient();
    const { showLoading, hideLoading } = useLoading();

    // 리스트 시각 표시 포맷 (YYYY-MM-DD HH:MM)
    const formatListDateTime = (value) => {
        if (!value) return "";
        if (typeof value === "string") {
            const base = value.slice(0, 16); // YYYY-MM-DDTHH:MM
            return base.replace("T", " ");
        }
        return "";
    };

    const readAllProject = async () => {
        const r = await axios.get("http://localhost:8080/api/project/all", {
            withCredentials: true,
        });
        const d = Array.isArray(r.data) ? r.data : [];
        return d.map((p) => ({
            ...p,
            createdate: formatListDateTime(p.createdate),
        }));
    }; // func end

    const { data: projects = [], isFetching: isProjectListFetching } = useQuery({
        queryKey: ["projectList", projectListVersion],
        queryFn: readAllProject,
        staleTime: 1000 * 60, // 목록은 짧게 캐싱
        keepPreviousData: true,
    });

    const fetchProjectDetail = async (pjno) => {
        const r = await axios.get(`http://localhost:8080/api/project?pjno=${pjno}`, {
            withCredentials: true,
        });
        return r.data;
    };

    const fetchProjectExchange = async (pjno) => {
        const r = await axios.get("http://localhost:8080/api/inout", {
            params: { pjno },
            withCredentials: true,
        });
        const inputList = Array.isArray(r?.data?.inputList) ? r.data.inputList : [];
        const outputList = Array.isArray(r?.data?.outputList) ? r.data.outputList : [];
        return { inputList, outputList };
    };

    const fetchProjectLci = async (pjno) => {
        const r = await axios.get("http://localhost:8080/api/lci", {
            params: { pjno },
            withCredentials: true,
        });
        const inputList = Array.isArray(r?.data?.inputList) ? r.data.inputList : [];
        const outputList = Array.isArray(r?.data?.outputList) ? r.data.outputList : [];
        return { inputList, outputList };
    };

    // 클릭 시 해당 pjno 상세 조회 + 캐시 프리패치
    const handleRowClick = useCallback(
        async (row) => {
            const pjno = row?.pjno;
            if (!pjno) return;

            const loadingId = showLoading("로딩중입니다.");
            try {
                const detail = await queryClient.fetchQuery({
                    queryKey: ["project", pjno, "detail"],
                    queryFn: () => fetchProjectDetail(pjno),
                    staleTime: 1000 * 60,
                });
                dispatch(setSelectedProject(detail));

                // 우측 섹션 로딩 가속을 위해 주요 데이터 프리패치
                queryClient.prefetchQuery({
                    queryKey: ["project", pjno, "exchange"],
                    queryFn: () => fetchProjectExchange(pjno),
                    staleTime: 1000 * 30,
                });
                queryClient.prefetchQuery({
                    queryKey: ["project", pjno, "lci"],
                    queryFn: () => fetchProjectLci(pjno),
                    staleTime: 1000 * 30,
                });
            } catch (e) {
                console.error("[readProject error]", e);
            } finally {
                hideLoading(loadingId);
            }
        },
        [dispatch, hideLoading, queryClient, showLoading]
    );

    // ProjectListTable 컬럼 정의
    const columns = [
        { id: "pjno", title: "No", width: 60 },
        { id: "pjname", title: "프로젝트명", width: 100 },
        { id: "pjdesc", title: "프로젝트 설명", width: 100 },
        { id: "mname", title: "작성자", width: 100 },
        { id: "createdate", title: "작성일", width: 100 },
    ];

    // return ==================================================================
    return (
        <>
            <div>
                <div className="projectListNameBox">프로젝트 목록</div>
                <div className="projectListBox">
                    <ProjectListTable
                        columns={columns}
                        data={projects}
                        rememberKey="ProjectListTable"
                        onRowClick={handleRowClick}
                        loading={isProjectListFetching}
                    />
                </div>
            </div>
        </>
    ); // return end
} // func end
