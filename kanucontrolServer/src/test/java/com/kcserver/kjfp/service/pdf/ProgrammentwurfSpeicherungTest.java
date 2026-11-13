package com.kcserver.kjfp.service.pdf;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.core.repository.VeranstaltungRepository;
import com.kcserver.core.dto.veranstaltung.VeranstaltungUpdateDTO;
import com.kcserver.core.mapper.VeranstaltungMapper;
import com.kcserver.core.service.veranstaltung.VeranstaltungServiceImpl;
import com.kcserver.kjfp.service.veranstaltung.KjfpVeranstaltungRegeln;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgrammentwurfSpeicherungTest {
    @Mock VeranstaltungRepository repository;
    @Mock VeranstaltungMapper mapper;
    @Mock KjfpVeranstaltungRegeln regeln;
    @InjectMocks VeranstaltungServiceImpl service;
    @Test void saveOmittedAndClear() {
        Veranstaltung v = new Veranstaltung(); v.setKjfpAktiv(false);
        when(repository.findByIdWithRelations(42L)).thenReturn(Optional.of(v));
        when(repository.save(v)).thenReturn(v);
        VeranstaltungUpdateDTO dto = new VeranstaltungUpdateDTO();
        dto.setProgrammentwurfTitel("Programm"); dto.setProgrammentwurfInhalt("<p><b>Start</b><script>bad()</script></p>");
        service.update(42L,dto);
        assertThat(v.getProgrammentwurfTitel()).isEqualTo("Programm");
        assertThat(v.getProgrammentwurfInhalt()).isEqualTo("<p><strong>Start</strong></p>");
        service.update(42L,new VeranstaltungUpdateDTO());
        assertThat(v.getProgrammentwurfInhalt()).contains("Start");
        dto.setProgrammentwurfTitel(""); dto.setProgrammentwurfInhalt(""); service.update(42L,dto);
        assertThat(v.getProgrammentwurfTitel()).isNull(); assertThat(v.getProgrammentwurfInhalt()).isNull();
    }
}
