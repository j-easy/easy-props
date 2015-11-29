## What is Easy Properties?

Easy Properties is a Java library that allows you to inject configuration properties in Java objects in a declarative way using annotations.

The idea is to implement the _"Inversion Of Control"_ principle : Instead of having objects looking actively for configuration properties,
 these objects simply declare configuration properties they need, and these properties will be provided to them by a tool, Easy Properties for instance!

It is a kind of dependency injection, but for properties. Let's call it _"property injection"_.

## Why Easy Properties?

Dependency injection frameworks allow you to inject properties in your Java objects and they do it very well.

But in order to benefit from this feature, your code should run inside a DI container, or at least, the object in which your are trying to inject properties must be managed by a DI container.

What if your code does **not** run inside a DI container? This is where Easy Properties comes to play, to allow you to benefit from dependency injection without requiring your code to run inside a DI container.

That said, **Easy Properties is a library, not a framework**. It is **not** YADIF (Yet Another DI Framework) :smirk:

## Quick example

With Easy Properties, you declare properties you need on your object's fields using a set of intuitive annotations and instruct it to inject these properties. Let's see an example:

Suppose you have a Java object of type `Bean` which should be configured with:

* An Integer property `threshold` from a system property passed to the JVM with -Dthreshold=100

* A String property `bean.name` from a properties file named `myProperties.properties`

To load these properties in your `Bean` object using Easy Properties, you annotate fields to declare needed configuration properties as follows:

```java
public class Bean {

    @Property(source = "myProperties.properties", key = "bean.name")
    private String beanName;

    @SystemProperty(value = "threshold", defaultValue = "50")
    private int threshold;

    //getters and setters omitted

}
```

and instructs Easy Properties to inject these configuration properties in the annotated fields:

```java
//Instantiate your object
Bean bean = new Bean();

//Instantiate a properties injector
PropertiesInjector propertiesInjector = new PropertiesInjectorBuilder().build();

//Inject properties in your object
propertiesInjector.injectProperties(bean);
```

That's it! Easy Properties will introspect the `Bean` instance looking for fields annotated with `@Property` and `@SystemProperty`, convert each property value to the field's type and inject that value into the annotated field.

**Without** Easy Properties, you would write something like this:

```java
public class Bean {

    private int threshold;

    private String beanName;

    public Bean() {

        //Load 'threshold' property from system properties
        String thresholdProperty = System.getProperty("threshold");
        if ( thresholdProperty != null ) {
            try {
                threshold = Integer.parseInt(thresholdProperty);
            } catch (NumberFormatException e) {
                // log exception
                threshold = 50; //default threshold value;
            }
        }

        //Load 'bean.name' property from properties file
        Properties properties = new Properties();
        try {
            InputStream inputStream = this.getClass().getClassLoader()
                        .getResourceAsStream("myProperties.properties");
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

As you can see, a lot of boilerplate code is written to load two properties, convert them to the right type, etc.

Easy Properties takes care of all this boilerplate for you, which makes your code cleaner, more readable and maintainable.

In this quick example, you have seen two types of properties sources (system and resource bundle). 
Easy Properties allows you to inject properties from many other sources like databases, JNDI contexts, and more.

Even better, Easy Properties allows you write your own annotations and inject properties from a custom configuration source.

Checkout the complete reference in the project's [wiki](https://github.com/benas/easy-properties/wiki).

## Documentation

Easy Properties documentation can be found here : [https://github.com/benas/easy-properties/wiki](https://github.com/benas/easy-properties/wiki)

## Awesome contributors

* [natlantisprog](https://github.com/natlantisprog)

## License
Easy Properties is released under the [MIT License](http://opensource.org/licenses/mit-license.php/).
