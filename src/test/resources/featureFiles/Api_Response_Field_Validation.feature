Feature: Verify API response field validations

  Scenario: Verify "id" and "type" field for all items in data array
    Given I send a GET request to "https://testapi.io/api/RMSTest/ibltest"
    Then the "id" field should never be null or empty for all items in the data array
    And the "type" field in "episode" should always be "episode" for all items

  Scenario: Verify "title" field in "episode" is never null or empty
    Given I send a GET request to "https://testapi.io/api/RMSTest/ibltest"
    Then the "title" field in "episode" should not be null or empty for any schedule item

  Scenario: Verify only one episode in the list has "live" field in "episode" as true
    Given I send a GET request to "https://testapi.io/api/RMSTest/ibltest"
    Then only one episode in the list has "live" field in "episode" as true

  Scenario: Verify "transmission_start" date value is before the "transmission_end" date
    Given I send a GET request to "https://testapi.io/api/RMSTest/ibltest"
    Then the "transmission_start" date should be before the "transmission_end" date for all schedule items
