package com.kcserver.kjfp.usecase;

import com.kcserver.kjfp.entity.beitraege.Beitragsregel;
import com.kcserver.kjfp.service.beitrag.BeitragsregelValidator;
import com.kcserver.core.enumtype.TeilnehmerRolle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class BeitragsregelValidierungUseCaseTest {
    final BeitragsregelValidator validator=new BeitragsregelValidator();
    Beitragsregel regel(Integer sort,Integer bis){var r=new Beitragsregel();r.setSortierung(sort);r.setAlterBis(bis);return r;}
    @Test void altersstufenSindJeRolleUnabhaengigUndMuessenOffenEnden(){var ma=regel(1,null);ma.setRolle(TeilnehmerRolle.MITARBEITER);assertThatCode(()->validator.validate(List.of(regel(1,10),regel(2,20),regel(3,null),ma))).doesNotThrowAnyException();}
    @ParameterizedTest @ValueSource(strings={"leer","negativ","absteigend","doppelt","offenVorLetzter","ohneOffen","fehlendeSortierung"})
    void ungueltigeAltersstufenLiefernFachfehler(String fall) {
        List<Beitragsregel> regeln=switch(fall){
            case "leer"->List.of();case "negativ"->List.of(regel(1,-1),regel(2,null));case "absteigend"->List.of(regel(1,20),regel(2,10),regel(3,null));case "doppelt"->List.of(regel(1,20),regel(2,20),regel(3,null));case "offenVorLetzter"->List.of(regel(1,null),regel(2,20));case "ohneOffen"->List.of(regel(1,20));default->List.of(regel(null,20),regel(2,null));};
        assertThatThrownBy(()->validator.validate(regeln)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class).hasMessageContaining("400");
    }
}
