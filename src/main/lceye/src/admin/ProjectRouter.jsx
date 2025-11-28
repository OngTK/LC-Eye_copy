import Header from './components/layout/Header.jsx';
import ProjectLeftSection from "./pages/project/ProjectLeftSection";
import ProjectRightSection from "./pages/project/ProjectRightSection";
import '../assets/css/project.css';
import SimpleSplitPane from './components/layout/SplitPaneResponsive.jsx';
import { LoadingProvider } from "./contexts/LoadingContext.jsx";

export default function ProjectRouter(props) {
    return (
        <LoadingProvider>
            <div className='header'>
                <Header />
            </div>
                <SimpleSplitPane
                    initLeftPct={40}              // 초기 좌측 폭(%)
                    minLeftPx={500}               // 좌측 최소(px)
                    minRightPx={500}              // 우측 최소(px)
                    left={<ProjectLeftSection />}        // 좌측 콘텐츠
                    right={<ProjectRightSection />}     // 우측 콘텐츠
                />
        </LoadingProvider>
    ) // return end
} // func end
