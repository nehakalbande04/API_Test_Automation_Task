Feature: Verify API success response status and performance

  Scenario: Verify success status code and response time
    Given I send a GET request to "https://testapi.io/api/RMSTest/ibltest"
    Then the response status code should be 200
    And the response time should be less than 1000 milliseconds

