import { createContext, useContext, useMemo, useRef, useState } from "react";
import Modal from "@mui/joy/Modal";
import Sheet from "@mui/joy/Sheet";
import Typography from "@mui/joy/Typography";
import CircularProgress from "@mui/joy/CircularProgress";

const LoadingContext = createContext(null);

export function LoadingProvider({ children }) {
  const [state, setState] = useState({
    open: false,
    message: "로딩중입니다.",
  });
  const timerRef = useRef(null);
  const currentIdRef = useRef(0);

  const hide = (id) => {
    if (id && id !== currentIdRef.current) return;
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
    setState((prev) => ({ ...prev, open: false }));
  };

  const show = (message = "로딩중입니다.", options = {}) => {
    const nextId = currentIdRef.current + 1;
    currentIdRef.current = nextId;
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
    setState({ open: true, message });
    if (options.autoHideMs && options.autoHideMs > 0) {
      timerRef.current = setTimeout(() => {
        if (currentIdRef.current === nextId) {
          setState((prev) => ({ ...prev, open: false }));
        }
      }, options.autoHideMs);
    }
    return nextId;
  };

  const value = useMemo(
    () => ({
      showLoading: show,
      hideLoading: hide,
    }),
    []
  );

  return (
    <LoadingContext.Provider value={value}>
      {children}
      <Modal
        open={state.open}
        aria-label="loading"
        sx={{ display: "flex", alignItems: "center", justifyContent: "center" }}
      >
        <Sheet
          variant="soft"
          color="neutral"
          sx={{
            p: 3,
            borderRadius: 12,
            minWidth: 220,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            gap: 1.5,
            boxShadow: "lg",
          }}
        >
          <CircularProgress size="lg" />
          <Typography level="title-sm">{state.message}</Typography>
        </Sheet>
      </Modal>
    </LoadingContext.Provider>
  );
}

export function useLoading() {
  const ctx = useContext(LoadingContext);
  if (!ctx) throw new Error("useLoading must be used within LoadingProvider");
  return ctx;
}
