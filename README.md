***

<div align="center">
    <b><em>Easy Props</em></b><br>
    The simple, stupid properties library for Java&trade;
</div>

<div align="center">

[![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT)
[![Build Status](https://github.com/j-easy/easy-props/workflows/Java%20CI/badge.svg)](https://github.com/j-easy/easy-props/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jeasy/easy-props/badge.svg?style=flat)](http://search.maven.org/#artifactdetails|org.jeasy|easy-props|3.1.0|)
[![Javadocs](http://www.javadoc.io/badge/org.jeasy/easy-props.svg)](http://www.javadoc.io/doc/org.jeasy/easy-props)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/j-easy/easy-props)

</div>

***

## Latest news

* 18/09/2020: Version 3.1.0 is now released with a number of new features and enhancements! You can find all details about the changes in the [release notes](https://github.com/j-easy/easy-props/releases).

## What is Easy Props?

Easy Props is a library to inject configuration properties in Java objects declaratively using annotations.
Let's see a quick example. Suppose you have an object of type `Bean` which should be configured with:

* An Integer property `threshold` from a system property passed to the JVM with `-Dthreshold=100`
* A String property `bean.name` from a properties file named `myProperties.properties`

To load these properties in the `Bean` object using Easy Props, you annotate fields as follows:

```java
public class Bean {

    @Property(source = "myProperties.properties", key = "bean.name")
    private String beanName;

    @SystemProperty(value = "threshold", defaultValue = "50")
    private int threshold;

    //getters and setters omitted

}
```

and instruct the library to inject these properties in the annotated fields:

```java
//Instantiate your object
Bean bean = new Bean();

//Create a PropertiesInjector and inject properties in your object
aNewPropertiesInjector().injectProperties(bean);
```

That's it! Easy Props will:

* introspect the `Bean` instance looking for fields annotated with `@Property` and `@SystemProperty`
* convert each property value to the target field's type
* and inject that value into the annotated field

You can also configure your object at construction time:

```java
public class Bean {

    @Property(source = "myProperties.properties", key = "bean.name")
    private String beanName;

    @SystemProperty(value = "threshold", defaultValue = "50")
    private int threshold;

    public Bean () {
        aNewPropertiesInjector().injectProperties(this);
    }

    //getters and setters omitted

}
```

Now just create your object and it will be configured and ready to use.

**Without** Easy Props, you would write something like this:

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

As you can see, a lot of boilerplate code is written to load just two properties, convert them to the target field type, etc.
Easy Props takes care of all this boilerplate with a couple of intuitive annotations, which makes your code cleaner, more readable and maintainable.

In this quick example, you have seen two types of properties sources (system and resource bundle).
Easy Props can inject properties from many other sources like databases, JNDI contexts, environment variables and more!

Even better, Easy Props allows you write your own annotations and inject properties from custom configuration sources.
Checkout the complete reference in the project's [wiki](https://github.com/j-easy/easy-props/wiki).

## Why Easy Props?

Dependency injection frameworks allow you to inject properties in Java objects and they do it well.
But in order to benefit from this feature, your code should run inside a DI container, or at least, the object in which you are trying to inject properties must be managed by a DI container.
What if your code does **not** run inside a DI container? This is where Easy Props comes to play, to allow you to benefit from properties injection **without** requiring your code to run inside a DI container.

## Core features

* Lightweight library with no dependencies
* Type safe access to configuration properties
* Declarative configuration with intuitive annotations
* Ability to inject properties from custom configuration sources
* Ability to hot reload configuration automatically at runtime
* Ability to manage configuration at runtime via JMX

## Contribution

You are welcome to contribute to the project with pull requests on GitHub.

If you find a bug or want to request a feature, please use the [issue tracker](https://github.com/j-easy/easy-props/issues).

For any further question, you can use the [Gitter channel](https://gitter.im/j-easy/easy-props) of the project.

## Awesome contributors

* [gscho](https://github.com/gscho)
* [natlantisprog](https://github.com/natlantisprog)
* [pblanche](https://github.com/pblanche)
* [michaelcouck](https://github.com/michaelcouck)

Thank you all for your contributions!

## License

Easy Props is released under the terms of the MIT license:

```
The MIT License (MIT)

Copyright (c) 2020 Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
