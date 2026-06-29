package com.kcserver.service;
import com.kcserver.dto.verpflegung.VerpflegungsmodellCreateUpdateDTO;
import com.kcserver.dto.verpflegung.VerpflegungsmodellDTO;
import com.kcserver.dto.verpflegung.VerpflegungsmodellRefDTO;

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