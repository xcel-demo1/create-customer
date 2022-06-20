package test.bdd.wholeFlow;

import static com.ibm.integration.test.v1.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;

import org.junit.platform.suite.api.IncludeEngines;

import com.ibm.integration.test.v1.NodeSpy;
import com.ibm.integration.test.v1.SpyObjectReference;
import com.ibm.integration.test.v1.TestMessageAssembly;
import com.ibm.integration.test.v1.exception.TestException;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

// Run the cucumber tests
@IncludeEngines("cucumber")
public class WholeFlowApplication_DoSeveralThings_SetJSON_0001_Test {

	// This is used as one example, using an MXML message file
	static String messageAssemblyName; 

	// This is used for the reply so we can perform checks in the "Then" method.
	// Note that this could cause trouble if not cleaned up and multiple tests
	// are run from the same class.
	static TestMessageAssembly replyMessageAssembly;
	
	// Set up for the Sunday test, using an MXML message file
	@Given("a blank body")
	public void today_is_sunday() {
		messageAssemblyName = "EmptyInputMessage";
	}

	
	
	@When("I ask if header and body are correct")
	public void WholeFlowApplication_DoSeveralThings_SetJSON_TestCase_001() throws TestException {

		
		// Define the SpyObjectReference objects
		SpyObjectReference httpInputObjRef = new SpyObjectReference().application("WholeFlowApplication")
				.messageFlow("DoSeveralThings").node("HTTP Input");
		SpyObjectReference httpReplyObjRef = new SpyObjectReference().application("WholeFlowApplication")
				.messageFlow("DoSeveralThings").node("HTTP Reply");

		// Initialise NodeSpy objects
		NodeSpy httpInputSpy = new NodeSpy(httpInputObjRef);
		NodeSpy httpReplySpy = new NodeSpy(httpReplyObjRef);
		// Declare a new TestMessageAssembly object for the message being sent into the node
		TestMessageAssembly inputMessageAssembly = new TestMessageAssembly();
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
		
		// Configure the "in" terminal on the HTTP Reply node not to propagate.
		// If we don't do this, then the reply node will throw exceptions when it  
		// realises we haven't actually used the HTTP transport.
		httpReplySpy.setStopAtInputTerminal("in");

		// Now call propagate on the "out" terminal of the HTTP Input node.
		// This takes the place of an actual HTTP message: we simple hand the node
		// the message assembly and tell it to propagate that as if it came from an
		// actual client. This line is where the flow is actually run.
		httpInputSpy.propagate(inputMessageAssembly, "out");
		
		// Validate the results from the flow execution
        // We will now pick up the message that is propagated into the "HttpReply" node and validate it
	    replyMessageAssembly = httpReplySpy.receivedMessageAssembly("in", 1);
	}
	
	
	
	
	@Then("I should be told {string}")
	public void i_should_be_told(String string) throws TestException 
	{
		TestMessageAssembly expectedMessageAssembly = new TestMessageAssembly();
		try {
			String messageAssemblyPath = "/ExpectedReplyMessage.mxml";
			InputStream messageStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(messageAssemblyPath);
			if (messageStream == null) {
				throw new TestException("Unable to locate message assembly file: " + messageAssemblyPath);
			}
			expectedMessageAssembly.buildFromRecordedMessageAssembly(messageStream);
		} catch (Exception ex) {
			throw new TestException("Failed to load input message", ex);
		}
        // Assert that the actual message assembly matches the expected message assembly
	    assertThat(replyMessageAssembly, equalsMessage(expectedMessageAssembly).ignoreTimeStamps());
	}
}
