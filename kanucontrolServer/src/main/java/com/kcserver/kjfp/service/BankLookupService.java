package com.kcserver.kjfp.service;

import com.kcserver.kjfp.entity.Person;
import com.kcserver.kjfp.entity.Verein;

public interface BankLookupService {

    String findBankName(String bic);

    void fillBankNameIfMissing(Person person);

    void fillBankNameIfMissing(Verein verein);
}