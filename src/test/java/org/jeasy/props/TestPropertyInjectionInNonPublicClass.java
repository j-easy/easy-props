package org.jeasy.props;

import org.jeasy.props.annotations.SystemProperty;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjector;

public class TestPropertyInjectionInNonPublicClass {

	@Before
	public void setUp() {
		System.setProperty("name", "foo");
	}

	@Test
	public void testPropertyInjectionInPrivateClassWithoutSetter() {
		// given
		MyBean myBean = new MyBean();

		//when
		aNewPropertiesInjector().injectProperties(myBean);

		// then
		assertThat(myBean.getName()).isEqualTo("foo");
	}
	
	private static class MyBean {

		@SystemProperty("name")
		private String name;

		public String getName() {
			return name;
		}
	}
}
