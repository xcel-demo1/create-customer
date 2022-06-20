Feature: Multiple nodes in a flow
  Example for testing multiple nodes in a flow

  Scenario: The flow has multiple nodes
    Given a blank body
    When I ask if header and body are correct 
    Then I should be told "yes"
