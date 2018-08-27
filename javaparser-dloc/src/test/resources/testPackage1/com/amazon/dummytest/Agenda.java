// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.dummytest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

@Nonnull
public class Agenda {

    /**
     * person list.
     */
    List<Person> persons = new ArrayList<>();

    public void addPerson(Person person) {
        persons.add(person);
    }

    public Person getPerson() {
        return persons.get(0);
    }

    public List<Address> findAddressesOfPersons(String personName) {
        return persons.stream()
                .filter(p -> p.getName().equals(personName))
                .map(p -> p.getAddress())
                .collect(Collectors.toList());
    }

}