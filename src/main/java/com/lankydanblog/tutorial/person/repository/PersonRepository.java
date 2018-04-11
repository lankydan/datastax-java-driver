package com.lankydanblog.tutorial.person.repository;

import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.CreateKeyspace;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.lankydanblog.tutorial.person.Person;
import org.springframework.stereotype.Repository;

import static com.datastax.driver.core.DataType.*;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Repository
public class PersonRepository {

  private Mapper<Person> mapper;
  private Session session;

  private static final String TABLE = "people_by_country";

  public PersonRepository(MappingManager mappingManager) {
    this.mapper = mappingManager.mapper(Person.class);
    this.session = mappingManager.getSession();
  }

  public static Create tableSchema() {
    SchemaBuilder.createTable(TABLE).addPartitionKey("country", text()).addClusteringColumn("first_name", text()).addClusteringColumn("last_name", text()).addClusteringColumn("id", uuid()).addColumn("age", cint()).addColumn("profession", text()).addColumn("salary", cint());
  }

  public Person find(String country, String firstName, String secondName, UUID id) {
    return mapper.get(country, firstName, secondName, id);
  }

  public List<Person> findAll() {
    final ResultSet result = session.execute(select().all().from(TABLE));
    return mapper.map(result).all();
  }

  public List<Person> findAllByCountry(String country) {
    final ResultSet result = session.execute(select().all().from(TABLE).where(eq("country", country)));
    return mapper.map(result).all();
  }

  public void delete(String country, String firstName, String secondName, UUID id) {
    mapper.delete(country, firstName, secondName, id);
  }

  public Person save(Person person) {
    mapper.save(person);
    return person;
  }

}
