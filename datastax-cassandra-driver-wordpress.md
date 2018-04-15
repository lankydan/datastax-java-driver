I'm back with more Cassandra and Java integration today this time focusing on using the Datastax Java driver rather than Spring Data Cassandra which I have already written about quite a lot. The Datastax driver is actually used by Spring Data to interact with Cassandra but comes with some extra goodies built on top of it. But we don't want any of these today! We are going to use the Datastax driver directly and at the end of the post once we have seen how use it we will compare it against Spring Data.

This post makes the assumption that you are already familiar with Cassandra and possibly Spring Data Cassandra. Since I have already written quite a few posts around this subject I have only brushed over how Cassandra works where context is required. If you do not have this background information I recommend reading <a href="https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/" target="_blank" rel="noopener">Getting started with Spring Data Cassandra</a> where I obviously talked about using Spring Data Cassandra but also went into more thorough explanations of how Cassandra works than I do in this post. There is also the <a href="https://academy.datastax.com/courses" target="_blank" rel="noopener">Datastax Academy</a> which provide some very useful resources for learning how to use Cassandra yourself.

First things first, dependencies.

[gist https://gist.github.com/lankydan/e1766b6f87c31de174b7f102037d9511 /]

As always I am using Spring Boot, just because we are depriving ourselves of Spring Data doesn't mean we need to go completely cold turkey from all Spring libraries. The Datastax related dependencies here are <code>cassandra-driver-core</code> and <code>cassandra-driver-mapping</code>. <code>cassandra-driver-core</code>, as the name suggests provides the core functionality to interact with Cassandra such as setting up a session and writing queries. <code>cassandra-driver-mapping</code> is not required to query Cassandra but does provide some object mapping, in conjunction with the core driver it will now serve as an ORM rather than only allowing us execute CQL statements.

We now have our dependencies sorted, the next step is to get connected to Cassandra so that we can actually start querying it.

[gist https://gist.github.com/lankydan/bb9af7cba9a379871e5853fdcc87cff5 /]

There is a bit more core here when compared to a similar setup using Spring Data (this class isn't even needed when combined with Spring Boot's auto-configuration) but the class itself is pretty simple. The basic setup of the <code>Cluster</code> and <code>Session</code> beans shown here is the bare minimum required for the application to work and will likely remain the same for any application you write. More methods are provided so you can add any additional configuration to make them suitable for your use-case.

By using values from <code>application.properties</code> we set the host address, cluster name and port of the <code>Cluster</code>. The <code>Cluster</code> is then used to create a <code>Session</code>. There are two options to choose from when doing this, setting the default keyspace or not. If you want to set the default keyspace then all you need to do is use the below code instead.

[gist https://gist.github.com/lankydan/45dab6ed3159b62878c7c2c61f2b3834 /]

The keyspace is passed into the <code>connect</code> method which will create a <code>Session</code> and then execute <code>USE <keyspace></code> thus setting the default keyspace. This relies on the keyspace existing before creating the session, if it does not it will fail when executing the <code>USE</code> statement.

If you do not know if the keyspace exists at startup or know that you definitely want to create it dynamically based on the keyspace value from the properties file, then you will need to call <code>connect</code> without specifying the keyspace. You will then need to create it yourself so you actually have something to use. To do this use of the <code>createKeyspace</code> method provided by <code>SchemaBuilder</code>. Below is the CQL statement to create the keyspace.
<pre>CREATE KEYSPACE IF NOT EXISTS <keyspace> WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1 };
</pre>
I have also added the keyspace code below again as its a bit far away now.

[gist https://gist.github.com/lankydan/9ac9cbb65ac4d67b6112a5a501b0c69c /]

The <code>SchemaBuilder</code> is nice and easy to use and looks very similar to the CQL as you go through it. We add a <code>ifNotExists</code> clause and set the replication factor by first calling <code>with</code> and then passing a <code>Map<String, Object></code> into the <code>replicationMethod</code>. This map needs to contain the class and replication factor, basically use the keys shown here but change the mapped values to whatever you need them to be. Don't forget to <code>execute</code> the statement and then tell the session to use the keyspace that was just created. Unfortunately there isn't a nicer way to set the default keyspace manually and executing a <code>USE</code> statement is the only option.

Following on from the two previous options regarding setting the default keyspace. If we choose to not set the default keyspace at all, then we need to prepend a keyspace onto each table we create and for every query that is executed. It isn't too hard to do as Datastax provides ways to add keyspace names to queries as well as onto entities for mapping. I won't go into this subject any further, but know that not setting the keyspace will not prevent your application from working if you have setup everything else correctly.

Once the keyspace is set we can get around to creating the tables. There are two possible ways to do this. One, execute some CQL statements, whether they are strings in your Java code or read from a external CQL script. Two, use the <code>SchemaBuilder</code> to create them.

Let's look at executing CQL statements first, or more precisely executing them from a CQL file. You might have noticed that I left some commented out code in the original example, when uncommented that code will find a file named <code>setup.cql</code>, read out a single CQL statement, execute it and then move onto the next statement. Here it is again.

[gist https://gist.github.com/lankydan/06ae39cc0a70270f1be7dcdfbc407d7a /]

Below is the CQL contained in the file to create the Cassandra table.

[gist https://gist.github.com/lankydan/f9b3d3ef67240f189cb2a4c1b0f8d156 /]

The primary key consists of the <code>country</code>, <code>first_name</code>, <code>last_name</code> and <code>id</code> field. The partition key consists of just the <code>country</code> field and the clustering columns are the remaining keys in the key, <code>id</code> is only included for uniqueness as you can obviously have people with the same names. I go into the topic of primary keys in much more depth in my earlier post, <a href="https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/" target="_blank" rel="noopener">Getting started with Spring Data Cassandra</a>.

This code makes use of the <code>commons-io</code> and <code>commons-lang3</code> dependencies. If we are not executing CQL in this way, then these dependencies can be removed (within the context of this post).

What about using the <code>SchemaBuilder</code>? I haven't included any code to create a table in the original snippet because I was playing around and trying to figure out the nicest place to put it, for now I have stuck it in the repository but I'm still not convinced thats the perfect place for it. Anyway, I will paste the code here so we can look at it now and then we can skip over it later when it reappears.

[gist https://gist.github.com/lankydan/bbf8bda5bbe4fc9e9c87b5b11c1a4a36 /]

This matches up quite nicely with the CQL shown above. We are able to define the different column types using <code>addPartitionKey</code> and <code>addClusteringColumn</code> to create our primary key and <code>addColumn</code> for the standard fields. There are plenty of other methods, such as <code>addStaticColumn</code> and <code>withOptions</code> allowing you to then call <code>clusteringOrder</code> to define the sorting direction of your clustering columns. The order that you call these methods is very important as the partition key and clustering columns will be created in the order which their respective methods are called. Datastax also provide the <code>DataType</code> class to make defining the column types easier, for example <code>text</code> matches to <code>TEXT</code> and <code>cint</code> matches to <code>INT</code>. As with the last time we use <code>SchemaBuilder</code>, once we are happy with the table design we need to <code>execute</code> it.

Onto the <code>MappingManager</code>, the snippet to create the bean is below.

[gist https://gist.github.com/lankydan/a860b854896e7553132589e86f831c06 /]

The <code>MappingManager</code> bean comes from the <code>cassandra-driver-mapping</code> dependency and will map a <code>ResultSet</code> to an entity (which we will look at later). For now we just need to create the bean. If we aren't happy with the default naming strategy of converting Java camel case to all lowercase with no separators in Cassandra we will need to set our own. To do this we can pass in a <code>DefaultNamingStrategy</code> to define the case that we are using within our Java classes and what we are using in Cassandra. Since in Java it is typical to use camel case we pass in <code>LOWER_CAMEL_CASE</code> and since I like to use snake case in Cassandra we can use <code>LOWER_SNAKE_CASE</code> (these are found in the <code>NamingConventions</code> class). The reference to lower specifies the case of the first character in a string, so <code>LOWER_CAMEL_CASE</code> represents <code>firstName</code> and <code>UPPER_CAMEL_CASE</code> represents <code>FirstName</code>. <code>DefaultPropertyMapper</code> comes with extra methods for more specific configuration but <code>MappingConfiguration</code> only has one job of taking in a <code>PropertyMapper</code> to be passed to a <code>MappingManager</code>.

The next thing we should look at is the entity that will be persisted to and retrieved from Cassandra, saving us the effort of manually setting values for inserts and converting results from reads. The Datastax driver provides us with a relatively simple way to do just that by using annotations to mark properties like the name of the table it is mapping to, which field matches to what Cassandra columns and which fields the primary key consists of.

[gist https://gist.github.com/lankydan/d174a8d4d0360a9cc48bed1f25424359 /]

This entity represents the <code>people_by_country</code> table as denoted by the <code>@Table</code>. I have put the CQL of the table below again for reference.

[gist https://gist.github.com/lankydan/f9b3d3ef67240f189cb2a4c1b0f8d156 /]

The <code>@Table</code> annotation must specify the name of the table the entity represents, it also comes with various other options depending on your requirements, such as <code>keyspace</code> if you don't want to use the default keyspace the <code>Session</code> bean is configured to use and <code>caseSensitiveTable</code> which is self explanatory.

What about the primary key? As touched on above, a primary key consists of a partition key that itself contains one or more columns and/or clustering columns(s). To match up to the Cassandra table defined above we added the <code>@PartitionKey</code> and <code>@ClusteringColumn</code> annotations to the required fields. Both of the annotations have one property, <code>value</code> which specifies the order which the column appears in the primary key. The default value is <code>0</code> which is why some annotations do not include a value.

The last requirements to get this entity to work are getters, setters and a default constructor so that the mapper can do it's thing. The default constructor can be private if you don't want anyone accessing it as the mapper uses reflection to retrieve it. You might not want to have setters on your entity since you would like the object to be immutable, unfortunately, there isn't anything you can really do about this and you'll just have to concede this fight. Although I personally think this is fine as you could (and maybe should) convert the entity into another object that can be passed around your application without any of the entity annotations and thus no knowledge of the database itself. The entity can then be left as mutable and the other object that you are passing around can work exactly as you wish.

One last thing I want to mention before we move on. Remember the <code>DefaultNamingConvention</code> we defined earlier? This means that our fields are being matched to the correct columns without any extra work in the entity. If you didn't do this or wanted to provide a different field name to your column name then you could use the <code>@Column</code> annotation and specify it there.

We nearly have all the components we need to build our example application. The penultimate component is creating a repository that will contain all the logic for persisting and reading data to and from Cassandra. We will make use of the <code>MappingManager</code> bean that we created earlier and the annotations that we put onto the entity to convert a <code>ResultSet</code> into an entity without needing to do anything else ourselves.

[gist https://gist.github.com/lankydan/e24225d3ca936ce8fb5848eddf9efb1c /]

By injecting the <code>MappingManager</code> in via the constructor and calling the <code>mapper</code> method for the <code>Person</code> class, we are returned with a <code>Mapper<Person></code> that will personally handle all of our mapping needs. We also need to retrieve the <code>Session</code> to be able to execute queries which is nicely contained within the <code>MappingManager</code> we injected.

For three of the queries we are directly relying on the mapper to interact with Cassandra but this only works for a single record. <code>get</code>, <code>save</code> and <code>delete</code> each work by accepting in the values that make up the <code>Person</code> entity's primary key and they must be entered in the correct order or you will experience unexpected results or exceptions will be thrown.

The other situations require a query to be executed before the mapper can be called to convert the returned <code>ResultSet</code> into an entity or collection of entities. I have made use of <code>QueryBuilder</code> to write queries and I have also chosen for this post to not write prepared statements. Although in most cases you should be using prepared statements I thought I would cover these in a separate post in the future, although they are similar enough and <code>QueryBuilder</code> can still be used so I am confident you could figure it out on your own if needed.

<code>QueryBuilder</code> provides static methods to create <code>select</code>, <code>insert</code>, <code>update</code> and <code>delete</code> statements which can then be chained together to (I know this sounds obvious) build the query. The <code>QueryBuilder</code> used here is also the same one that you can use in Spring Data Cassandra when you need to manually create your own queries and not rely on the inferred queries coming from the Cassandra repositories.

The final step to creating this little application, is actually running it. Since we are using Spring Boot we just add the standard <code>@SpringBootApplication</code> and run the class. I have done just that below, as well as using <code>CommandLineRunner</code> to execute the methods within the repository so we can check that they are doing what we expect.

[gist https://gist.github.com/lankydan/85780b17f207d143de04f0b5ec091910 /]

The <code>run</code> method contains some print lines so we can see whats happening, below is what they output.
<pre>Find all
Person{country='US', firstName='Alice', lastName='Cooper', id=e113b6c2-5041-4575-9b0b-a0726710e82d, age=45, profession='Engineer', salary=1000000}
Person{country='UK', firstName='Bob', lastName='Bobbington', id=d6af6b9a-341c-4023-acb5-8c22e0174da7, age=50, profession='Software Developer', salary=50000}
Person{country='UK', firstName='John', lastName='Doe', id=f7015e45-34d7-4f25-ab25-ca3727df7759, age=30, profession='Doctor', salary=100000}

Find one record
Person{country='UK', firstName='John', lastName='Doe', id=f7015e45-34d7-4f25-ab25-ca3727df7759, age=30, profession='Doctor', salary=100000}

Find all by country
Person{country='UK', firstName='Bob', lastName='Bobbington', id=d6af6b9a-341c-4023-acb5-8c22e0174da7, age=50, profession='Software Developer', salary=50000}
Person{country='UK', firstName='John', lastName='Doe', id=f7015e45-34d7-4f25-ab25-ca3727df7759, age=30, profession='Doctor', salary=100000}

Demonstrating updating a record
Person{country='UK', firstName='John', lastName='Doe', id=f7015e45-34d7-4f25-ab25-ca3727df7759, age=30, profession='Unemployed', salary=0}

Demonstrating deleting a record
null
</pre>
We can see that <code>findAll</code> has returned all records and <code>find</code> has only retrieved the record that matches the input primary key values. <code>findAllByCountry</code> has excluded Alice and only found the records from the UK. Calling <code>save</code> again on an existing record will update the record rather than inserting. Finally <code>delete</code> will delete the person's data from the database (like deleting facebook?!?!).

And thats a wrap.

I will try to write some follow up posts to this in the future as there are a few more interesting things that we can do with the Datastax driver that we haven't gone through in this post. What we have covered here should be enough to make your first steps in using the driver and start querying Cassandra from your application.

Before we go I would like to make a few comparisons between the Datastax driver and Spring Data Cassandra.

Support for creating tables is lacking in the Datastax driver (in my opinion) compared to Spring Data Cassandra. The fact that Spring Data is able to create your tables solely based on your entities removes all this extra effort to basically rewrite what you have already written. Obviously if you don't want to use entity annotations then the difference goes away as you will need to manually create the tables in both Datastax and Spring Data.

The way the entities are designed and the annotations used are also quite different. This point is tied closely to the previous point I made. Because Spring Data can create your tables for you, it has a greater need for more precise annotations that allow you to specify the design of your tables, such as the sorting order of clustering columns. This obviously can clutter up the class with a load of annotations which is normally frowned upon.

Spring Data also provides better support for standard queries such as <code>findAll</code> and the inserting of a collection of entities. Obviously this is not exactly the end of the world and implementing these will take very little effort but this pretty much sums up the main difference between the Datastax driver and Spring Data Cassandra.

Spring Data is just easier to use. I don't think there is really anything else to say on the subject. Since Spring Data Cassandra is built upon the Datastax driver it can obviously do everything the driver can and if anything that you require is missing, then you can just access the Datastax classes directly and do what you need. But the convenience that Spring Data provides shouldn't be looked over and I don't think I have even covered some of the more helpful parts that it provides since this post is only covering the basics. Don't even get me started on how much easier it is once you make use of Spring Boot's auto-configuration and the inferred queries that Cassandra repositories generate for you.

I should stop... This is turning into a rant.

In conclusion using the Datastax driver to connect and query a Cassandra database is relatively straight forward. Establish a connection to Cassandra, create the entities that you need and write the repositories that make use of the former, then you have everything that you need to get going. We also compared the Datastax driver to Spring Data Cassandra which pretty much comes down to, Datastax will do what you need but Spring Data makes it easier.

The code used in this post can be found on my <a href="https://github.com/lankydan/datastax-java-driver" target="_blank" rel="noopener">GitHub</a>.

If you found this post helpful and want to keep up to date with my latest posts, then you can follow me on twitter at <a href="https://twitter.com/LankyDanDev" target="_blank" rel="noopener">@LankyDanDev</a>.