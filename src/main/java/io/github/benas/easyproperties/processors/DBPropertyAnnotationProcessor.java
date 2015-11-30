/*
 *   The MIT License
 *
 *    Copyright (c) 2016, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy
 *    of this software and associated documentation files (the "Software"), to deal
 *    in the Software without restriction, including without limitation the rights
 *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *    copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in
 *    all copies or substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *    THE SOFTWARE.
 */

package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.DBProperty;
import io.github.benas.easyproperties.api.AnnotationProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * An annotation processor that loads properties from a database.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class DBPropertyAnnotationProcessor extends AbstractAnnotationProcessor<DBProperty> {

    private static final Logger LOGGER = Logger.getLogger(DBPropertyAnnotationProcessor.class.getName());

    /**
     * A map holding database configuration properties file names and properties object serving as a cache.
     */
    private Map<String, Properties> dbConfigurationMap = new HashMap<>();

    /**
     * A map holding database properties file name and resource properties object serving as a cache.
     */
    private Map<String, Properties> dbPropertiesMap = new HashMap<>();

    @Override
    public void processAnnotation(final DBProperty dbPropertyAnnotation, final Field field, final Object object) throws AnnotationProcessingException {

        String configuration = dbPropertyAnnotation.configuration().trim();
        String key = dbPropertyAnnotation.key().trim();

        //check attributes
        rejectIfEmpty(configuration, missingAttributeValue("configuration", "@DBProperty", field, object));
        rejectIfEmpty(key, missingAttributeValue("key", "@DBProperty", field, object));

        //check if database connection configuration is not already loaded
        if (!dbConfigurationMap.containsKey(configuration)) {
            loadDatabaseConfigurationProperties(configuration);
        }

        //check if database connection properties are not already loaded
        if (!dbPropertiesMap.containsKey(configuration)) {
            loadDatabaseProperties(configuration);
        }

        //check object obtained from database
        String value = dbPropertiesMap.get(configuration).getProperty(key);
        if (value == null) {
            LOGGER.log(Level.WARNING, "Key ''{0}'' not found in database configured with properties:: {1}",
                    new Object[]{key, dbConfigurationMap.get(configuration)});
            return;
        }

        //inject object in annotated field
        processAnnotation(object, field, key, value);

    }

    private void loadDatabaseProperties(final String configuration) throws AnnotationProcessingException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            Properties dbConfigurationProperties = dbConfigurationMap.get(configuration);
            Class.forName(dbConfigurationProperties.getProperty("db.driver"));
            connection = getConnection(dbConfigurationProperties);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(getSqlQuery(dbConfigurationProperties));
            Properties dbProperties = extractProperties(resultSet, dbConfigurationProperties);
            dbPropertiesMap.put(configuration, dbProperties);
        } catch (Exception e) {
            throw new AnnotationProcessingException("Unable to get database properties", e);
        } finally {
            try {
                closeResources(connection, statement, resultSet);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Unable to close database resources", e);
            }
        }
    }

    private Properties extractProperties(final ResultSet resultSet, final Properties dbConfigurationProperties) throws SQLException {
        Properties dbProperties = new Properties();
        String keyColumn = dbConfigurationProperties.getProperty("db.table.keyColumn");
        String valueColumn = dbConfigurationProperties.getProperty("db.table.valueColumn");
        while (resultSet.next()) {
            String dbKey = resultSet.getString(keyColumn);
            String dbValue = resultSet.getString(valueColumn);
            dbProperties.put(dbKey, dbValue);
        }
        return dbProperties;
    }

    private Connection getConnection(final Properties dbConfigurationProperties) throws SQLException {
        String url = dbConfigurationProperties.getProperty("db.url");
        String user = dbConfigurationProperties.getProperty("db.user");
        String password = dbConfigurationProperties.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }

    private String getSqlQuery(final Properties dbConfigurationProperties) {
        String schema = dbConfigurationProperties.getProperty("db.schema");
        String table = dbConfigurationProperties.getProperty("db.table");
        String keyColumn = dbConfigurationProperties.getProperty("db.table.keyColumn");
        String valueColumn = dbConfigurationProperties.getProperty("db.table.valueColumn");
        return format("SELECT %s, %s FROM %s.%s", keyColumn, valueColumn, schema, table);
    }

    private void closeResources(final Connection connection, final Statement statement, final ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    private void loadDatabaseConfigurationProperties(final String configuration) throws AnnotationProcessingException {
        Properties dbConfigurationProperties = new Properties();
        try {
            InputStream inputStream = getResourceAsStream(configuration);
            if (inputStream != null) {
                dbConfigurationProperties.load(inputStream);
                dbConfigurationMap.put(configuration, dbConfigurationProperties);
            }
        } catch (IOException e) {
            throw new AnnotationProcessingException(format("Unable to get properties from %s", configuration), e);
        }
    }

}
