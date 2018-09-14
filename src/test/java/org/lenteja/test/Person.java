package org.lenteja.test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Person {

    IdPerson id;
    String name;
    int age;
    Date birthDate;

    public Person() {
        super();
    }

    public Person(IdPerson id, String name, int age, Date birthDate) {
        super();
        this.id = id;
        this.name = name;
        this.age = age;
        this.birthDate = birthDate;
    }

    public IdPerson getId() {
        return id;
    }

    public void setId(IdPerson id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return "Person [id=" + id + ", name=" + name + ", age=" + age + ", birthDate=" + sdf.format(birthDate) + "]";
    }

}