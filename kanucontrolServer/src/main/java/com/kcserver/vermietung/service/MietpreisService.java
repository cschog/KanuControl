package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.MietpreisDTO;
import com.kcserver.vermietung.entity.Mietbereich;
import com.kcserver.vermietung.entity.Mietpreis;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.vermietung.repository.MietbereichRepository;
import com.kcserver.vermietung.repository.MietpreisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MietpreisService {

    private final MietpreisRepository mietpreisRepository;
    private final MietbereichRepository mietbereichRepository;

    /**
     * Ermittelt den am Stichtag gültigen Preis.
     */
    public Mietpreis ermittleGueltigenPreis(
            Long mietbereichId,
            Buchungsquelle buchungsquelle,
            LocalDate stichtag
    ) {
        return mietpreisRepository
                .findFirstByMietbereichIdAndBuchungsquelleAndGueltigAbLessThanEqualOrderByGueltigAbDesc(
                        mietbereichId,
                        buchungsquelle,
                        stichtag
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Kein gültiger Mietpreis für Mietbereich "
                                + mietbereichId
                                + " und Buchungsquelle "
                                + buchungsquelle
                                + " am "
                                + stichtag
                                + " vorhanden."
                ));
    }

    /**
     * Listet alle Preise eines Mietbereichs auf.
     */
    public List<MietpreisDTO> findeNachMietbereich(Long mietbereichId) {
        return mietpreisRepository
                .findByMietbereichIdOrderByGueltigAbDesc(mietbereichId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Legt einen neuen Mietpreis an.
     */
    @Transactional
    public MietpreisDTO anlegen(MietpreisDTO dto) {
        pruefePreis(dto);

        if (mietpreisRepository
                .existsByMietbereichIdAndBuchungsquelleAndGueltigAb(
                        dto.getMietbereichId(),
                        dto.getBuchungsquelle(),
                        dto.getGueltigAb()
                )) {
            throw new IllegalArgumentException(
                    "Für diesen Mietbereich, diese Buchungsquelle "
                            + "und diesen Gültigkeitsbeginn existiert bereits ein Preis."
            );
        }

        Mietbereich mietbereich = findeMietbereich(dto.getMietbereichId());

        Mietpreis mietpreis = new Mietpreis();
        mietpreis.setMietbereich(mietbereich);
        mietpreis.setBuchungsquelle(dto.getBuchungsquelle());
        mietpreis.setGueltigAb(dto.getGueltigAb());
        mietpreis.setPreis(dto.getPreis());
        mietpreis.setBemerkung(dto.getBemerkung());

        return toDTO(mietpreisRepository.save(mietpreis));
    }

    /**
     * Ändert einen bestehenden Mietpreis.
     */
    @Transactional
    public MietpreisDTO aktualisieren(Long id, MietpreisDTO dto) {
        pruefePreis(dto);

        Mietpreis mietpreis = mietpreisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mietpreis mit ID " + id + " wurde nicht gefunden."
                ));

        boolean bereitsVorhanden =
                mietpreisRepository
                        .existsByMietbereichIdAndBuchungsquelleAndGueltigAbAndIdNot(
                                dto.getMietbereichId(),
                                dto.getBuchungsquelle(),
                                dto.getGueltigAb(),
                                id
                        );

        if (bereitsVorhanden) {
            throw new IllegalArgumentException(
                    "Für diesen Mietbereich, diese Buchungsquelle "
                            + "und diesen Gültigkeitsbeginn existiert bereits ein Preis."
            );
        }

        Mietbereich mietbereich = findeMietbereich(dto.getMietbereichId());

        mietpreis.setMietbereich(mietbereich);
        mietpreis.setBuchungsquelle(dto.getBuchungsquelle());
        mietpreis.setGueltigAb(dto.getGueltigAb());
        mietpreis.setPreis(dto.getPreis());
        mietpreis.setBemerkung(dto.getBemerkung());

        return toDTO(mietpreisRepository.save(mietpreis));
    }

    private Mietbereich findeMietbereich(Long mietbereichId) {
        return mietbereichRepository.findById(mietbereichId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mietbereich mit ID " + mietbereichId + " wurde nicht gefunden."
                ));
    }

    private void pruefePreis(MietpreisDTO dto) {
        if (dto.getMietbereichId() == null
                || dto.getBuchungsquelle() == null
                || dto.getGueltigAb() == null
                || dto.getPreis() == null) {
            throw new IllegalArgumentException(
                    "Mietbereich, Buchungsquelle, Gültigkeitsbeginn und Preis sind erforderlich."
            );
        }

        if (dto.getPreis().signum() < 0) {
            throw new IllegalArgumentException(
                    "Der Preis darf nicht negativ sein."
            );
        }
    }

    private MietpreisDTO toDTO(Mietpreis mietpreis) {
        return MietpreisDTO.builder()
                .id(mietpreis.getId())
                .mietbereichId(mietpreis.getMietbereich().getId())
                .mietbereichBezeichnung(mietpreis.getMietbereich().getBezeichnung())
                .buchungsquelle(mietpreis.getBuchungsquelle())
                .gueltigAb(mietpreis.getGueltigAb())
                .preis(mietpreis.getPreis())
                .bemerkung(mietpreis.getBemerkung())
                .build();
    }
}