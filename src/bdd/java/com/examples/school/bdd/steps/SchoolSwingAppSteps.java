package com.examples.school.bdd.steps;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.bson.Document;

import com.mongodb.MongoClient;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SchoolSwingAppSteps {
	
	private static final String DB_NAME = "test-db";
	private static final String COLLECTION_NAME = "test-collection";
	
	private FrameFixture window;
	
	private MongoClient mongoClient;
	
	@Before
	public void setUp() {
		mongoClient = new MongoClient();
		// always start with an empty database
		mongoClient.getDatabase(DB_NAME).drop();
	}
	
	@After
	public void tearDown() {
		mongoClient.close();
		if (window != null)
			window.cleanUp();
	}
	
	@When("The Student View is shown")
	public void the_Student_View_is_shown() {
		// start the Swing application
		application("com.examples.school.app.swing.SchoolSwingApp")
			.withArgs(
					"--db-name=" + DB_NAME,
					"--db-collection=" + COLLECTION_NAME
					)
			.start();
			// get a reference of its JFrame
			window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
				@Override
				protected boolean isMatching(JFrame frame) {
					return "Student View".equals(frame.getTitle()) && frame.isShowing();
				}
			}).using(BasicRobot.robotWithCurrentAwtHierarchy());
	}
	
	@Then("The list contains elements with the following values")
	public void the_list_contains_elements_with_the_following_values(List<List<String>> values) {
		values.forEach(
				v -> assertThat(window.list().contents())
						.anySatisfy(e -> assertThat(e).contains(v.get(0), v.get(1)))
		);
	}
	
	@When("The user enters the following values in the text fields")
	public void the_user_enters_the_following_values_in_the_text_fields(List<Map<String, String>> values) {
		values
			.stream() // each element is a Map<String,String>
			.flatMap(m -> m.entrySet().stream())
			.forEach(
					e -> window
							.textBox(e.getKey() + "TextBox")
							.enterText(e.getValue())
			);
	}
	
	@When("The user clicks the {string} button")
	public void the_user_clicks_the_button(String buttonText) {
		window.button(JButtonMatcher.withText(buttonText)).click();
	}
	
	@Then("An error is shown containing the following values")
	public void an_error_is_shown_containing_the_following_values(List<List<String>> values) {
		assertThat(window.label("errorMessageLabel").text()).contains(values.get(0));
	}
	
	
	
	
	
	@Given("The user provides student data in the text fields")
	public void the_user_provides_student_data_in_the_text_fields() {
		window.textBox("idTextBox").enterText("10");
		window.textBox("nameTextBox").enterText("new student");
	}

	@Then("The list contains the new student")
	public void the_list_contains_the_new_student() {
		assertThat(window.list().contents()).anySatisfy(e -> assertThat(e).contains("10", "new student"));
	}
	
	@Given("The user provides student data in the text fields, specifying an existing id")
	public void the_user_provides_student_data_in_the_text_fields_specifying_an_existing_id() {
			window.textBox("idTextBox").enterText(DatabaseSteps.STUDENT_FIXTURE_1_ID);
			window.textBox("nameTextBox").enterText("new student");
	}
	
	@Then("An error is shown containing the name of the existing student")
	public void an_error_is_shown_containing_the_name_of_the_existing_student() {
		assertThat(window.label("errorMessageLabel").text()).contains(DatabaseSteps.STUDENT_FIXTURE_1_NAME);
	}
	
	@Given("The user selects a student from the list")
	public void the_user_selects_a_student_from_the_list() {
		window.list("studentList")
			.selectItem(Pattern.compile(".*" + DatabaseSteps.STUDENT_FIXTURE_1_NAME + ".*"));
	}
	
	@Then("The student is removed from the list")
	public void the_student_is_removed_from_the_list() {
		assertThat(window.list().contents())
			.noneMatch(e -> e.contains(DatabaseSteps.STUDENT_FIXTURE_1_NAME));
	}
	
	@Then("An error is shown containing the name of the selected student")
	public void an_error_is_shown_containing_the_name_of_the_selected_student() {
		assertThat(window.label("errorMessageLabel").text()).contains(DatabaseSteps.STUDENT_FIXTURE_1_NAME);
	}
}
