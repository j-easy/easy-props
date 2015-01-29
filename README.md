## What is Projector?

Projector (**PRO**perty inj**ECTOR**) is a Java library that allows you to inject configuration properties in Java objects in a declarative way using annotations.

The idea behind Projector is to implement the _"Inversion Of Control"_ principle : Instead of having objects looking actively for configuration properties, these objects simply declare configuration properties they need and these properties will be provided to them by a tool, Projector for instance!

It is a kind of dependency injection tool, but for properties. Let's call it _"property injection"_.

## Why Projector?

Dependency injections frameworks allow you to inject properties in your Java objects and they do it very well.

In order to benefit from this feature, your code should run inside a DI container, or at least, the object in which your are trying to inject properties must be managed by a DI container.

But what if your code does not run inside a DI container? This is where Projector comes to play, to allow you to benefit from DI without requiring your code to run inside a DI container.

That said, **Projector is a library, not a framework**. It is **not** YADIF (Yet Another DI Framework) :smirk:

## Quick example

With Projector, you declare properties you need on your objects using a set of intuitive annotations and instruct it to inject these properties. Let's see an example:

Suppose you have a Java object of type `Bean` which should be configured with:

* An Integer property `threshold` from a system property passed to the JVM with -Dthreshold=100

* A String property `bean.name` from a properties file named `myProperties.properties`

To load these properties in your `Bean` object using Projector, you annotate fields to declare needed configuration properties as follows:

```java
public class Bean {

    @Property(source = "myProperties.properties", key = "bean.name")
    private String beanName;

    @SystemProperty(value = "threshold", defaultValue = "50")
    private int threshold;

    //getters and setters omitted

}
```

and instructs Projector to inject these configuration properties in the annotated fields:

```java
//Instantiate your object
Bean bean = new Bean();

//Instantiate Projector
PropertiesInjector propertiesInjector = new PropertiesInjectorBuilder().build();

//Inject properties in your object
propertiesInjector.injectProperties(bean);
```

That's it! Projector will introspect the `Bean` instance looking for fields annotated with @Property and @SystemProperty, convert each property value to the field type and inject that value into the annotated field.

**Without** Projector, you would write something like this:

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

Projector handles all this boilerplate for you, which makes your code cleaner, more readable and maintainable.

In this quick example, you have seen two types of properties sources (system and resource bundle). Projector allows you to inject properties from many other sources like databases, JNDI contexts, and more.

Even better, Projector allows you write your own annotations and inject properties from a custom configuration source.

Checkout the complete reference in the project's [wiki](https://github.com/benas/projector/wiki).

## Documentation

Projector's documentation can be found here : [https://github.com/benas/projector/wiki](https://github.com/benas/projector/wiki)

## Awesome contributors

* [natlantisprog](https://github.com/natlantisprog)

## License
Projector is released under the [MIT License](http://opensource.org/licenses/mit-license.php/).
