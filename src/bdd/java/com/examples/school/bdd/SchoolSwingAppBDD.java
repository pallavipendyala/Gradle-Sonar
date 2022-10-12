package com.examples.school.bdd;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.junit.runner.RunWith;

import io.cucumber.java.Before;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/bdd/resources", monochrome = true)
public class SchoolSwingAppBDD {

	//@Before
	//public static void setUpOnce() {
	//	FailOnThreadViolationRepaintManager.install();
	//}
}
