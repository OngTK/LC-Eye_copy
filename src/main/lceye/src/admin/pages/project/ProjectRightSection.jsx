import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import ProjectBasicInfo from "../../components/project/ProjectBasicInfo";
import ProjectExchange from "../../components/project/ProjectExchange.jsx";
import ProjectResult from "../../components/project/ProjectResult";
import Accordion from "@mui/joy/Accordion";
import AccordionDetails from "@mui/joy/AccordionDetails";
import AccordionGroup from "@mui/joy/AccordionGroup";
import AccordionSummary from "@mui/joy/AccordionSummary";

export default function ProjectRightSection(props) {
    const basicInfoStatus = useSelector(
        (state) => state.project?.basicInfoStatus
    );
    const selectedProject = useSelector(
        (state) => state.project?.selectedProject
    );

    const canOpenSubAccordions =
        !!basicInfoStatus &&
        basicInfoStatus.isInitialized &&
        basicInfoStatus.isSaved &&
        !basicInfoStatus.isDirty;

    const [basicOpen, setBasicOpen] = useState(true);
    const [exchangeOpen, setExchangeOpen] = useState(false);
    const [resultOpen, setResultOpen] = useState(false);

    // 기본 정보가 초기화되지 않았거나 dirty 상태이면 하위 아코디언 닫기
    useEffect(() => {
        if (!canOpenSubAccordions) {
            setExchangeOpen(false);
            setResultOpen(false);
        }
    }, [canOpenSubAccordions]);

    // 좌측 프로젝트 목록에서 pjno가 변경되면 투입물·산출물 아코디언 접기
    useEffect(() => {
        setExchangeOpen(false);
        setResultOpen(false);
    }, [selectedProject?.pjno]);

    return (
        <>
            <div className="projectRigthTop">
                <div className="projectNameBox">프로젝트</div>
            </div>
            <div className="projectRightBot">
                <AccordionGroup
                    size={"md"}
                    variant="outlined"
                    sx={{ borderRadius: "lg" }}
                >
                    <Accordion
                        expanded={basicOpen}
                        onChange={(_, expanded) => setBasicOpen(expanded)}
                    >
                        <AccordionSummary>
                            <div className="sectionName">프로젝트 기본정보</div>
                        </AccordionSummary>
                        <AccordionDetails>
                            <ProjectBasicInfo />
                        </AccordionDetails>
                    </Accordion>
                    <Accordion
                        expanded={exchangeOpen}
                        disabled={!canOpenSubAccordions}
                        onChange={(_, expanded) => {
                            if (!canOpenSubAccordions) return;
                            setExchangeOpen(expanded);
                        }}
                    >
                        <AccordionSummary>
                            <div className="sectionName">
                                투입물·산출물 정보
                            </div>
                        </AccordionSummary>
                        <AccordionDetails>
                            <ProjectExchange
                                key={selectedProject?.pjno ?? "none"}
                                pjno={selectedProject?.pjno}
                                isOpen={exchangeOpen}
                                onCalcSuccess={() => setResultOpen(true)}
                            />
                        </AccordionDetails>
                    </Accordion>
                    <Accordion
                        expanded={resultOpen}
                        disabled={!canOpenSubAccordions}
                        onChange={(_, expanded) => {
                            if (!canOpenSubAccordions) return;
                            setResultOpen(expanded);
                        }}
                    >
                        <AccordionSummary>
                            <div className="sectionName">LCI 결과</div>
                        </AccordionSummary>
                        <AccordionDetails>
                            <ProjectResult
                                pjno={selectedProject?.pjno}
                                isOpen={resultOpen}
                            />
                        </AccordionDetails>
                    </Accordion>
                </AccordionGroup>
            </div>
        </>
    ); // return end
} // func end
