package lceye.service;

import lceye.model.dto.ProcessInfoDto;
import lceye.model.entity.ProcessInfoEntity;
import lceye.model.mapper.ProcessInfoMapper;
import lceye.model.repository.ProcessInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional
@RequiredArgsConstructor
public class ProcessInfoService {
    private final ProcessInfoMapper processInfoMapper;
    private final ProcessInfoRepository processInfoRepository;

    /**
     * 프로세스정보 개별조회
     *
     * @param pcname 프로세스 이름
     * @return dto
     * @author 민성호
     */
    public ProcessInfoDto findByPcname(String pcname){
        return processInfoMapper.findByDto(pcname);
    }// func end

    /**
     * 프로세스정보 전체조회
     *
     * @return List
     */
    public List<ProcessInfoDto> getProcessInfo(){
        return processInfoRepository.findAll()
                .stream().map(ProcessInfoEntity::toDto).toList();
    }// func end

    /**
     * 매칭에 필요한 pcname과 pcdesc 만 조회
     *
     * @return List
     */
    public List<ProcessInfoDto> matchData(){
        return processInfoMapper.matchData();
    }// func end
}// class end
