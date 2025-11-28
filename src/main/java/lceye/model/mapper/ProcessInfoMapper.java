package lceye.model.mapper;

import lceye.model.dto.ProcessInfoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProcessInfoMapper {

    // 프로세스 이름으로 개별조회
    @Select("select * from process_info where pcname = #{pcname}")
    ProcessInfoDto findByDto(String pcname);

    // 프로세스 이름과 설명만 조회
    @Select("select pcname, pcdesc from process_info")
    List<ProcessInfoDto> matchData();
}// interface end
