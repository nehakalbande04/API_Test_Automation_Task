Feature: Verify API failure response for invalid endpoint

  Scenario: Verify failure status code and error response structure
    Given I send a GET request to "https://testapi.io/api/RMSTest/ibltest/2023-09-11"
    Then the invalid response status code should be 404
    And the error object should have the properties "details" and "http_response_code"