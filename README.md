# scriptlanguages-thymeleaf

A experimental module that provides basic Thymeleaf scripting language support. This module hasn't gone through Jahia's rigorous QA process and is therefore not supported.

## Features
- Makes it possible to use Thymeleaf as a scripting languages for Jahia views
- All standard Jahia variables are exposed as Thymeleaf variables, you can display node contents, etc.
- Custom dialects may be added inside custom modules !
- Full integration with Jahia's resource bundle system for messages

## Requirements
- Jahia 7.0.0.0 or more recent
- Maven 3.0+ for module compilation
- JDK 7 (maybe JDK 8 also works but not tested)

## Usage

1. Compile the whole project using : mvn clean install
2. Deploy the core/target/scriptlanguages-thymeleaf*.jar and examples/target/scriptlanguages-thymeleaf-example*.jar modules
3. In Edit mode, add a Basic Content -> ThymeleafNode content node, and enter a value for the thymeleafText property.
The value of this property will be displayed by the node template written in Thymeleaf.

## Example template

You will find an example template in the following directory : examples/src/main/resources/jnt_thymeleafNode/html/thymeleafNode.html
with the following content :

    <!DOCTYPE html SYSTEM "http://www.thymeleaf.org/dtd/xhtml1-strict-thymeleaf-4.dtd">
    <div xmlns="http://www.w3.org/1999/xhtml"
       xmlns:th="http://www.thymeleaf.org" xmlns:hello="http://www.jahia.com/thymeleaf/hello">
        <p th:text="${currentNode.getProperty('thymeleafText').getValue().getString()}">Welcome to our grocery store!</p>
        <p th:text="#{home.welcome}">Message test</p>
        <p hello:sayto="World">Hi ya!</p>
    </div>

As you can see this will simply display the node property as well as display a message coming from the module's resource
bundle file located in src/main/resources/resources/scriptlanguages-thymeleaf.properties. It also supports custom dialects!

## Custom dialects

There is initial support for additional dialects implemented in other modules. In order for these to be recognized,
they must be registered as OSGi services using the org.thymeleaf.dialect.IDialect interface. Here is an example using
a Spring descriptor file.

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:osgi="http://www.springframework.org/schema/osgi"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/osgi
           http://www.springframework.org/schema/osgi/spring-osgi.xsd">

        <bean id="helloThymeleafDialect" class="org.jahia.services.render.scripting.thymeleaf.examples.dialect.HelloDialect" />

        <osgi:service id="helloThymeleafDialectOsgiService" ref="helloThymeleafDialect" interface="org.thymeleaf.dialect.IDialect" />

    </beans>

You will this file inside the examples project in: examples/src/main/resources/META-INF/spring/mod-scriptlanguages-thymeleaf-examples.xml

## TODO

- Remove hack to access localization context in ThymeLeafMessageResolver
- Test and improve overall performance, as not much caching is present
- Test, test, test :)
