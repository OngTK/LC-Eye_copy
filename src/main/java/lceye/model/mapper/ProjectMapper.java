package lceye.model.mapper;

import lceye.model.dto.ProjectDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectMapper {

    /**
     * 회사번호로 프로젝트파일명 조회
     *
     * @param cno 회사번호
     * @return json 파일명 리스트
     * @author 민성호
     */
    @Select("select pjfilename from project p inner join member m on p.mno = m.mno where m.cno = #{cno}")
    List<String> findByCno(int cno);

    /**
     * [PJ-02-01] 프로젝트 전체 조회
     * <p>
     * 검색 기준 : mno 기반 전체 조회
     * @author OngTK
     */
    @Select("""
            select p.*, m.mname from project p join member m on p.mno = m.mno where p.mno = #{mno};
            """)
    List<ProjectDto> readByMno( int mno);

    /**
     * [PJ-02-02] 프로젝트 전체 조회
     * <p>
     * 검색 기준 : cno
     * @author OngTK
     */
    @Select("""
            select p.*, m.mname from project p join member m on p.mno = m.mno where m.cno= #{cno};
            """)
    List<ProjectDto> readByCno(int cno);
}// interface end
