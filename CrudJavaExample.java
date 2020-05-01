package com.amazonaws.samples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

public class CrudJavaExample {

	static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
	static DynamoDB dynamoDB = new DynamoDB(client);
	static String tableName = "ProductCatalog";
	static DynamoDBMapper mapper = new DynamoDBMapper(client);

	public static void main(String args[]) {

		listTables();
	}

	public static void createTable() {

		List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("Id").withAttributeType("N"));

		List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
		keySchema.add(new KeySchemaElement().withAttributeName("Id").withKeyType(KeyType.HASH));

		CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
				.withKeySchema(keySchema).withAttributeDefinitions(attributeDefinitions).withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(5L).withWriteCapacityUnits(6L));

		Table table = dynamoDB.createTable(createTableRequest);

		try {
			table.waitForActive();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void describeTable() {

		TableDescription tableDescription = dynamoDB.getTable(tableName).describe();

		System.out.printf("%s: %s \t ReadCapacityUnits: %d \t WriteCapacityUnits: %d",
				tableDescription.getTableStatus(), tableDescription.getTableName(),
				tableDescription.getProvisionedThroughput().getReadCapacityUnits(),
				tableDescription.getProvisionedThroughput().getWriteCapacityUnits());
	}

	public static void saveitem() {

		CatalogItem item = new CatalogItem();
		item.setId(102);
		item.setTitle("Dinesh");
		item.setISBN("222-2222222222");
		item.setBookAuthors(new HashSet<String>(Arrays.asList("Author 1", "Author 2")));
		item.setSomeProp("Test");

		mapper.save(item);

	}

	public static void getItem() {

		CatalogItem partitionKey = new CatalogItem();

		partitionKey.setId(102);
		DynamoDBQueryExpression<CatalogItem> queryExpression = new DynamoDBQueryExpression<CatalogItem>()
				.withHashKeyValues(partitionKey);

		List<CatalogItem> itemList = mapper.query(CatalogItem.class, queryExpression);

		itemList.forEach(item -> {
			System.out.println(item.getTitle());
			System.out.println(item.getBookAuthors());
		});
	}

	public static void deleteTable() {

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		DynamoDB dynamoDB = new DynamoDB(client);

		Table table = dynamoDB.getTable("ProductCatalog");

		table.delete();

		try {
			table.waitForDelete();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void listTables() {

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		DynamoDB dynamoDB = new DynamoDB(client);

		TableCollection<ListTablesResult> tables = dynamoDB.listTables();
		Iterator<Table> iterator = tables.iterator();

		while (iterator.hasNext()) {
			Table table = iterator.next();
			System.out.println(table.getTableName());
		}
	}

}

@DynamoDBTable(tableName = "ProductCatalog")
class CatalogItem {

	private Integer id;
	private String title;
	private String ISBN;
	private Set<String> bookAuthors;
	private String someProp;

	@DynamoDBHashKey(attributeName = "Id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@DynamoDBAttribute(attributeName = "Title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@DynamoDBAttribute(attributeName = "ISBN")
	public String getISBN() {
		return ISBN;
	}

	public void setISBN(String ISBN) {
		this.ISBN = ISBN;
	}

	@DynamoDBAttribute(attributeName = "Authors")
	public Set<String> getBookAuthors() {
		return bookAuthors;
	}

	public void setBookAuthors(Set<String> bookAuthors) {
		this.bookAuthors = bookAuthors;
	}

	@DynamoDBIgnore
	public String getSomeProp() {
		return someProp;
	}

	public void setSomeProp(String someProp) {
		this.someProp = someProp;
	}
}
