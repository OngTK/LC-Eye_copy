import { useDispatch, useSelector } from "react-redux"
import { Navigate, Outlet } from "react-router-dom";
import axios from 'axios';
import { useEffect, useState } from "react";
import { checkingLogin } from "../store/adminSlice";
import '../../assets/css/login.css';
import Sheet from "@mui/joy/Sheet";
import LinearProgress from "@mui/joy/LinearProgress";
import Typography from "@mui/joy/Typography";

const axiosOption = { withCredentials: true };

export default function RoleRoute(props) {
    //======================= useDispatch =======================
    const dispatch = useDispatch();
    //======================= useSelector =======================
    const { isLogin } = useSelector((state) => state.admin);
    console.log(isLogin);
    //======================= checkAuth =======================
    const checkAuth = async () => {
        try {
            const response = await axios.get("http://localhost:8080/api/member/getinfo", axiosOption);
            const data = await response.data;
            dispatch(checkingLogin(data));
        } catch (error) {
            dispatch(checkingLogin({
                ...isLogin,
                isAuth: false
            }))
        } // try-catch end
    } // func end
    //======================= useEffect - 최초 렌더링시 1회 실행 =======================
    useEffect(() => {
        checkAuth();
    }, []);
    // 1. 아직 권한(로그인) 확인중이라면, 안내문구 출력
    if (isLogin.isAuth == null) return (<>
        <LoginCheckingBox/>
    </>)


    // 2. 만약 비로그인 상태라면, 메인페이지로 이동
    if (isLogin.isAuth == false) return <Navigate to="/" />

    // 3. 
    if (!props.roles.includes(isLogin.role)) return <Navigate to="/" />

    // 4. 만약 로그인 상태라면, 자식 컴포넌트 렌더링하기
    return (
        <>
            <Outlet />
        </>
    ) // return end
} // func end

function LoginCheckingBox() {
  // progress 상태는 useState + useEffect로 0→100 천천히 증가시키면 됩니다.
  const [value, setValue] = useState(0);

  // ...타이머로 setValue(0→100) 구현
   useEffect(() => {
    // 1초 동안 0 → 100 으로 채우기
    const duration = 50; // 1s
    const start = performance.now();

    const tick = (now) => {
      const elapsed = now - start;
      const ratio = Math.min(elapsed / duration, 1); // 0 ~ 1
      setValue(ratio * 100);
      if (ratio < 1) requestAnimationFrame(tick);
    };

    const id = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(id);
  }, []);

  return (
    <Sheet
      variant="outlined"
      sx={{
        width: 400,
        borderRadius: "12px",
        p: 3,
        bgcolor: "#f4f4f4",
      }}
      className="loginCheckingBox"
    >
      <LinearProgress
        determinate
        value={value}
        sx={{
          mb: 2,
          borderRadius: 999,
          overflow: "hidden",
          "& .MuiLinearProgress-bar": {
            borderRadius: 999,
            bgcolor: "#0b2945", // 짙은 남색
          },
        }}
      />
      <Typography textAlign="center" className="loginCheckingText">
        로그인 확인중 입니다.
      </Typography>
    </Sheet>
  );
}