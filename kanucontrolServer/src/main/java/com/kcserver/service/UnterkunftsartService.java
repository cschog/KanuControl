package com.kcserver.service;

import com.kcserver.dto.unterkunft.UnterkunftsartCreateUpdateDTO;
import com.kcserver.dto.unterkunft.UnterkunftsartDTO;
import com.kcserver.dto.unterkunft.UnterkunftsartRefDTO;

import java.util.List;

public interface UnterkunftsartService {

    List<UnterkunftsartDTO> getAll();

    List<UnterkunftsartDTO> getActive();

    UnterkunftsartDTO getById(Long id);

    UnterkunftsartDTO create(UnterkunftsartCreateUpdateDTO dto);

    UnterkunftsartDTO update(Long id, UnterkunftsartCreateUpdateDTO dto);

    List<UnterkunftsartRefDTO> getRefs();

    void delete(Long id);
}