package lceye.model.mapper;

import lceye.model.dto.ExcelProjectDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExcelProjectMapper {


    /**
     * [Excle-01-01] cno, pjno 로 프로젝트 정보 조회
     */
    @Select("""
                SELECT p.*, ug.ugname, u.unit, m.mname, m.memail, c.cname AS cnmae
                FROM project p
                JOIN member m ON p.mno = m.mno
                JOIN company c ON m.cno = c.cno
                JOIN units u ON p.uno = u.uno
                JOIN unitGroup ug ON u.ugno = ug.ugno
                WHERE p.mno = #{mno}
                AND p.pjno = #{pjno};
            """)
    ExcelProjectDto readByMnoAndPjno(int mno, int pjno);

    /**
     * [Excle-01-02] cno, pjno 로 프로젝트 정보 조회
     */
    @Select("""
                SELECT p.*, ug.ugname, u.unit, m.mname, m.memail, c.cname AS cnmae
                FROM project p
                JOIN member m ON p.mno = m.mno
                JOIN company c ON m.cno = c.cno
                JOIN units u ON p.uno = u.uno
                JOIN unitGroup ug ON u.ugno = ug.ugno
                WHERE m.cno = #{cno}
                AND p.pjno = #{pjno};
            """)
    ExcelProjectDto readByCnoAndPjno(int cno, int pjno);
} // Interface end
