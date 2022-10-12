package com.examples.school.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.examples.school.controller.SchoolController;

import com.examples.school.model.Student;

/**
 * Utilizziamo AssertJ Swing Runner poiché ci consente di creare screenshots in caso di fallimento.
 * 
 * Estendiamo con la classe base di AssertJ Swing, la quale esegue la maggior parte dei più comuni setup.
 */

@RunWith(GUITestRunner.class)
public class StudentSwingViewTest extends AssertJSwingJUnitTestCase {

	private FrameFixture window;

	private StudentSwingView studentSwingView;

	@Mock
	private SchoolController schoolController;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			studentSwingView = new StudentSwingView();
			studentSwingView.setSchoolController(schoolController);
			return studentSwingView;
		});
		// The robot simulates user input on a Swing Component
		window = new FrameFixture(robot(), studentSwingView);
		window.show(); // shows the frame to test
	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}

	/**
	 * With @GUITest you tell AssertJ Swing to take a screenshot of the desktop when a JUnit GUI
	   test fails. Screenshots of failed tests will be saved in the directory failed-gui-tests (in the
	   directory where tests are executed).
	 */
	@Test @GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("id"));
		window.textBox("idTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("name"));
		window.textBox("nameTextBox").requireEnabled();
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
		window.list("studentList");
		window.button(JButtonMatcher.withText("Delete Selected")).requireDisabled();
		/**
		 * Using a single space for labels that can be empty is a trick to make sure 
		 * that the space for the label is always reserved from the very beginning of the
		 * application.
		 */
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testWhenIdAndNameAreNonEmptyThenAddButtonShouldBeEnabled() {
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Add")).requireEnabled();
	}

	@Test
	public void testWhenEitherIdOrNameAreBlankThenAddButtonShouldBeDisabled() {
		JTextComponentFixture idTextBox = window.textBox("idTextBox");
		JTextComponentFixture nameTextBox = window.textBox("nameTextBox");

		idTextBox.enterText("1");
		nameTextBox.enterText(" ");
		window.button(JButtonMatcher.withText("Add")).requireDisabled();

		idTextBox.setText("");
		nameTextBox.setText("");

		idTextBox.enterText(" ");
		nameTextBox.enterText("test");
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
	}
	
	/**
	 * Vogliamo che il bottone Delete Selected sia abilitato solo quando un elemento
	 * è selezionato dalla lista. Per scrivere questo test non dobbiamo utilizzere i 
	 * metodi della view che aggiungono elementi alla lista.
	 */
	
	/**
	 * AssertJ Swing controlla che tutte le operazioni sulle componenti Swing siano
	 * eseguite sullo speciale Event Dispatch Thread (EDT), se aggiungiamo un elemento
	 * al modello della lista lo andiamo a violare.
	 * 
	 * La GuiActionRunner.execute esegue una lambda nell'EDT, osserviamo che questo metodo
	 * attende fino a quando la lambda non termina la sua esecuzione. Questo è utile siccome
	 * non dobbiamo scrivere manualmente le operazioni di sincronizzazione.
	 */

	@Test
	public void testDeleteButtonShouldBeEnabledOnlyWhenAStudentIsSelected() {
		GuiActionRunner.execute(() -> studentSwingView.getListStudentsModel().addElement(new Student("1", "test")));
		window.list("studentList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Selected"));
		deleteButton.requireEnabled();
		window.list("studentList").clearSelection();
		deleteButton.requireDisabled();
	}

	@Test
	public void testsShowAllStudentsShouldAddStudentDescriptionsToTheList() {
		Student student1 = new Student("1", "test1");
		Student student2 = new Student("2", "test2");
		GuiActionRunner.execute(
			() -> studentSwingView.showAllStudents(
					Arrays.asList(student1, student2))
		);
		String[] listContents = window.list().contents();
		assertThat(listContents)
			.containsExactly("1 - test1", "2 - test2");
	}

	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		Student student = new Student("1", "test1");
		GuiActionRunner.execute(
			() -> studentSwingView.showError("error message", student)
		);
		window.label("errorMessageLabel")
			.requireText("error message: 1 - test1");
	}

	@Test
	public void testStudentAddedShouldAddTheStudentToTheListAndResetTheErrorLabel() {
		Student student = new Student("1", "test1");
		GuiActionRunner.execute(
				() ->
				studentSwingView.studentAdded(new Student("1", "test1"))
				);
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly("1 - test1");
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testStudentRemovedShouldRemoveTheStudentFromTheListAndResetTheErrorLabel() {
		// setup
		Student student1 = new Student("1", "test1");
		Student student2 = new Student("2", "test2");
		GuiActionRunner.execute(
			() -> {
				DefaultListModel<Student> listStudentsModel = studentSwingView.getListStudentsModel();
				listStudentsModel.addElement(student1);
				listStudentsModel.addElement(student2);
			}
		);
		// execute
		GuiActionRunner.execute(
			() ->
			studentSwingView.studentRemoved(new Student("1", "test1"))
		);
		// verify
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly("2 - test2");
		window.label("errorMessageLabel").requireText(" ");
	}
	
	/**
	 * I seguenti test verificano che quando si effettua l'aggiunta o la cancellazione
	 * di uno studente, venga invocato il metodo opportuno dell'oggetto SchoolController
	 * falsificando la sua implementazione. Questo perchè gli unit test devono essere
	 * isolati e quindi al momento della loro creazione non sappiamo se la classe
	 * SchoolController è già stata implementata.
	 */

	@Test
	public void testAddButtonShouldDelegateToSchoolControllerNewStudent() {
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Add")).click();
		verify(schoolController).newStudent(new Student("1", "test"));
	}

	@Test
	public void testDeleteButtonShouldDelegateToSchoolControllerDeleteStudent() {
		Student student1 = new Student("1", "test1");
		Student student2 = new Student("2", "test2");
		GuiActionRunner.execute(
			() -> {
				DefaultListModel<Student> listStudentsModel = studentSwingView.getListStudentsModel();
				listStudentsModel.addElement(student1);
				listStudentsModel.addElement(student2);
			}
		);
		window.list("studentList").selectItem(1);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		verify(schoolController).deleteStudent(student2);
	}
	
	@Test
	public void testShowErrorStudentNotFound() {
		Student student1 = new Student("1", "test1");
		Student student2 = new Student("2", "test2");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Student> listStudentsModel = studentSwingView.getListStudentsModel();
			listStudentsModel.addElement(student1);
			listStudentsModel.addElement(student2);
		});
		GuiActionRunner.execute(
				() -> studentSwingView.showErrorStudentNotFound("error message", student1)
		);
		window.label("errorMessageLabel").requireText("error message: 1 - test1");
		// new assertion
		assertThat(window.list().contents()).containsExactly("2 - test2");
	}
}
