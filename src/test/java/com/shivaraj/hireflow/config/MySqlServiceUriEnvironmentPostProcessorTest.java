package com.shivaraj.hireflow.config;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class MySqlServiceUriEnvironmentPostProcessorTest {

	@Test
	void aivenServiceUriBecomesJdbcUrlWithCredentials() {
		Map<String, Object> result = MySqlServiceUriEnvironmentPostProcessor
			.rewrite("mysql://avnadmin:p%40ss:w0rd@mysql-demo.aivencloud.com:12691/defaultdb?ssl-mode=REQUIRED");

		assertThat(result).containsEntry("spring.datasource.url",
				"jdbc:mysql://mysql-demo.aivencloud.com:12691/defaultdb?sslMode=REQUIRED")
			.containsEntry("spring.datasource.username", "avnadmin")
			.containsEntry("spring.datasource.password", "p@ss:w0rd");
	}

	@Test
	void serviceUriWithoutCredentialsKeepsConfiguredOnes() {
		assertThat(MySqlServiceUriEnvironmentPostProcessor.rewrite("mysql://db.example.com/app"))
			.containsOnly(Map.entry("spring.datasource.url", "jdbc:mysql://db.example.com:3306/app"));
	}

	@Test
	void jdbcUrlOnlyGetsSslModeFixedAndWhitespaceTrimmed() {
		assertThat(MySqlServiceUriEnvironmentPostProcessor.rewrite(" jdbc:mysql://h:1/d?ssl-mode=REQUIRED "))
			.containsOnly(Map.entry("spring.datasource.url", "jdbc:mysql://h:1/d?sslMode=REQUIRED"));
		assertThat(MySqlServiceUriEnvironmentPostProcessor.rewrite("jdbc:mysql://h:1/d?sslMode=REQUIRED")).isEmpty();
	}

	@Test
	void overridesWinOverTheConfiguredValues() {
		MockEnvironment env = new MockEnvironment()
			.withProperty("spring.datasource.url", "mysql://u:secret@h:2/d")
			.withProperty("spring.datasource.password", "from-env");

		new MySqlServiceUriEnvironmentPostProcessor().postProcessEnvironment(env, new SpringApplication());

		assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:mysql://h:2/d");
		assertThat(env.getProperty("spring.datasource.password")).isEqualTo("secret");
	}

	@Test
	void unresolvedPlaceholderIsLeftForTheDatasourceToReport() {
		MockEnvironment env = new MockEnvironment().withProperty("spring.datasource.url", "${DB_URL}");
		env.setIgnoreUnresolvableNestedPlaceholders(false);

		new MySqlServiceUriEnvironmentPostProcessor().postProcessEnvironment(env, new SpringApplication());

		assertThat(env.getPropertySources().contains(MySqlServiceUriEnvironmentPostProcessor.PROPERTY_SOURCE_NAME))
			.isFalse();
	}
}
