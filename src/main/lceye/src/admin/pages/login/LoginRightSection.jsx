import lceye from '../../../assets/img/LC-Eye.svg';
import { Button, Input, Alert, Snackbar } from '@mui/joy';
import axios from 'axios';
import { useState } from 'react';

const axiosOption = { withCredentials: true };

export default function LoingRightSection(props) {
    //======================= useState =======================
    const [idInput, setIdInput] = useState('');
    const [pwdInput, setPwdInput] = useState('');
    const [openAlert, setOpenAlert] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    const login = async () => {
        if (!idInput || !pwdInput) {
            setErrorMessage('아이디와 비밀번호를 모두 입력해주세요.');
            setOpenAlert(true);
            return;
        } // if end
        try {
            const obj = {
                mid: idInput,
                mpwd: pwdInput
            } // obj end
            const response = await axios.post("http://localhost:8080/api/member/login", obj, axiosOption);
            const data = await response.data;
            if (data != null) location.href = "/project"
        } catch (error) {
            console.log(error);
            setErrorMessage('아이디 또는 비밀번호가 일치하지 않습니다.');
            setOpenAlert(true);
        } // try-catch end
    } // func end

    const handelIdChange = (e) => {
        const input = e.target.value;
        if (input.length <= 18) {
            setIdInput(input);
        } // if end
    } // func end

    const handlePwdChange = (e) => {
        const input = e.target.value;
        if (input.length <= 18) {
            setPwdInput(input);
        } // if end
    } // func end

    const enterKeyEvent = () => {
        if (window.event.keyCode == 13) login();
    } // func end

    const handleCloseAlert = (event, reason) => {
        setOpenAlert(false); // 알림창 닫기 (false)
    };

    return (
        <>
            <div className="rightSection">
                <div>
                    <img src={lceye} alt="lceye 이미지" />
                </div>
                <div className='inputBox'>
                    <Input
                        className='loginInput'
                        value={idInput}
                        onChange={handelIdChange}
                        onKeyUp={enterKeyEvent}
                        placeholder="아이디"
                        required
                    />
                    <Input
                        className='loginInput'
                        type='password'
                        value={pwdInput}
                        onChange={handlePwdChange}
                        onKeyUp={enterKeyEvent}
                        placeholder="비밀번호"
                        required
                    />
                    <Button type="submit" onClick={login}>로그인</Button>

                </div>
            </div>
            <Snackbar
                color='danger'
                variant="soft"
                open={openAlert}
                autoHideDuration={3000}
                onClose={handleCloseAlert}
                anchorOrigin={{ vertical: 'top', horizontal: 'center' }} // 위치: 상단 중앙
            >
                <Alert
                    key="Warning"
                    color='danger'
                    variant="soft"
                    onClose={handleCloseAlert}
                    sx={{ width: '100%', fontSize: 17 }}
                >
                    {errorMessage}
                </Alert>
            </Snackbar>
        </>
    ) // return end
} // func end