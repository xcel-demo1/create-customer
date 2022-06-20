package bdd.cucumber;

import static com.ibm.integration.test.v1.Matchers.nodeCallCountIs;
import static com.ibm.integration.test.v1.Matchers.terminalPropagateCountIs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.IncludeEngines;

import com.ibm.integration.test.v1.NodeSpy;
import com.ibm.integration.test.v1.SpyObjectReference;
import com.ibm.integration.test.v1.TestMessageAssembly;
import com.ibm.integration.test.v1.TestSetup;
import com.ibm.integration.test.v1.exception.TestException;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.junit.platform.engine.Cucumber;
import sun.misc.Contended;

// Run the cucumber tests
@IncludeEngines("cucumber")
public class RunCucumberTest 
{
	// Should probably be using message assemblies instead of strings . . .
	
	// This is used as one example, using an MXML message file
	static String messageAssemblyName; 
	// This is used as another example, providing just the string for the input JSON
	static String jsonValueForDay;
	
	// This is used to hold the result so it can be checked
	static String returnResult;
	
	// Set up for the Sunday test, using an MXML message file
	@Given("today is Sunday")
	public void today_is_sunday() {
		messageAssemblyName = "Sunday";
		jsonValueForDay = null;
	}
	
	// Set up for the Friday test, using JSON data
	@Given("today is Friday")
	public void today_is_friday() {
		messageAssemblyName = null;
		jsonValueForDay = "Friday";
	}
	
	// Actually run the flow
	@When("I ask whether it's Friday yet")
	public void i_ask_whether_it_s_friday_yet() throws TestException 
	{
		// Define the SpyObjectReference
		SpyObjectReference nodeReference = new SpyObjectReference().application("FridayApplication")
				.messageFlow("MainFlow").node("Compute");

		// Initialise a NodeSpy
		NodeSpy nodeSpy = new NodeSpy(nodeReference);

		// Declare a new TestMessageAssembly object for the message being sent into the node
		TestMessageAssembly inputMessageAssembly = new TestMessageAssembly();

		if ( messageAssemblyName != null )
		{
			// Create a Message Assembly from the input data file; can represent any message
			// tree no matter how complex.
			try {
				String messageAssemblyPath = "/"+messageAssemblyName+".mxml";
				InputStream messageStream = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(messageAssemblyPath);
				if (messageStream == null) {
					throw new TestException("Unable to locate message assembly file: " + messageAssemblyPath);
				}
				inputMessageAssembly.buildFromRecordedMessageAssembly(messageStream);
			} catch (Exception ex) {
				throw new TestException("Failed to load input message", ex);
			}
		}
		else
		{
			// Directly create the JSON data; useful for small testcases.
            inputMessageAssembly.messagePath("JSON.Data.today").setValue(jsonValueForDay);
		}

		// Call the message flow node with the Message Assembly
		nodeSpy.evaluate(inputMessageAssembly, true, "in");

		// Assert the number of times that the node is called
		assertThat(nodeSpy, nodeCallCountIs(1));

		// Assert the terminal propagate count for the message
		assertThat(nodeSpy, terminalPropagateCountIs("out", 1));

        /* Compare Output Message 1 at output terminal out */
        TestMessageAssembly actualMessageAssembly = nodeSpy.propagatedMessageAssembly("out", 1);

        // Read the result; this will throw if the value does not exist
        returnResult = actualMessageAssembly.messagePath("JSON.Data.result").getStringValue();	

		// Ensure any mocks created by a test are cleared after the test runs; we could 
        // leave them around in this case, as the tests all do the same sort of thing, but
        // clearing everything leaves us with a clean state for every test and avoids 
        // issues in the future.
        //
		// Note that this makes it hard to share message assemblies between steps, and would
        // need to be changed if message assemblies (as opposed to MXML files) are used.
		TestSetup.restoreAllMocks();
	}
	
	// Validate the result
	@Then("I should be told {string}")
	public void i_should_be_told(String string) 
	{
		assertEquals(string, returnResult);
	}

	@AfterEach
	public void cleanupTest() throws TestException {
		// Make absolutely sure everything is cleaned up (which it should already be).
		TestSetup.restoreAllMocks();
	}

}
