import { useCallback, useEffect, useState } from "react";
import axios from "axios";

export default function useUnits() {
  const [units, setUnits] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchUnits = useCallback(async () => {
    setLoading(true);
    try {
      const res = await axios.get("http://localhost:8080/api/units", {
        withCredentials: true,
      });
      const data = Array.isArray(res.data) ? res.data : [];
      setUnits(data);
      setError(null);
    } catch (e) {
      console.error("[/api/units error]", e);
      setError(e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUnits();
  }, [fetchUnits]);

  return { units, loading, error, refresh: fetchUnits };
}
