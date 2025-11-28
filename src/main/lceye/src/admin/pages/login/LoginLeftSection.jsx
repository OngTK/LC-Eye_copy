import logo from '../../../assets/img/LC-Eye_Logo.svg';

export default function LoingLeftSection(props){
    return(
        <>
        <div className='leftSection'>
            <div className='loginLogoBox'>
                <img src={logo} alt="메인 로고" />
            </div>
            <div className="loginSectionText">
                <div className='mainText'>제품의 전 과정을 눈으로 보는 듯 투명하게 드러내는 LCI 시스템</div>
                <div className='subText'><span>즉, LC-Eye는 단순한 계산기가 아니라 제품의 전 과정(Input-Output-Flow)을</span><br />
                <span> '한눈에 파악'하고 '정확히 분석'하는 시작적 LCI 도구라는 뜻을 가집니다.</span></div>
            </div>
        </div>
        </>
    ) // return end
} // func end