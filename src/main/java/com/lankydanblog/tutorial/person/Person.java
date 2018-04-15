package com.lankydanblog.tutorial.person;

import java.util.Objects;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(name = "people_by_country")
public class Person {

  @PartitionKey private String country;

  @ClusteringColumn private String firstName;

  @ClusteringColumn(1)
  private String lastName;

  @ClusteringColumn(2)
  private UUID id;

  private int age;
  private String profession;
  private int salary;

  private Person() {}

  public Person(
      String country,
      String firstName,
      String lastName,
      UUID id,
      int age,
      String profession,
      int salary) {
    this.country = country;
    this.firstName = firstName;
    this.lastName = lastName;
    this.id = id;
    this.age = age;
    this.profession = profession;
    this.salary = salary;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public String getProfession() {
    return profession;
  }

  public void setProfession(String profession) {
    this.profession = profession;
  }

  public int getSalary() {
    return salary;
  }

  public void setSalary(int salary) {
    this.salary = salary;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person person = (Person) o;
    return age == person.age
        && salary == person.salary
        && Objects.equals(country, person.country)
        && Objects.equals(firstName, person.firstName)
        && Objects.equals(lastName, person.lastName)
        && Objects.equals(id, person.id)
        && Objects.equals(profession, person.profession);
  }

  @Override
  public int hashCode() {
    return Objects.hash(country, firstName, lastName, id, age, profession, salary);
  }

  @Override
  public String toString() {
    return "Person{"
        + "country='"
        + country
        + '\''
        + ", firstName='"
        + firstName
        + '\''
        + ", lastName='"
        + lastName
        + '\''
        + ", id="
        + id
        + ", age="
        + age
        + ", profession='"
        + profession
        + '\''
        + ", salary="
        + salary
        + '}';
  }
}
