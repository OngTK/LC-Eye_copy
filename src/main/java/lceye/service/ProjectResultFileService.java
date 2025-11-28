package lceye.service;

import org.springframework.stereotype.Service;

import java.util.Optional;

import jakarta.transaction.Transactional;
import lceye.model.entity.ProjectResultFileEntity;
import lceye.model.repository.ProjectResultFileRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectResultFileService {
    private final ProjectResultFileRepository projectResultFileRepository;

    public String getResultFileName(int pjno){
        return projectResultFileRepository.returnFilename(pjno);
    } // func end

    public void deleteResultFileName(int pjno){
        int prfno = projectResultFileRepository.getPrfno(pjno);
        Optional<ProjectResultFileEntity> optional = projectResultFileRepository.findById(prfno);
        System.out.println("optional = " + optional);
        if (optional.isPresent()){
            ProjectResultFileEntity projectResultFileEntity = optional.get();
            System.out.println("projectResultFileEntity = " + projectResultFileEntity);
            projectResultFileEntity.setPrfname(null);
        } // if end
    } // func end
} // class end