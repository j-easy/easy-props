## What is ADP4J ?

Annotation Driven Properties For Java is a library that allows you to inject configuration properties in your objects in a declarative way using annotations.

Configuration properties can be loaded from a variety of sources:

 - System properties passed to JVM
 - Properties files
 - Resource bundles
 - Database, etc

Usually, we write a lot of boilerplate code to load these properties into objects, convert them to typed values, etc.

The idea behind ADP4J is to implement the "Inversion Of Control" principle : Instead of having objects looking actively for configuration properties, these objects simply declare configuration properties they need and these properties will be provided to them by a tool, ADP4J for instance!

Let's see an example. Suppose you have a java object of type `Bean` which should be configured with:

 - An Integer property "threshold" from a system property passed to the JVM with -Dthreshold=100

 - A String property "bean.name" from a properties file named "myProperties.properties"

To load these properties in your `Bean` object using ADP4J, you annotate fields to declare needed configuration properties as follows:

```java
public class Bean {

    @SystemProperty(value = "threshold", defaultValue = "50")
    private int threshold;

    @Property(source = "myProperties.properties", key = "bean.name")
    private String beanName;

    //getters and setters omitted
}
```

and instructs ADP4J to inject these configuration properties in the annotated fields :

```java
//Instantiate your object
Bean bean = new Bean();

//Instantiate ADP4J properties injector
PropertiesInjector propertiesInjector = new PropertiesInjectorBuilder().build();

//Inject properties in your object
propertiesInjector.injectProperties(bean);
```

That it! ADP4J will introspect the `Bean` type instance looking for fields annotated with `@Property` and `@SystemProperty`,
 convert each property value to the field type and inject that value into the annotated field.

Without ADP4J, you would write something like this :

```java
public class Bean {

    private int threshold;

    private String beanName;

    public Bean() {

        //Load 'threshold' property from system properties
        String thresholdProperty = System.getProperty("threshold");
        try {
            threshold = Integer.parseInt(thresholdProperty);
        } catch (NumberFormatException e) {
            // log exception
            threshold = 50; //default threshold value;
        }

        //Load 'bean.name' property from properties file
        Properties properties = new Properties();
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("myProperties.properties");
            if (inputStream != null) {
                properties.load(inputStream);
                beanName = properties.getProperty("bean.name");
            }
        } catch (IOException ex) {
            // log exception
            beanName = "FOO"; // default bean name value
        }

    }

    //getters and setters omitted

}
```

As you can see, a lot of plumbing code is written to load two properties, convert them to the right type, etc.

ADP4J handles all this plumbing for you, which makes your code cleaner, more readable and maintainable.

## Built-in Annotations

By default, ADP4J provides 8 annotations to load configuration properties from a variety of sources.

### @Property

This annotation can be used to load a property from a java properties file.

The annotation can be declared on a field to tell ADP4J to inject the specified key value from the properties file into the annotated field.

Attributes of this annotation are described in the following table:

| Attribute  | Type    | Required | Description                                       |
|:-----------|:-------:|:--------:|---------------------------------------------------|
| source     | String  | yes      | The properties file name                          |
| key        | String  | yes      | The key to load from the source properties file   |

Example :

```java
@Property(source = "myProperties.properties", key = "bean.name")
private String beanName;
```

In this example, ADP4J will look for the property `bean.name` in the `myProperties.properties` properties file in the classpath and set its value to the `userHome` field.

### @SystemProperty

This annotation allows you to inject a property from the system properties passed to your java program at JVM startup using the -D prefix.

The @SystemProperty annotation can be declared on a field and have the following attributes :

| Attribute    | Type    | Required | Description                                                         |
|:-------------|:-------:|:--------:|---------------------------------------------------------------------|
| value        | String  | yes      | The system property to inject in the annotated field.               |
| defaultValue | String  | no       | The default value to set in case the system property does not exist |

Example:

```java
@SystemProperty("user.home")
private String userHome;
```

In this example, ADP4J will look for the system property `user.home` and set its value to the `userHome` field.

### @I18NProperty

This annotation allows you to inject a property from a java resource bundle for a specified locale.

The annotation can be declared on a field to tell ADP4J to inject the specified key value from the resource bundle into the annotated field.

Attributes of this annotation are described in the following table:

| Attribute  | Type     | Required | Description                                                      |
|:-----------|:--------:|:--------:|------------------------------------------------------------------|
| bundle     | String   | yes      | The resource bundle containing the property to load              |
| key        | String   | yes      | The key to load from the resource bundle                         |
| language   | String   | no       | The locale language to use (default to default locale language)  |
| country    | String   | no       | The locale country to use (default to default locale country)    |
| variant    | String   | no       | The locale variant to use (default to default locale variant)    |

Example :

```java
@I18NProperty(bundle = "i18n/messages", key = "my.message")
private String message;
```

In this example, ADP4J will look for the property `my.message` in the resource bundle `i18n/messages.properties` in the classpath and set its value to the `message` field.

Note that this annotation is not suited to applications where the locale may change during the application lifetime. This is because java annotation attributes can only have constant values.

### @DBProperty

This annotation can be used to load properties from a database.

Attributes of this annotation are described in the following table:

| Attribute     | Type    | Required | Description                                                        |
|:--------------|:-------:|:--------:|--------------------------------------------------------------------|
| configuration | String  | yes      | The configuration file containing database connection properties.  |
| key           | String  | yes      | The key to load from the specified column in database table.       |

The annotation processor of `@DBProperty` will load database connection properties from the file specified in `configuration` attribute. The following is an example of a configuration file:

```
db.driver=org.hsqldb.jdbcDriver
db.url=jdbc:hsqldb:mem:test
db.user=sa
db.password=pwd
db.schema=public
db.table=ApplicationProperties
db.table.keyColumn=key
db.table.valueColumn=value
```

Properties `db.driver`, `db.url`, `db.user`, `db.password` and `db.schema` are self explanatory : ADP4J will use these parameters to connect to the database.

`db.table` specifies the table containing keys (in `db.table.keyColumn` column) and values (in `db.table.valueColumn` column).

Example :

```java
@DBProperty(configuration = "database.properties", key = "bean.name")
private String name;
```

In this example, if we use the previous configuration file, ADP4J will look for the key `bean.name` in the `key` column of the `ApplicationProperties` table defined in `test` database and set its value to the `name` field.

Note that ADP4J caches properties loaded from a database for further reuse. If you have more than one field annotated with @DBProperty with the same database configuration, ADP4J will connect only once the the specified database.

### @JNDIProperty

This annotation can be declared on a field to inject a property or an object from a JNDI context.

The annotation have a single attribute that corresponds to the object name in JNDI context. Example:

```java
@JNDIProperty("jdbc/dataSource")
private javax.sql.DataSource dataSource;
```

In this example, ADP4J will look for an object named `jdbc/dataSource` in JNDI context and inject it in `dataSource` field.

Of course, you should have already specified JNDI parameters (context provider, factory, etc) via system properties or a properties file `jndi.properties` in the classpath (standard JNDI configuration).

### @ManifestProperty

This annotation can be used to inject a header value from a META-INF/MANIFEST.MF file in the annotated field.

Attributes of this annotation are described in the following table:

| Attribute     | Type    | Required | Description                                                                                          |
|:--------------|:-------:|:--------:|------------------------------------------------------------------------------------------------------|
| jar           | String  | no       | The jar file containing the manifest file to load (by default, the jar containing the target object) |
| header        | String  | yes      | The header to load from the specified manifest file                                                  |

Example :

```java
@ManifestProperty(jar = "junit-4.11.jar", header = "Created-By")
private String createdByJdk;
```

In this example, we are injecting the `Created-By` header value of the META-INF/MANIFEST.MF file of the jar `junit-4.11.jar` into the `createdByJdk` field.

### @MavenProperty

This annotation can be used to load and inject maven properties into java objects. A typical usage of this annotation is to know the application version at runtime.

Attributes of this annotation are described in the following table:

| Attribute     | Type    | Required | Description                                                             |
|:--------------|:-------:|:--------:|-------------------------------------------------------------------------|
| source        | String  | no       | The source file containing maven properties (pom.properties by default) |
| key           | String  | yes      | The key to load from the specified pom properties                       |
| groupId       | String  | yes      | The groupId of the JAR containing the pom.properties file               |
| artifact      | String  | yes      | The artifact of the JAR containing the pom.properties file              |

Example :

```java
@MavenProperty(key = "version", groupId = "commons-beanutils", artifactId = "commons-beanutils")
private String pomVersion;
```

In this example, we are injecting the current version number of the commons-beanutils dependency into the `pomVersion` field.

Note that ADP4J caches maven context loaded from pom.properties for further reuse.

### @Properties

This annotation can be declared on a field of type `java.util.Properties` and allows you to inject all properties of a properties file in that field.

The annotation have a single attribute that corresponds to the properties file name. Example:

```java
@Properties("myProperties.properties")
private java.util.Properties myProperties;
```

In this example, ADP4J will populate the `myProperties` field with properties from the file `myProperties.properties`.

## Using ADP4J in a web environment

Using ADP4J to inject properties in web components is straightforward. Like any other object, all you need is to annotate your instance variable with ADP4J annotations and provide a setter.

Usually, web frameworks provide initialization methods in components lifecycle, you can call ADP4J code inside these methods.

This section will show how to use ADP4J to inject properties in several web frameworks components.

### Injecting properties in a Servlet

The servlet API provides an initialization method `init` that is called when a servlet is first initialized by the container.
This is a good place to call ADP4J code to inject properties in the servlet instance variables.

```java
public class MyServlet extends HttpServlet {

    @SystemProperty(value = "user.home", defaultValue = "/home/me")
    private String userHome;

    @Override
    public void init() throws ServletException {
        PropertiesInjector propertiesInjector = new PropertiesInjectorBuilder().build();
        try {
            propertiesInjector.injectProperties(this);
        } catch (Exception e) {
            System.err.println("Unable to inject properties in servlet MyServlet!");
        }
    }

    public void setUserHome(String userHome) {
        this.userHome = userHome;
    }
}
```

In this example, ADP4J will inject the System property `user.home` in the `userHome` field when the servlet `MyServlet` is first initialized by the container.

### Injecting properties in a Struts Action

Struts 2 provides an initialization hook for actions through the [Preparable][] interface.

The following example shows how to inject the System property `user.home` in the `userHome` field of the `MyAction` action when it is initialized by Struts.

```java
public class MyAction extends ActionSupport implements Preparable {

    @SystemProperty(value = "user.home", defaultValue = "/home/me")
    private String userHome;

    public void prepare() throws Exception {
        PropertiesInjector propertiesInjector = new PropertiesInjectorBuilder().build();
        try {
            propertiesInjector.injectProperties(this);
        } catch (Exception e) {
            System.err.println("Unable to inject properties in action MyAction!");
        }
    }

    public void setUserHome(String userHome) {
        this.userHome = userHome;
    }

}
```

Note that the `prepare` interceptor should be enabled, as mentioned in Struts documentation [here].

### Injecting properties in a Tapestry Page

Tapestry 5 provides an initialization hook for pages through the [Activate][] event.

The following example shows how to inject the System property `user.home` in the `userHome` field of the `MyPage` page when it is initialized by Tapestry.

```java
public class MyPage {

    @SystemProperty(value = "user.home", defaultValue = "/home/me")
    private String userHome;

    @OnEvent(EventConstants.ACTIVATE)
    public void init(){
        PropertiesInjector propertiesInjector = new PropertiesInjectorBuilder().build();
        try {
            propertiesInjector.injectProperties(this);
        } catch (Exception e) {
            System.err.println("Unable to inject properties in page MyPage !");
        }
    }

    public void setUserHome(String userHome) {
        this.userHome = userHome;
    }

}
```

## Custom Annotations

With ADP4J, you can write your own annotations to load configuration properties from a custom location.
The following steps describe how to create your own annotation and how to use it with ADP4J.

### 1. Create custom annotation

First, you need to create your annotation with all the necessary information about where to load properties, which property to load, etc.
You can specify these parameters using public methods in your annotation interface.
You can see how the built-in annotations are defined in the `io.github.benas.adp4j.annotations` [package][annotationsPackage].

### 2. Implement the `AnnotationProcessor` interface

The `AnnotationProcessor` interface allows you to specify how to process your annotation to load all the necessary parameters specified by the user.

The following is the definition of this interface:

 ```java
public interface AnnotationProcessor<T extends Annotation> {

    /**
     * Process an annotation of type T to be introspected by ADP4J.
     *
     * @param annotation the annotation to process.
     * @param field the field annotated with the annotation.
     * @param object the object being configured.
     * @throws Exception thrown if an exception occurs during annotation processing
     */
    void processAnnotation(T annotation, Field field, Object object) throws Exception;

}
 ```

You can see some examples of how the built-in annotations are processed in the `io.github.benas.adp4j.processors` [package][processorsPackage].

### 3. Register the annotation processor within ADP4J

To register your custom annotation processor within ADP4J, you can use the `PropertiesInjectorBuilder` API as follows:

 ```java
 //Instantiate ADP4J properties injector and register custom annotation processor
 PropertiesInjector propertiesInjector = new PropertiesInjectorBuilder()
                             .registerAnnotationProcessor(MyAnnotation.class, new myAnnotationProcessor())
                             .build();
 ```

Now you can annotate any field with your annotation and ADP4J will automatically use the registered annotation processor to
load the value, convert it to the field type and set it to the field value.

## Getting started

ADP4J is a single jar file with no dependencies. To build it from sources, you need to have maven installed and set up.

To use ADP4J, please follow these instructions :

 * $>`git clone https://github.com/benas/adp4j.git`

 * $>`mvn package`

 * Add the generated jar `target/adp4j-${version}.jar` to your application's classpath

If you use maven, you should add the following dependency to your pom.xml :
```xml
<dependency>
    <groupId>io.github.benas</groupId>
    <artifactId>adp4j</artifactId>
    <version>${version}</version>
</dependency>
```

## License
ADP4J is released under the [MIT License][].

[annotationsPackage]: https://github.com/benas/adp4j/tree/master/src/main/java/io/github/benas/adp4j/annotations
[processorsPackage]: https://github.com/benas/adp4j/tree/master/src/main/java/io/github/benas/adp4j/processors
[Preparable]: http://struts.apache.org/release/2.1.x/struts2-core/apidocs/com/opensymphony/xwork2/Preparable.html
[here]: http://struts.apache.org/release/2.1.x/docs/prepare-interceptor.html
[Activate]: http://tapestry.apache.org/current/apidocs/org/apache/tapestry5/EventConstants.html#ACTIVATE
[MIT License]: http://opensource.org/licenses/mit-license.php/