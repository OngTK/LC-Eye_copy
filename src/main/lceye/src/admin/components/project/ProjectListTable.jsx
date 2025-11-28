import { useEffect, useMemo, useRef, useState } from "react";
import "../../../assets/css/projectListTable.css";

export default function PorjectListTable({
  columns = [],                // [{ id, title, width? }]
  data = [],                   // [{ [id]: value }]
  minColWidth = 30,
  rememberKey,                 // e.g. "ProjectListTable"
  stickyFirst = true,
  sortable = true,
  resizeGrab = 10,             // px 범위에서만 resize 허용
  showGuide = false,           // 리사이즈 가이드 라인 표시 여부
  onRowClick,
}) {
  const initialWidths = useMemo(
    () => columns.map((c) => Math.max(minColWidth, c.width ?? 80)),
    [columns, minColWidth]
  );

  const [widths, setWidths] = useState(initialWidths);
  const storageKey = useMemo(
    () => (rememberKey ? `${rememberKey}@v1:${columns.map((c) => c.id).join("|")}` : null),
    [rememberKey, columns]
  );

  const [sort, setSort] = useState({ key: null, dir: "asc" });

  // 컬럼 너비 복원
  useEffect(() => {
    if (!storageKey) return;
    try {
      const saved = JSON.parse(localStorage.getItem(storageKey) || "null");
      if (Array.isArray(saved) && saved.length === columns.length) {
        setWidths(saved);
      }
    } catch {
      // ignore
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [storageKey]);

  const persist = (next) => {
    setWidths(next);
    if (!storageKey) return;
    try {
      localStorage.setItem(storageKey, JSON.stringify(next));
    } catch {
      // ignore
    }
  };

  // 정렬
  const sorted = useMemo(() => {
    if (!sortable || !sort.key) return data;
    const dir = sort.dir === "desc" ? -1 : 1;
    const arr = [...data];
    arr.sort((a, b) => {
      const va = a[sort.key];
      const vb = b[sort.key];
      const na = Number(va);
      const nb = Number(vb);
      if (!Number.isNaN(na) && !Number.isNaN(nb)) {
        return (na - nb) * dir;
      }
      return String(va ?? "").localeCompare(String(vb ?? "")) * dir;
    });
    return arr;
  }, [data, sort, sortable]);

  // 보더 감지 & 드래그 상태
  const tableRef = useRef(null);
  const scrollRef = useRef(null);   // 가이드 라인 컨테이너
  const guideRef = useRef(null);
  const dragRef = useRef({
    leftIdx: null,
    rightIdx: null,
    startX: 0,
    startLeft: 0,
    pairTotal: 0,
  });
  const rafIdRef = useRef(null);

  const nearRightEdge = (ev, el) => {
    const rect = el.getBoundingClientRect();
    const x = (ev.touches?.[0]?.clientX) ?? ev.clientX;
    return rect.right - x <= resizeGrab && rect.right - x >= -2;
  };
  const nearLeftEdge = (ev, el) => {
    const rect = el.getBoundingClientRect();
    const x = (ev.touches?.[0]?.clientX) ?? ev.clientX;
    return x - rect.left <= resizeGrab && x - rect.left >= -2;
  };

  // 커서: 경계 근처에서 col-resize 표시
  const onMouseMove = (e) => {
    const cell = e.target.closest("th,td");
    if (!cell) {
      if (tableRef.current) tableRef.current.style.cursor = "";
      return;
    }
    if (nearRightEdge(e, cell) || (cell.cellIndex > 0 && nearLeftEdge(e, cell))) {
      if (tableRef.current) tableRef.current.style.cursor = "col-resize";
    } else {
      if (tableRef.current) tableRef.current.style.cursor = "";
    }
  };

  // 컬럼 리사이즈 시작
  const onPointerDown = (e) => {
    const cell = e.target.closest("th,td");
    if (!cell) return;

    const cellIdx = cell.cellIndex;

    // 경계 감지
    let targetIdx = null;
    if (nearRightEdge(e, cell)) {
      targetIdx = cellIdx;          // 오른쪽 경계 => 해당 컬럼
    } else if (cellIdx > 0 && nearLeftEdge(e, cell)) {
      targetIdx = cellIdx - 1;      // 왼쪽 경계 => 이전 컬럼
    } else {
      // 경계가 아니면 정렬 처리
      if (sortable && cell.tagName === "TH") {
        const id = columns[cellIdx]?.id;
        if (id) {
          setSort((s) =>
            s.key !== id ? { key: id, dir: "asc" } : { key: id, dir: s.dir === "asc" ? "desc" : "asc" },
          );
        }
      }
      return;
    }

    const leftIdx = targetIdx;
    const rightIdx = leftIdx + 1;
    if (rightIdx >= columns.length) {
      // 마지막 컬럼 바깥 경계는 리사이즈하지 않음
      return;
    }

    e.preventDefault();

    // 가이드 라인 생성
    if (showGuide && !guideRef.current && scrollRef.current) {
      const guide = document.createElement("div");
      guide.className = "resize-guide";
      scrollRef.current.appendChild(guide);
      guideRef.current = guide;
    }
    if (showGuide && guideRef.current && scrollRef.current) {
      const scrollRect = scrollRef.current.getBoundingClientRect();
      const startX = (e.touches?.[0]?.clientX) ?? e.clientX;
      guideRef.current.style.left = `${startX - scrollRect.left}px`;
      guideRef.current.style.display = "block";
    }

    dragRef.current = {
      leftIdx,
      rightIdx,
      startX: (e.touches?.[0]?.clientX) ?? e.clientX,
      startLeft: widths[leftIdx],
      pairTotal: widths[leftIdx] + widths[rightIdx],
    };

    const onMove = (ev) => {
      const doWork = () => {
        rafIdRef.current = null;
        const x = (ev.touches?.[0]?.clientX) ?? ev.clientX;
        const dx = x - dragRef.current.startX;

        const total = dragRef.current.pairTotal;
        const min = minColWidth;
        let left = dragRef.current.startLeft + dx;
        if (left < min) left = min;
        if (left > total - min) left = total - min;
        const right = total - left;

        setWidths((prev) => {
          const next = prev.slice();
          next[dragRef.current.leftIdx] = left;
          next[dragRef.current.rightIdx] = right;
          return next;
        });

        if (showGuide && guideRef.current && scrollRef.current) {
          const scrollRect = scrollRef.current.getBoundingClientRect();
          guideRef.current.style.left = `${x - scrollRect.left}px`;
        }

        document.body.style.cursor = "col-resize";
        document.body.style.userSelect = "none";
      };

      if (!rafIdRef.current) rafIdRef.current = requestAnimationFrame(doWork);
    };

    const onUp = (ev) => {
      // 최종 너비를 로컬스토리지에 저장
      persist(
        ((curr) => {
          const x = (ev.touches?.[0]?.clientX) ?? ev.clientX;
          const dx = x - dragRef.current.startX;
          const total = dragRef.current.pairTotal;
          const min = minColWidth;
          let left = dragRef.current.startLeft + dx;
          if (left < min) left = min;
          if (left > total - min) left = total - min;
          const right = total - left;
          const next = curr.slice();
          next[dragRef.current.leftIdx] = left;
          next[dragRef.current.rightIdx] = right;
          return next;
        })(widths),
      );

      if (showGuide && guideRef.current) guideRef.current.style.display = "none";
      document.body.style.cursor = "";
      document.body.style.userSelect = "";
      if (rafIdRef.current) cancelAnimationFrame(rafIdRef.current);
      rafIdRef.current = null;

      window.removeEventListener("mousemove", onMove);
      window.removeEventListener("mouseup", onUp);
      window.removeEventListener("touchmove", onMove);
      window.removeEventListener("touchend", onUp);
      dragRef.current = {
        leftIdx: null,
        rightIdx: null,
        startX: 0,
        startLeft: 0,
        pairTotal: 0,
      };
    };

    window.addEventListener("mousemove", onMove);
    window.addEventListener("mouseup", onUp, { once: true });
    window.addEventListener("touchmove", onMove, { passive: false });
    window.addEventListener("touchend", onUp, { once: true });
  };

  const totalWidth = widths.reduce((sum, w) => sum + w, 0) || 1;

  return (
    <div className="rzTable-wrap">
      <div className="rzTable-scroll admin-scope" ref={scrollRef}>
        <table
          ref={tableRef}
          className={`rzTable ${stickyFirst ? "has-sticky-first" : ""}`}
          onMouseMove={onMouseMove}
          onMouseDown={onPointerDown}
          onTouchStart={onPointerDown}
        >
          <colgroup>
            {widths.map((w, i) => (
              <col
                key={columns[i].id}
                style={{ width: `${(w / totalWidth) * 100}%` }}
              />
            ))}
          </colgroup>

          <thead>
            <tr>
              {columns.map((c, i) => (
                <th key={c.id} className={i === 0 && stickyFirst ? "sticky-first" : ""}>
                  <div className="rz-th-inner">
                    <span className="rz-title">
                      {c.title}
                      {sortable && sort.key === c.id && (sort.dir === "asc" ? " ↑" : " ↓")}
                    </span>
                    <span className="rz-border-visual" />
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {sorted.length === 1 && sorted[0]?.__empty ? (
              <tr key="no-data" className="no-data">
                <td
                  colSpan={columns.length}
                  style={{ textAlign: "center", color: "#666", padding: "20px 0" }}
                >
                  조회 결과가 없습니다.
                </td>
              </tr>
            ) : (
              sorted.map((row, rIdx) => (
                <tr
                  key={rIdx}
                  className={row._active ? "active" : undefined}
                  onClick={() => onRowClick?.(row)}
                >
                  {columns.map((c, i) => (
                    <td key={c.id} className={i === 0 && stickyFirst ? "sticky-first" : ""}>
                      {row[c.id]}
                      <span className="rz-border-visual" />
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

