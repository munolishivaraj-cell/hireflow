package com.shivaraj.hireflow.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Lets {@code spring.datasource.url} be the "Service URI" that cloud consoles such as Aiven show, for example
 * {@code mysql://avnadmin:secret@host:12345/defaultdb?ssl-mode=REQUIRED}. That form is rewritten to the JDBC
 * URL Connector/J expects, and the user and password embedded in it become the datasource credentials. A
 * normal {@code jdbc:mysql://} URL is left alone apart from the same {@code ssl-mode} fix.
 */
public class MySqlServiceUriEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	static final String PROPERTY_SOURCE_NAME = "mysqlServiceUri";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String url;
		try {
			url = environment.getProperty("spring.datasource.url");
		}
		catch (IllegalArgumentException unresolvedPlaceholder) {
			return; // e.g. DB_URL not set; let the normal datasource error explain it
		}
		Map<String, Object> overrides = rewrite(url);
		if (!overrides.isEmpty()) {
			environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, overrides));
		}
	}

	static Map<String, Object> rewrite(String url) {
		Map<String, Object> overrides = new LinkedHashMap<>();
		if (url == null || url.isBlank()) {
			return overrides;
		}
		String trimmed = url.trim();
		if (trimmed.startsWith("mysql://")) {
			URI uri = URI.create(trimmed);
			String query = uri.getRawQuery() == null ? "" : "?" + fixSslMode(uri.getRawQuery());
			int port = uri.getPort() == -1 ? 3306 : uri.getPort();
			overrides.put("spring.datasource.url", "jdbc:mysql://" + uri.getHost() + ":" + port + uri.getRawPath() + query);
			String userInfo = uri.getRawUserInfo();
			if (userInfo != null) {
				int colon = userInfo.indexOf(':');
				overrides.put("spring.datasource.username", decode(colon < 0 ? userInfo : userInfo.substring(0, colon)));
				if (colon >= 0) {
					overrides.put("spring.datasource.password", decode(userInfo.substring(colon + 1)));
				}
			}
		}
		else if (trimmed.startsWith("jdbc:mysql://") && (trimmed.contains("ssl-mode=") || !trimmed.equals(url))) {
			overrides.put("spring.datasource.url", fixSslMode(trimmed));
		}
		return overrides;
	}

	/** Aiven and the mysql CLI say {@code ssl-mode}; Connector/J calls the same option {@code sslMode}. */
	private static String fixSslMode(String text) {
		return text.replace("ssl-mode=", "sslMode=");
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	@Override
	public int getOrder() {
		// After config data (application-*.yml) has been loaded, so profile-specific values are visible
		return Ordered.LOWEST_PRECEDENCE;
	}
}
