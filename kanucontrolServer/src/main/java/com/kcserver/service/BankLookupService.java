package com.kcserver.service;

import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;

public interface BankLookupService {

    String findBankName(String bic);

    void fillBankNameIfMissing(Person person);

    void fillBankNameIfMissing(Verein verein);
}