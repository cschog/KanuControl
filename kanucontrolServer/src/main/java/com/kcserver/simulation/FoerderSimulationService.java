package com.kcserver.simulation;

import com.kcserver.config.FoerderConfig;
import com.kcserver.service.FoerdersatzService;
import com.kcserver.service.KikZuschlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FoerderSimulationService {

    private final FoerdersatzService foerdersatzService;
    private final KikZuschlagService kikZuschlagService;

}