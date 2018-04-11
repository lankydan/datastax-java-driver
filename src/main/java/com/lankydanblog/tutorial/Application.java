package com.lankydanblog.tutorial;

import java.util.UUID;

import com.lankydanblog.tutorial.person.Person;
import com.lankydanblog.tutorial.person.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

  @Autowired
  private PersonRepository personRepository;

  public static void main(String args[]) {
    SpringApplication.run(Application.class);
  }

  @Override
  public void run(String... args) {

    final Person bob = new Person("UK", "Bob", "Bobbington", UUID.randomUUID(), 50, "Software Developer", 50000);

    final Person john = new Person("UK", "John", "Doe", UUID.randomUUID(), 30, "Doctor", 100000);

    final Person alice = new Person("US", "Alice", "Cooper", UUID.randomUUID(), 45, "Engineer", 1000000);

    personRepository.save(bob);
    personRepository.save(john);
    personRepository.save(alice);

    System.out.println("Find all");
    personRepository.findAll().forEach(System.out::println);

    System.out.println("Find one record");
    System.out.println(personRepository.find(john.getCountry(), john.getFirstName(), john.getLastName(), john.getId()));

    System.out.println("Find all by country");
    personRepository.findAllByCountry("UK").forEach(System.out::println);

    john.setProfession("Unemployed");
    john.setSalary(0);
    personRepository.save(john);
    System.out.println("Demonstrating updating a record");
    System.out.println(personRepository.find(john.getCountry(), john.getFirstName(), john.getLastName(), john.getId()));

    personRepository.delete(john.getCountry(), john.getFirstName(), john.getLastName(), john.getId());
    System.out.println("Demonstrating deleting a record");
    System.out.println(personRepository.find(john.getCountry(), john.getFirstName(), john.getLastName(), john.getId()));
  }
}
