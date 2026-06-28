Customer Rewards Program
---------------------------
 
A Retailer offers a rewards program to its customers, awarding points based on each recorded purchase.  
Customer receives 2 points for every dollar spent over $100 in each transaction
Customer receives 1 point for every dollar spent between $50 and $100 in each transaction.  
Ex) $120 purchase = 2x$20 + 1x$50 = 90 points
Given a record of every transaction during a three month period, calculate the reward points earned for each customer per month and total. 


Implementation Details:
--
Spring Boot Application is created with two APIs
Fetch All Customer Purchases for the last three months and calculate the reward points earned for each qualified Customer per month and total.
Fetch the given Customer Purchases for the last three months and calculate the reward points earned for each qualified Customer per month and total.


APIs Created:
--
GET /api/rewards

GET /api/rewards/{customerId}


Components Implemented:
--
RewardsServiceApplication (Main)
RewardsController 
RewardsService (Service), RewardPointsCalculator (Strategy)
Customer, Purchase, Reward (Entities)
CustomerRepository, PurchaseRepository, RewardRepository (JPA Repositories)
GlobalExceptionHandler, CustomerNotFoundException


Other Important Application Support Files:
--
data.sql
application.properties
logback-spring.xml


Application Flow:
--
A set of Purchases are loaded into in memory database with Customer details.
DB has Customers, Purchases, Rewards Tables
APIs in the Controller - Fetches and Calculates the Rewards Points - By calling RewardService
Service making Asynchronous calls for the below operations:
   - Service Fetches the data Entities through JPA Repositories, which provides the basic CRUD
   - Service Using a RewardCalculator(Strategy) to find the total points for each Customer
   - Service Returns the Rewards Summary to Controller
   - Service also Save the calculated Reward details in the DB(backed by @Transactional)


Other features implemented:
--
Controller making a non-blocking and asynchronous call to Service
Service performing all operations asynchronously Also controlling Transactions in Saga Style
Customer API which Fetches, Calculates RewardPoints for the given Customer and update the DB
Logger implemented
Custom Exception and Global Exception Handled
Test cases added to Test the Scenarios: Eligible & Non Eligible Customers, Reward Calculation Logic.
Configured with OpenAPI-Swagger and Tested with Visual representation
Tested with Postman



---------------------------------------
Project Scope and Architecture Overview
----------------------------------------

The Customer Rewards Program application is built as a non-blocking, asynchronous REST API using Spring Boot. It is designed to process retail transactions over a rolling three-month window, calculate tiered loyalty rewards, and instantly serve data back to the client. It handles database persistence tasks in the background using dedicated worker threads so that the client doesn't have to wait for database writes to complete.

Key Capabilities Covered:
--
Tiered Business Logic: A custom algorithm that calculates points using one-point and two-point multipliers based on specific transaction milestones.
Asynchronous Processing: Leverages CompletableFuture and @Async thread pools to detach API response times from heavy processing and database tasks.
Resilient Persistence: Uses automated transactional updates to ensure the database stays consistent during background report updates.
Robust Exception Handling: Catches multi-threaded background failures and converts them into structured JSON feedback for the client.

Technical Component Breakdown:
--
Controller Layer (RewardsController):
The controller acts as the entry gateway for incoming HTTP clients. It maps network requests directly to their respective service handlers. Instead of blocking the server thread while waiting for calculations, it returns a CompletableFuture immediately. It also uses the .exceptionally() method to catch asynchronous background errors, ensuring they flow smoothly into the global exception handler.

Service Layer (RewardsService):
--
The service acts as the multi-threaded calculation engine. Methods like getRewardsForCustomer are marked with @Async to run on independent threads. The service fetches the necessary raw transaction records from the database, establishes a dynamic mathematical cutoff date based on the rolling month configuration, and groups the transactions by month. Once the final report DTO is built, it immediately passes the data to the database save method and returns the results to the controller right away.

Core Strategy Layer (RewardCalculator):
--
This component encapsulates the core mathematical formula so that business logic does not clutter the service layers. It evaluates each transaction based on three specific conditions:
Any amount spent under $50 earns 0 points.
Any amount spent between $50 and $100 earns 1 point per dollar.
Any amount spent over $100 earns 2 points per dollar, plus the 50 points accumulated from the previous tier.

Database and Persistence Layer (Entities and Repositories):
--
This layer manages relational data mapping and data integrity using Spring Data JPA.
The Purchase entity and its repository handle raw transaction histories, utilizing optimized database queries to pull data selectively by customer ID.
The Reward entity and its repository act as an aggregated cache layer. It breaks down the total rewards into clear, monthly point columns for fast reporting later.
The @Transactional annotation is placed on background database saving tasks to prevent data corruption during batch updates.

Diagnostics and Guardrails (Logging and Exceptions):
--
SLF4J Logging: Tracks application execution across multiple levels. Info logs mark request entries, debug logs track internal data filtering steps, and error logs capture detailed exception stack traces.

GlobalExceptionHandler: 
Intercepts standard runtime failures alongside a custom CustomerNotFoundException. It overrides the default Spring Whitelabel Error Page, returning a predictable JSON payload that contains a timestamp, HTTP status, error type, and a specific error message.


