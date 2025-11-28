import { useEffect, useMemo, useState } from "react";
import FormControl from "@mui/joy/FormControl";
import FormLabel from "@mui/joy/FormLabel";
import Select from "@mui/joy/Select";
import Option from "@mui/joy/Option";
import FormHelperText from "@mui/joy/FormHelperText";

export default function UnitSelector({
  units = [],
  value = null,
  onChange,
  error,
  onErrorClear,
  onDirty,
}) {
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [selectedUnit, setSelectedUnit] = useState(null);

  // 단위 그룹 목록 생성 ===================================================
  const unitGroups = useMemo(() => {
    const map = new Map();
    units.forEach((u) => {
      if (!map.has(u.ugno)) map.set(u.ugno, u.ugname);
    });
    return Array.from(map, ([ugno, ugname]) => ({ ugno, ugname }));
  }, [units]);

  // 선택된 단위 그룹에 따른 상세 단위 목록 필터링 ==============================
  const filteredUnits = useMemo(
    () => units.filter((u) => u.ugno === selectedGroup),
    [units, selectedGroup]
  );

  // value prop이 변경될 때 선택 상태 동기화 ==============================
  useEffect(() => {
    // 1. 외부에서 들어온 value(상세단위 ID)가 있을 경우: 그룹과 단위를 모두 맞춰줌
    if (value) {
      const unit = units.find((u) => u.uno === value);
      if (unit) {
        setSelectedGroup(unit.ugno);
        setSelectedUnit(unit.uno);
      }
    }
    // 2. value가 null인 경우
    else {
      setSelectedUnit(null);
    }
  }, [units, value]);

  // 단위 그룹 변경 핸들러 ==============================================
  const handleGroupChange = (_, newGroup) => {
    onDirty?.();
    setSelectedGroup(newGroup);
    setSelectedUnit(null);
    onChange?.(null);
    onErrorClear?.();
  };

  // 상세 단위 변경 핸들러 ==============================================
  const handleUnitChange = (_, newUno) => {
    onDirty?.();
    setSelectedUnit(newUno);
    onChange?.(newUno);
    onErrorClear?.();
  };

  // return ===================================================
  return (
    <div className="unitSelectArea">
      <FormControl>
        <FormLabel className="projectLabel">단위 그룹</FormLabel>
        <Select
          placeholder="단위 그룹 선택"
          value={selectedGroup}
          onChange={handleGroupChange}
        >
          {unitGroups.map((g) => (
            <Option key={g.ugno} value={g.ugno}>
              {g.ugname}
            </Option>
          ))}
        </Select>
      </FormControl>

      <FormControl error={!!error}>
        <FormLabel className="projectLabel">상세 단위</FormLabel>
        <Select
          placeholder="상세 단위 선택"
          value={selectedUnit}
          onChange={handleUnitChange}
          disabled={!selectedGroup}
        >
          {filteredUnits.map((u) => (
            <Option key={u.uno} value={u.uno}>
              {u.unit}
            </Option>
          ))}
        </Select>
        {error && <FormHelperText>{error}</FormHelperText>}
      </FormControl>
    </div>
  );
}
