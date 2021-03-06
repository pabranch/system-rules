package org.junit.contrib.java.lang.system;

import static java.lang.System.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.contrib.java.lang.system.Executor.executeTestWithRule;
import static org.junit.contrib.java.lang.system.Statements.SUCCESSFUL_TEST;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;

import java.util.Map;
import java.util.Properties;

public class ClearSystemPropertiesTest {
	@Rule
	public final RestoreSystemProperties restore = new RestoreSystemProperties();

	@Test
	public void properties_are_cleared_at_start_of_test() {
		setProperty("first property", "dummy value");
		setProperty("second property", "another dummy value");
		ClearSystemProperties rule = new ClearSystemProperties(
			"first property", "second property");
		TestThatCapturesProperties test = new TestThatCapturesProperties();
		executeTestWithRule(test, rule);
		assertThat(test.propertiesAtStart)
			.doesNotContainKey("first property")
			.doesNotContainKey("second property");
	}

	@Test
	public void property_is_cleared_after_added_to_rule_within_test() {
		setProperty("property", "dummy value");
		ClearSystemProperties rule = new ClearSystemProperties();
		TestThatAddsProperty test = new TestThatAddsProperty("property", rule);
		executeTestWithRule(test, rule);
		assertThat(test.propertiesAfterAddingProperty)
			.doesNotContainKey("property");
	}

	@Test
	public void after_test_properties_have_the_same_values_as_before() {
		setProperty("first property", "dummy value");
		setProperty("second property", "another dummy value");
		setProperty("third property", "another dummy value");
		ClearSystemProperties rule = new ClearSystemProperties(
			"first property", "second property");
		executeTestWithRule(new TestThatAddsProperty("third property", rule), rule);
		assertThat(getProperties())
			.containsEntry("first property", "dummy value")
			.containsEntry("second property", "another dummy value")
			.containsEntry("third property", "another dummy value");
	}

	@Test
	public void property_that_is_not_present_does_not_cause_failure() {
		clearProperty("property");
		ClearSystemProperties rule = new ClearSystemProperties("property");
		executeTestWithRule(SUCCESSFUL_TEST, rule);
		//everything is fine if no exception is thrown
	}

	private class TestThatAddsProperty extends Statement {
		private final String property;
		private ClearSystemProperties rule;
		Map<Object, Object> propertiesAfterAddingProperty;

		TestThatAddsProperty(String property, ClearSystemProperties rule) {
			this.property = property;
			this.rule = rule;
		}

		@Override
		public void evaluate() throws Throwable {
			rule.clearProperty(property);
			propertiesAfterAddingProperty = new Properties(getProperties());
		}
	}
}
