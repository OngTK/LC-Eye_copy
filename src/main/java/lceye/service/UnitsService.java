package lceye.service;

import lceye.model.dto.UnitsDto;
import lceye.model.entity.UnitsEntity;
import lceye.model.repository.UnitsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service @RequiredArgsConstructor
@Transactional
public class UnitsService {
    private final UnitsRepository unitsRepository;

    /**
     * 단위소분류 개별조회
     *
     * @param uno 단위소분류번호
     * @return dto
     * @author 민성호
     */
    public UnitsDto findByUno(int uno){
        Optional<UnitsEntity> optional = unitsRepository.findById(uno);
        if (optional.isPresent()){
            UnitsEntity entity = optional.get();
            return entity.toDto();
        }//if end
        return null;
    }// func end

    /**
     * [UN-01] 단위 조회
     */
    public List<UnitsDto> readAllUnit(){
        return unitsRepository.findAll().stream().map(UnitsEntity :: toDto).toList();
    }// func end
}// class end
