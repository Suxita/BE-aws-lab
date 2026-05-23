
const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");
const { DynamoDBDocumentClient, TransactWriteCommand } = require("@aws-sdk/lib-dynamodb");

const REGION = process.env.AWS_REGION || "eu-west-1";
const PRODUCTS_TABLE = process.env.PRODUCTS_TABLE || "products";
const STOCKS_TABLE = process.env.STOCKS_TABLE || "stocks";

const client = new DynamoDBClient({ region: REGION });
const docClient = DynamoDBDocumentClient.from(client);


const products = [
    {
        id: "7567ec4b-b10c-48c5-9345-fc73c48a80aa",
        title: "The Lord of the Rings",
        description: "Epic fantasy novel by J.R.R. Tolkien",
        price: 29.99,
    },
    {
        id: "7567ec4b-b10c-48c5-9345-fc73c48a80ab",
        title: "Harry Potter and the Philosopher's Stone",
        description: "Fantasy novel by J.K. Rowling",
        price: 19.99,
    },
    {
        id: "7567ec4b-b10c-48c5-9345-fc73c48a80ac",
        title: "Clean Code",
        description: "A handbook of agile software craftsmanship by Robert C. Martin",
        price: 39.99,
    },
    {
        id: "7567ec4b-b10c-48c5-9345-fc73c48a80ad",
        title: "The Pragmatic Programmer",
        description: "From journeyman to master by David Thomas & Andrew Hunt",
        price: 44.99,
    },
    {
        id: "7567ec4b-b10c-48c5-9345-fc73c48a80ae",
        title: "Design Patterns",
        description: "Elements of reusable object-oriented software by GoF",
        price: 49.99,
    },
];

// Matching stocks — same ids
const stocks = [
    { product_id: "7567ec4b-b10c-48c5-9345-fc73c48a80aa", count: 10 },
    { product_id: "7567ec4b-b10c-48c5-9345-fc73c48a80ab", count: 25 },
    { product_id: "7567ec4b-b10c-48c5-9345-fc73c48a80ac", count: 5  },
    { product_id: "7567ec4b-b10c-48c5-9345-fc73c48a80ad", count: 8  },
    { product_id: "7567ec4b-b10c-48c5-9345-fc73c48a80ae", count: 3  },
];

async function fillTables() {
    console.log(`Filling tables in region: ${REGION}`);
    console.log(`  products table : ${PRODUCTS_TABLE}`);
    console.log(`  stocks table   : ${STOCKS_TABLE}`);
    console.log();

    for (let i = 0; i < products.length; i++) {
        const product = products[i];
        const stock   = stocks[i];

        const command = new TransactWriteCommand({
            TransactItems: [
                {
                    Put: {
                        TableName: PRODUCTS_TABLE,
                        Item: product,
                    },
                },
                {
                    Put: {
                        TableName: STOCKS_TABLE,
                        Item: stock,
                    },
                },
            ],
        });

        try {
            await docClient.send(command);
            console.log(` Inserted product "${product.title}" (id: ${product.id})`);
        } catch (err) {
            console.error(` Failed to insert "${product.title}":`, err.message);
            process.exit(1);
        }
    }

    console.log("\nDone! All products and stocks inserted successfully.");
}

fillTables();