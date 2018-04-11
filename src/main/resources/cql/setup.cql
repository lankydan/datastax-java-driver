CREATE TABLE IF NOT EXISTS people_by_country(
  country TEXT,
  first_name TEXT,
  last_name TEXT,
  id UUID,
  age INT,
  profession TEXT,
  salary INT,
  PRIMARY KEY((country), first_name, last_name, id)
);

TRUNCATE TABLE people_by_country;