import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as apigateway from 'aws-cdk-lib/aws-apigateway';
import * as dynamodb from 'aws-cdk-lib/aws-dynamodb';
import * as path from 'path';

export class ProductServiceStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        const productsTable = dynamodb.Table.fromTableName(this, 'ProductsTable', 'products');
        const stocksTable = dynamodb.Table.fromTableName(this, 'StocksTable', 'stocks');

        const jarPath = path.join(__dirname, '../../product_service/target/product-service-1.0-SNAPSHOT.jar');

        const commonEnv = {
            PRODUCTS_TABLE: productsTable.tableName,
            STOCKS_TABLE: stocksTable.tableName,
        };

        const getProductsList = new lambda.Function(this, 'getProductsList', {
            runtime: lambda.Runtime.JAVA_17,
            handler: 'com.shop.product.handler.GetProductsListHandler::handleRequest',
            code: lambda.Code.fromAsset(jarPath),
            memorySize: 512,
            timeout: cdk.Duration.seconds(30),
            environment: commonEnv,
        });

        const getProductsById = new lambda.Function(this, 'getProductsById', {
            runtime: lambda.Runtime.JAVA_17,
            handler: 'com.shop.product.handler.GetProductsByIdHandler::handleRequest',
            code: lambda.Code.fromAsset(jarPath),
            memorySize: 512,
            timeout: cdk.Duration.seconds(30),
            environment: commonEnv,
        });

        const createProduct = new lambda.Function(this, 'createProduct', {
            runtime: lambda.Runtime.JAVA_17,
            handler: 'com.shop.product.handler.CreateProductHandler::handleRequest',
            code: lambda.Code.fromAsset(jarPath),
            memorySize: 512,
            timeout: cdk.Duration.seconds(30),
            environment: commonEnv,
        });

        productsTable.grantReadData(getProductsList);
        stocksTable.grantReadData(getProductsList);

        productsTable.grantReadData(getProductsById);
        stocksTable.grantReadData(getProductsById);

        productsTable.grantWriteData(createProduct);
        stocksTable.grantWriteData(createProduct);

        const api = new apigateway.RestApi(this, 'ProductServiceApi', {
            restApiName: 'Product Service',
            defaultCorsPreflightOptions: {
                allowOrigins: apigateway.Cors.ALL_ORIGINS,
                allowMethods: apigateway.Cors.ALL_METHODS,
            },
        });

        const productsResource = api.root.addResource('products');
        productsResource.addMethod('GET',  new apigateway.LambdaIntegration(getProductsList));
        productsResource.addMethod('POST', new apigateway.LambdaIntegration(createProduct));

        const productByIdResource = productsResource.addResource('{productId}');
        productByIdResource.addMethod('GET', new apigateway.LambdaIntegration(getProductsById));

        new cdk.CfnOutput(this, 'ApiUrl', {
            value: api.url,
            description: 'Product Service API URL',
        });
    }
}
