import { useRef } from "react";
import '../../../assets/css/dragResizeLayer.css';

export default function SimpleSplitPane({
  initLeftPct = 50,
  minLeftPx = 240,
  minRightPx = 320,
  barPx = 3, // ★ 분리바 너비 상수
  left,
  right
}) {
  const boxRef = useRef(null);
  const barRef = useRef(null);

  const onPointerDown = (e) => {
    e.preventDefault();
    const box = boxRef.current;
    const bar = barRef.current;
    if (!box || !bar) return;

    bar.setPointerCapture?.(e.pointerId);
    const rect = box.getBoundingClientRect();

    const move = (ev) => {
      const clientX = ev.clientX ?? ev.touches?.[0]?.clientX;
      const localX = clientX - rect.left;

      // ★ 우측 최소폭 + 분리바 폭을 함께 고려
      const maxLeftPx = rect.width - minRightPx - barPx;
      const clamped = Math.max(minLeftPx, Math.min(localX, maxLeftPx));
      const pct = (clamped / rect.width) * 100;

      box.style.setProperty("--leftPct", pct + "%");
      document.body.style.cursor = "col-resize";
      document.body.style.userSelect = "none";
    };

    const up = () => {
      window.removeEventListener("pointermove", move);
      window.removeEventListener("pointerup", up);
      document.body.style.cursor = "";
      document.body.style.userSelect = "";
    };

    window.addEventListener("pointermove", move, { passive: false });
    window.addEventListener("pointerup", up, { passive: true });
  };

  return (
    <div
      ref={boxRef}
      className="kt-split"
      style={{ "--leftPct": `${initLeftPct}%`, "--barPx": `${barPx}px`, "--minRight": `${minRightPx}px` }}
    >
      <section className="kt-left">{left}</section>

      <div
        ref={barRef}
        className="kt-bar"
        role="separator"
        aria-orientation="vertical"
        title="드래그하여 영역 너비 조절"
        onPointerDown={onPointerDown}
      />

      <section className="kt-right">{right}</section>

      <style>{`
        .kt-split{
          --leftPct: 50%;
          --barPx: 5px;
          --minRight: 320px;
          display: flex;
          flex-direction: row;
          align-items: stretch;
          width: 100%;
          min-width: 0;
          min-height: 0;
          height: auto;
        }
        .kt-left{
          flex: 0 0 var(--leftPct);
          min-width: ${minLeftPx}px;
          /* ★ 우측 최소폭 + 바 폭만큼은 남겨둔다 */
          max-width: calc(100% - (var(--minRight) + var(--barPx)));
          box-sizing:border-box;
          border-right:1px solid #e7e7e7;

        }
        .kt-bar{
          /* ★ 바가 0으로 찌그러지지 않도록 고정 */
          flex: 0 0 var(--barPx);
          width: var(--barPx);
          align-self: stretch;
          cursor: col-resize; user-select: none; touch-action: none;
          background: linear-gradient(to bottom, transparent, rgba(0,0,0,.10), transparent);
          z-index: 200;
          
        }
        .kt-bar:hover{ background-color: rgba(0,0,0,.14); }
        .kt-right{
          flex: 1 1 auto;
          min-width: var(--minRight);
          border-left:1px solid #e7e7e7; 
        }
        
        @media (max-width: 1100px){
          .kt-split{
            flex-direction:column;
            height: auto;
            align-items: stretch;
          }
          .kt-left{ flex: 0 0 auto; width:100%; min-width:0; max-width:none; border-right:0; border-bottom:1px solid #e7e7e7; }
          .kt-bar{ height: var(--barPx); width:100%; cursor: row-resize; flex: 0 0 var(--barPx); }
          .kt-right{ min-width:0; }
        }
      `}</style>
    </div>
  );
}
