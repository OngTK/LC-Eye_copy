package lceye.model.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import lceye.model.dto.MemberDto;

@Mapper
public interface MemberMapper {
    // 1. 요청된 로그인의 유효성 체크
    @Select("SELECT * FROM member JOIN company USING (cno) WHERE mid = #{mid} AND mpwd = #{mpwd}")
    MemberDto login(MemberDto memberDto);
} // interface end