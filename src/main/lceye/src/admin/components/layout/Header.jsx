import { useDispatch, useSelector } from 'react-redux';
import { Navigate } from "react-router-dom";
import headerLogo from '../../../assets/img/LC-Eye_HeaderLogo.svg';
import axios from 'axios';
import { checkingLogin } from '../../store/adminSlice';

const axiosOption = { withCredentials: true };

export default function Header(props) {
    const { isLogin } = useSelector((state) => state.admin);
    const dispatch = useDispatch();

    const logout = async () => {
        try {
            const response = await axios.get("http://localhost:8080/api/member/logout", axiosOption);
            const data = await response.data;
            if (data) {
                alert('로그아웃 완료');
                dispatch(checkingLogin({
                    isAuth: false,
                    role: null,
                    mno: null,
                    mname: null,
                    cno: null,
                    cname: null,
                }));
                return <Navigate to="/" replace />
            } // if end
        } catch (error) {
            console.log(error);
        } // try-catch end
    } // func end

    return (
        <>
            <div className='imgBox'>
                <img src={headerLogo} alt="headerLogo" onClick={() => { location.href = "/project"; }}/>
            </div>
            <div className='infoBox'>
                <div>
                    <span className='infoMname'>
                        {isLogin.mname}
                    </span>
                    <span> </span>
                    ({
                        isLogin.role == "ADMIN" ? "시스템 관리자"
                            : isLogin.role == "MANAGER" ? "회사 관리자"
                                : "실무자"
                    })님 <br />
                </div>
                <div>
                    소속 : {isLogin.cname}
                </div>
                <div className='logoutButton'>
                    <button onClick={logout}>로그아웃</button>
                </div>
            </div>
        </>
    ) // return end
} // func end