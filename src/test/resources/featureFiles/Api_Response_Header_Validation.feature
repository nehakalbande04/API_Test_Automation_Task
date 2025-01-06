Feature: Verify Date header in response

  Scenario: Verify "Date" value in response headers
    Given I send a GET request to "https://testapi.io/api/RMSTest/ibltest"
    Then the "Date" value should be present and valid in the response headers
