1. Introduction
The goal of this project was to build a highly scalable Java REST service capable of processing at least 10K requests per second. The service provides an endpoint /api/verve/accept that accepts:
An integer id as a mandatory query parameter.
An optional string HTTP endpoint query parameter.

2. System Design
2.1. Request Processing
The service processes incoming requests concurrently, tracking unique request id values using a thread-safe ConcurrentHashMap backed Set.
The id is checked for uniqueness, and if it's new, it is added to the set.
If an endpoint is provided, an HTTP GET request is sent to the provided endpoint with the count of unique id values as a query parameter.
2.2. Concurrency and Scalability
The ConcurrentHashMap ensures thread safety, making the application capable of handling a high volume of requests concurrently without data corruption or race conditions.
The Set stores the unique id values, and it is cleared every minute to reset the count for the next time window.
2.3. Logging and Monitoring
Every minute, the system logs the count of unique id values.
The logs contain the count of requests processed in the last minute and could be extended to send these logs to a distributed streaming service such as Kafka or AWS Kinesis in the future.

3. Extensions
3.1. Extension 1: HTTP POST Requests
In this extension, instead of firing a GET request, the service would fire an HTTP POST request to the provided endpoint. The body of the POST request would include the count of unique requests, and the content structure could be freely defined.

3.2. Extension 2: Deduplication Behind Load Balancers
To ensure deduplication of id values when the service is behind a load balancer (with multiple instances of the application), a distributed cache or database (e.g., Redis) would be used. This global data store would ensure that all instances of the service are aware of the id values across the entire system.

3.3. Extension 3: Distributed Streaming Service
Instead of writing the unique request count to a local log file, the count could be sent to a distributed streaming service. Kafka or a similar service could be used to send the count to downstream systems for processing, real-time monitoring, or analytics.