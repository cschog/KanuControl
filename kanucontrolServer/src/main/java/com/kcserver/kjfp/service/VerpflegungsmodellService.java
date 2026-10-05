package com.kcserver.kjfp.service;
import com.kcserver.kjfp.dto.verpflegung.VerpflegungsmodellCreateUpdateDTO;
import com.kcserver.kjfp.dto.verpflegung.VerpflegungsmodellDTO;
import com.kcserver.kjfp.dto.verpflegung.VerpflegungsmodellRefDTO;

import java.util.List;

public interface VerpflegungsmodellService {

    List<VerpflegungsmodellDTO> getAll();

    List<VerpflegungsmodellDTO> getActive();

    VerpflegungsmodellDTO getById(Long id);

    VerpflegungsmodellDTO create(
            VerpflegungsmodellCreateUpdateDTO dto);

    VerpflegungsmodellDTO update(
            Long id,
            VerpflegungsmodellCreateUpdateDTO dto);

    List<VerpflegungsmodellRefDTO> getRefs();

    void delete(Long id);
}