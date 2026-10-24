package com.itam.service.importer.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import com.itam.model.entity.Department;
import com.itam.model.entity.Person;

/**
 * Tek bir içe aktarma boyunca kişi ve departman eşlemeleri. İlk kullanıldıklarında
 * yükleniyorlar; departman kolonu olmayan bir tip departman tablosunu hiç okumuyor.
 */
final class ImportLookups {

    static final Locale TR = Locale.forLanguageTag("tr");

    private final Supplier<List<Person>> peopleSource;
    private final Supplier<List<Department>> departmentSource;
    private People people;
    private Map<String, Long> departments;

    ImportLookups(Supplier<List<Person>> peopleSource, Supplier<List<Department>> departmentSource) {
        this.peopleSource = peopleSource;
        this.departmentSource = departmentSource;
    }

    /**
     * Kişi kolonları sicil kodu, e-posta ya da tam adla doldurulabilir (export tam ad
     * yazıyor). Aynı adda iki kişi varsa tahmin yürütmek yerine hata veriyoruz.
     * Bulunamazsa hata mesajı fırlatır.
     */
    Long person(String text) {
        if (people == null) {
            people = new People(peopleSource.get());
        }
        return people.resolve(text);
    }

    /** Departman kodu ya da adıyla; export adı yazıyor. */
    Long department(String text) {
        if (departments == null) {
            departments = new HashMap<>();
            for (Department d : departmentSource.get()) {
                if (!d.isActive()) {
                    continue;
                }
                departments.put(d.getName().trim().toLowerCase(TR), d.getId());
                if (d.getCode() != null) {
                    departments.putIfAbsent(d.getCode().toLowerCase(TR), d.getId());
                }
            }
        }
        Long id = departments.get(text.toLowerCase(TR));
        if (id == null) {
            throw new LookupException("Departman bulunamadı: " + text);
        }
        return id;
    }

    static final class LookupException extends RuntimeException {
        LookupException(String message) {
            super(message, null, false, false);
        }
    }

    private static final class People {
        private final Map<String, Long> byCode = new HashMap<>();
        private final Map<String, Long> byEmail = new HashMap<>();
        private final Map<String, List<Long>> byName = new HashMap<>();

        People(List<Person> people) {
            for (Person p : people) {
                if (!p.isActive()) {
                    continue;
                }
                if (p.getCode() != null) {
                    byCode.put(p.getCode().toLowerCase(Locale.ROOT), p.getId());
                }
                if (p.getEmail() != null) {
                    byEmail.put(p.getEmail().toLowerCase(Locale.ROOT), p.getId());
                }
                if (p.getFullName() != null) {
                    byName.computeIfAbsent(p.getFullName().trim().toLowerCase(TR), k -> new ArrayList<>())
                            .add(p.getId());
                }
            }
        }

        Long resolve(String text) {
            String key = text.toLowerCase(Locale.ROOT);
            Long id = byCode.get(key);
            if (id == null) {
                id = byEmail.get(key);
            }
            if (id != null) {
                return id;
            }
            List<Long> named = byName.getOrDefault(text.toLowerCase(TR), List.of());
            if (named.size() == 1) {
                return named.get(0);
            }
            throw new LookupException(named.isEmpty()
                    ? "Kişi bulunamadı: " + text
                    : "Bu adda birden fazla kişi var, sicil kodu ya da e-posta yazın: " + text);
        }
    }
}
