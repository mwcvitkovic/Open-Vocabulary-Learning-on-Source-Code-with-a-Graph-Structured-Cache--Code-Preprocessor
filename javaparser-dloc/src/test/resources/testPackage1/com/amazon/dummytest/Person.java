// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.dummytest;

public class Person {

    protected String name;
    private Address address;

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(Address address) {
        if (this.address == null) {
            this.address = address;
        }
        throw new RuntimeException("Not expected.");
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return this.address;
    }

    public String compareAndGetName(Person person) {
        if (this.name.hashCode() > person.name.hashCode()) {
           return this.name;
        }
        return person.name;
    }

}