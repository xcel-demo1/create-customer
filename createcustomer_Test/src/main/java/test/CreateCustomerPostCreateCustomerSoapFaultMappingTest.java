package test;

import static com.ibm.integration.test.v1.Matchers.nodeCallCountIs;
import static com.ibm.integration.test.v1.Matchers.terminalPropagateCountIs;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.ibm.integration.test.v1.NodeSpy;
import com.ibm.integration.test.v1.SpyObjectReference;
import com.ibm.integration.test.v1.TestMessageAssembly;
import com.ibm.integration.test.v1.TestSetup;
import com.ibm.integration.test.v1.exception.TestException;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;


public class CreateCustomerPostCreateCustomerSoapFaultMappingTest {
	
	@AfterEach
	public void cleanupTest() throws TestException {
		// Ensure any mocks created by a test are cleared after the test runs 
		TestSetup.restoreAllMocks();
	}

	@Test
	public void testInputJsonToSoapPayload() throws TestException {

		
		// Define the SpyObjectReference
		SpyObjectReference nodeReference = new SpyObjectReference().application("createcustomer")
				.messageFlow("gen.createcustomer").subflowNode("postCreatecustomer (Implementation)")
				.node("fault_mapping");

		// Initialise a NodeSpy
		NodeSpy nodeSpy = new NodeSpy(nodeReference);

		// Declare a new TestMessageAssembly object for the message being sent into the node
		TestMessageAssembly inputMessageAssembly = new TestMessageAssembly();

		// Create a Message Assembly from the input data file
		try {
			String inputResourcePath = "/postCreatecustomer_fault_mapping_input_data.xml";
			InputStream resourceStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(inputResourcePath);
			if (resourceStream == null) {
				throw new TestException("Unable to locate resource: " + inputResourcePath);
			}
			inputMessageAssembly.buildXMLMessage(resourceStream);
		} catch (Exception ex) {
			throw new TestException("Failed to load input message", ex);
		}

		// Call the message flow node with the Message Assembly
		nodeSpy.evaluate(inputMessageAssembly, true, "in");

		// Assert the number of times that the node is called
		assertThat(nodeSpy, nodeCallCountIs(1));

		// Assert the terminal propagate count for the message
		assertThat(nodeSpy, terminalPropagateCountIs("out", 1));

		// Compare Output Message 1 at output terminal out

		try {

			TestMessageAssembly actualMessageAssembly = null;
			String actualOutputData = null;
			String expectedOutputData = null;

			// Get the TestMessageAssembly object for the actual propagated message
			actualMessageAssembly = nodeSpy.propagatedMessageAssembly("out", 1);

			// Assert output message body data
			// Get the string containing the actual data that was propagated from the node
			actualOutputData = actualMessageAssembly.getMessageBodyAsString();

			// Create an InputStream from the expected resource
			String expectedResourcePath = "/postCreatecustomer_fault_mapping_output_data.json";
			InputStream resourceStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(expectedResourcePath);
			if (resourceStream == null) {
				throw new TestException("Unable to locate resource: " + expectedResourcePath);
			}
			byte[] buffer = new byte[resourceStream.available()];
			resourceStream.read(buffer);

			// Get the expected output data using output data file
			expectedOutputData = new String(buffer, StandardCharsets.UTF_8);

			assertJsonEquals(expectedOutputData, actualOutputData);

		} catch (Exception ex) {
			throw new TestException("Failed to compare with expected message", ex);
		}

	}
}
