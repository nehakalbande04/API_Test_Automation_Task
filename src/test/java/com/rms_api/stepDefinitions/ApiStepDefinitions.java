package com.rms_api.stepDefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class ApiStepDefinitions {
    private Response response;

    /* Implementation of Scenario-1 */

    @Given("I send a GET request to {string}")
    public void send_api_request(String endpoint) {
        try {
            response = RestAssured.get(endpoint);
            assertNotNull(response, "Response is null");
            assertNotNull(response.getBody(), "Response body is null");
        } catch (Exception e) {
            fail("Failed to send GET request to " + endpoint + ": " + e.getMessage());
        }
    }

    @Then("the response status code should be {int}")
    public void validate_status_code(int statusCode) {
        int responseCode = response.getStatusCode();
        assertEquals(statusCode, responseCode, "Unexpected status code");
        System.out.println("Status Code: " + responseCode);
    }

    @And("the response time should be less than {int} milliseconds")
    public void validate_response_time_less_than_1000_milliseconds(int time) {
        long responseTime = response.getTime();
        System.out.println("Response Time: " + responseTime + " ms");

        // Enhanced error message for the assertion failure
        assertTrue(responseTime < time,
                String.format("Test failed! Response time of %d ms exceeds the expected threshold of %d ms. " +
                                "Actual response time: %d ms",
                        responseTime, time, responseTime));
    }

    /* Implementation of Scenario-2 */

    @Then("the {string} field should never be null or empty for all items in the data array")
    public void validate_id_field_not_null(String field) {
        JsonPath jsonPath = response.jsonPath();
        List<String> fieldValues = jsonPath.getList("schedule.elements." + field);

        fieldValues.forEach(value -> {
            assertNotNull(value, field + " field is null");
            assertFalse(value.isEmpty(), field + " field is empty");
        });
    }

    @And("the {string} field in {string} should always be {string} for all items")
    public void validate_type_field(String field, String parentField, String expectedValue) {
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> elements = jsonPath.getList("schedule.elements");

        for (Map<String, Object> element : elements) {
            Map<String, Object> episode = (Map<String, Object>) element.get(parentField);
            String type = (String) episode.get(field);
            System.out.println("Validating type field: " + field + " in episode for value: " + type);
            assertEquals(expectedValue, type, field + " field is not " + expectedValue);
            System.out.println("Successfully validated type field: " + field + " in episode with value: " + type);
        }
    }


    /* Implementation of Scenario-3 */

    @Then("the {string} field in {string} should not be null or empty for any schedule item")
    public void validate_title_field(String field, String parentField) {
        JsonPath jsonPath = response.jsonPath();

        List<Map<String, Object>> elements = jsonPath.getList("schedule.elements");

        for (Map<String, Object> element : elements) {
            Map<String, Object> episode = (Map<String, Object>) element.get(parentField);
            String title = (String) episode.get(field);
            System.out.println("Validating title field: " + field + " in episode for value: " + title);
            assertFalse(title.isEmpty(), "The title field should not be empty");
            System.out.println("Successfully validated title field: " + field + " in episode with value: " + title);
        }
    }

    /* Implementation of Scenario-4 */

    @Then("only one episode in the list has {string} field in {string} as true")
    public void validate_live_field(String field, String parentField) {
        JsonPath jsonPath = response.jsonPath();

        List<Map<String, Object>> elements = jsonPath.getList("schedule.elements");
        int liveTrueCount = 0;

        for (Map<String, Object> element : elements) {
            Map<String, Object> episode = (Map<String, Object>) element.get(parentField);
            Boolean isLive = (Boolean) episode.get(field);
            System.out.println("Validating live field: " + field + " in episode for value: " + isLive);
            if (isLive != null && isLive) {
                liveTrueCount++;
            }
        }

        assertEquals(1, liveTrueCount);
        System.out.println("Validation passed: Exactly one episode has 'live' field as true.");
    }

    /* Implementation of Scenario-5 */

    @Then("the {string} date should be before the {string} date for all schedule items")
    public void verify_transmission_start_before_end(String startField, String endField) {
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> elements = jsonPath.getList("schedule.elements");
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        elements.forEach(element -> {
            String startDateStr = (String) element.get(startField);
            String endDateStr = (String) element.get(endField);

            if (startDateStr == null || endDateStr == null) {
                throw new AssertionError("Date fields should not be null");
            }

            OffsetDateTime startDate = OffsetDateTime.parse(startDateStr, formatter);
            OffsetDateTime endDate = OffsetDateTime.parse(endDateStr, formatter);

            assertTrue(startDate.isBefore(endDate),
                    String.format("%s should be before %s: Start = %s, End = %s", startField, endField, startDate, endDate));
            System.out.println("Successfully validated that " + startField + " is before " + endField);
        });
    }

    /* Implementation of Scenario-6 */

    @Then("the {string} value should be present and valid in the response headers")
    public void verify_header_value_is_valid(String headerName) {

        if (!response.getHeaders().hasHeaderWithName(headerName)) {
            throw new AssertionError("The '" + headerName + "' header is missing from the response headers.");
        }

        String headerValue = response.getHeader(headerName);
        assertNotNull("The '" + headerName + "' header should not be null", headerValue);

        if ("Date".equals(headerName)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

            try {
                Date date = dateFormat.parse(headerValue);
                long currentTime = System.currentTimeMillis();
                assertTrue(date.getTime() <= currentTime, "The '" + headerName + "' header contains a date in the future");
                System.out.println("Validated '" + headerName + "' header: " + date);
            } catch (Exception e) {
                throw new AssertionError("The '" + headerName + "' header is not in a valid date format: " + headerValue, e);
            }
        } else {
            System.out.println("Validated '" + headerName + "' header with value: " + headerValue);
        }
    }

    /* Implementation of Scenario-7 */

    @Then("the invalid response status code should be {int}")
    public void validate_invalid_status_code(int statusCode) {
        int responseCode = response.getStatusCode();
        assertEquals(statusCode, responseCode, "Unexpected status code");
        System.out.println("Status Code: " + responseCode);
    }

    @And("the error object should have the properties {string} and {string}")
    public void validate_error_object_properties(String property_1, String property_2) {
        try {
            String responseBody = response.getBody().asString();
            JsonPath jsonPath = new JsonPath(responseBody);
            Object errorObject = jsonPath.get("error");

            assertNotNull(errorObject, "The error object is missing");
            assertNotNull(jsonPath.get("error." + property_1), "The error object is missing the property: " + property_1);
            assertNotNull(jsonPath.get("error." + property_2), "The error object is missing the property: " + property_2);

            String details = jsonPath.get("error." + property_1).toString();
            String responseCode = jsonPath.get("error." + property_2).toString();

            assertFalse(details.isEmpty(), "The 'details' property is empty");
            assertFalse(responseCode.isEmpty(), "The 'http_response_code' property is empty");

            System.out.println("Error Details: " + details);
            System.out.println("HTTP Response Code: " + responseCode);

        } catch (Exception e) {
            fail("Failed to validate error object properties: " + e.getMessage());
        }
    }
}