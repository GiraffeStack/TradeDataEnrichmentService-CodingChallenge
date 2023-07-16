# Trade Data Enrichment Service

The Trade Data Enrichment Service is a Java-based RESTful API designed to enhance trading data with product names derived from a static data file. It processes CSV files containing trade data, adds value by integrating product names, and returns the enriched data back to the user.

## Setup Guide

Follow these instructions to clone, build, and run the project in your local development environment.

### Pre-requisites

Ensure the following software is installed on your system before proceeding:

- Java JDK 11 or later
- Apache Maven

### Building the Project

Navigate to the root directory of the project via your terminal or command prompt and execute the following command to build the project:

```bash
mvn clean install
```

This command cleans any previous build, compiles the source code, runs the tests and packages the compiled code into a JAR file.

### Running the Application

After successfully building the project, you can run the service with the following command:

```bash
java -jar target/trade-enricher-1.0.0.jar
```

This command starts the server and the service is accessible at `http://localhost:8080`.

## API Reference

The service exposes a single API endpoint:

- `POST /api/v1/enrich`: Accepts a CSV file containing trade data and returns the enriched data.

Use the following cURL command as an example of how to call the API:

```bash
curl --request POST -F "file=@trade.csv" --header "Accept: text/csv" http://localhost:8080/api/v1/enrich
```

The service has also been deployed to Heroku and is accessible at `https://trade-enricher-96278b8a15a3.herokuapp.com/api/v1/enrich`. 
You can use the following cURL command to call the API on Heroku:

```bash
curl --request POST -F "file=@trade.csv" --header "Accept: text/csv" https://trade-enricher-96278b8a15a3.herokuapp.com/api/v1/enrich
```

Replace `trade.csv` with the path to your CSV file.

## JMeter Test Results (Against Heroku)
| Label | # Samples | Average | Median | 90% Line | 95% Line | 99% Line | Min | Max | Error % | Throughput | Received KB/sec | Sent KB/sec |
|-|-|-|-|-|-|-|-|-|-|-|-|-|  
| HTTP Request (V1 CSV with 5 data rows) | 500 | 41 | 29 | 72 | 98 | 129 | 20 | 175 | 0.000% | 16.61903 | 5.91 | 10.10 |
| HTTP Request (V1 CSV with 10,000 data rows) | 500 | 7896 | 7414 | 13433 | 16085 | 22438 | 283 | 39910 | 0.000% | 6.89617 | 2131.56 | 2010.50 |

### Analysis
- Two tests were executed - one with a small CSV data set and one with a larger 10,000 row CSV.
- The test with the smaller data set unsurprisingly had much faster response times - average of 41ms vs 7896ms for the larger test.
- Median response times tell a similar story - 29ms for small test vs 7414ms for the large CSV. The medians are lower than the averages, indicating some very long response times skewing the averages upwards.
- The 90th, 95th and 99th percentile response times show expected increasing response times as the percentiles get higher, especially for the larger data test. 
- The maximum response time shows the large CSV test had some very long max times up to 39 seconds. This indicates potential scalability issues with this much data.
- Error percentage was 0 for both tests, so errors don't seem to be an issue.
- Throughput (requests/second) was much higher for the smaller test, almost 3x higher than the large CSV test. This makes sense given the faster response times.

Overall, the test with the larger dataset highlighted some performance and scalability issues under load that would need to be addressed before going live. 
Further analysis would be required to determine the root cause of the performance issues and how to address them.

## Additional Notes
The service employs MultipartFile for file uploads due to its efficiency in memory management and other benefits over InputStream or String. If you would like to see a version of the code using InputStream or String for file uploads, please let me know. I am open to demonstrating how these different approaches can be implemented.

Your feedback and suggestions are always welcome. I believe in continuous learning and improvement, and I look forward to any insights you may have to offer.

## Future Enhancements
- Further improve performance and scalability of the service to handle larger datasets and higher load. This could involve optimisations for in-memory processing, caching, multi-node scaling, etc.
- Communicate to the user any values that could not be enriched by returning additional metadata in the response payload or exposing another API endpoint. This provides transparency into missing mappings.
- Expand on the exception handling to make it more robust. Custom runtime exceptions could be defined for specific failures to enable centralised handling.
- Capture additional metrics in the load tests to help further diagnose the root causes of any performance issues observed. Useful metrics could include CPU usage, memory utilisation, database connections, etc.
- Add Swagger documentation to fully document the API endpoints, models, parameters, sample requests/responses, and error codes. This improves discoverability and the overall developer experience.
- I was reluctant to add abstractions such as Trade Formatter and CSV Processor as I felt they were unnecessary for the current scope of the project. However, if the scope were to expand, these abstractions would be useful for improving the maintainability of the codebase.