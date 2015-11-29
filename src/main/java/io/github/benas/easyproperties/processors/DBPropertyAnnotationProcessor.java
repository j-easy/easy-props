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
import io.github.benas.easyproperties.api.AnnotationProcessor;

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
public class DBPropertyAnnotationProcessor extends AbstractAnnotationProcessor implements AnnotationProcessor<DBProperty> {

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

        //check configuration attribute value
        if (configuration.isEmpty()) {
            throw new AnnotationProcessingException(missingAttributeValue("configuration", "@DBProperty", field, object));
        }

        //check key attribute value
        if (key.isEmpty()) {
            throw new AnnotationProcessingException(missingAttributeValue("key", "@DBProperty", field, object));
        }

        //check if database connection properties are not already loaded
        if (!dbConfigurationMap.containsKey(configuration)) {
            Properties dbConfigurationProperties = new Properties();
            try {
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configuration);
                if (inputStream != null) {
                    dbConfigurationProperties.load(inputStream);
                    dbConfigurationMap.put(configuration, dbConfigurationProperties);
                } else {
                    throw new AnnotationProcessingException(missingSourceFile(configuration, field, object));
                }
            } catch (IOException ex) {
                throw new AnnotationProcessingException(missingSourceFile(configuration, field, object), ex);
            }
        }

        //check if database properties are not already loaded
        if (!dbPropertiesMap.containsKey(configuration)) {
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                Properties dbConfigurationProperties = dbConfigurationMap.get(configuration);

                Class.forName(dbConfigurationProperties.getProperty("db.driver"));

                String url = dbConfigurationProperties.getProperty("db.url");
                String user = dbConfigurationProperties.getProperty("db.user");
                String password = dbConfigurationProperties.getProperty("db.password");
                connection = DriverManager.getConnection(url, user, password);

                String schema = dbConfigurationProperties.getProperty("db.schema");
                String table = dbConfigurationProperties.getProperty("db.table");
                String keyColumn = dbConfigurationProperties.getProperty("db.table.keyColumn");
                String valueColumn = dbConfigurationProperties.getProperty("db.table.valueColumn");
                statement = connection.createStatement();
                resultSet = statement.executeQuery(
                        "SELECT " + keyColumn + ", " + valueColumn + " FROM " + schema + "." + table
                );

                Properties dbProperties = new Properties();
                while (resultSet.next()) {
                    String dbKey = resultSet.getString(keyColumn);
                    String dbValue = resultSet.getString(valueColumn);
                    dbProperties.put(dbKey, dbValue);
                }
                dbPropertiesMap.put(configuration, dbProperties);

            } catch (Exception e) {
                throw new AnnotationProcessingException("Unable to get database properties", e);
            } finally {
                try {
                    if (resultSet != null ) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch(SQLException e) {
                    LOGGER.log(Level.WARNING, "Unable to close database resources", e);
                }
            }
        }

        //check object obtained from database
        String value = dbPropertiesMap.get(configuration).getProperty(key);
        if (value == null || value.isEmpty()) {
            throw new AnnotationProcessingException(format("Key %s not found or empty in database configured with properties: %s", key, dbConfigurationMap.get(configuration)));
        }

        //inject object in annotated field
        processAnnotation(object, field, key, value);

    }

}
