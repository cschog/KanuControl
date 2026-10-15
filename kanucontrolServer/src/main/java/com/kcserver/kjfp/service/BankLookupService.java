package com.kcserver.kjfp.service;

import com.kcserver.core.entity.Person;
import com.kcserver.core.entity.Verein;

public interface BankLookupService {

    String findBankName(String bic);

    void fillBankNameIfMissing(Person person);

    void fillBankNameIfMissing(Verein verein);
}