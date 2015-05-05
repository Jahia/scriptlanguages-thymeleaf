# scriptlanguages-thymeleaf

A experimental module that provides basic Thymeleaf scripting language support. This module should mostly be considered
as a proof-of-concept because it contains some hacks to get this working properly. It is not recommended to use this
code in production environment

## Requirements
- Jahia 7.0.0.0 or more recent
- Maven 3.0+ for module compilation
- JDK 7 (maybe JDK 8 also works but not tested)

## Usage

1. Deploy the module
2. In Edit mode, add a Basic Content -> ThymeleafNode content node, and enter a value for the thymeleafText property.
The value of this property will be displayed by the node template written in Thymeleaf.

## Example template

You will find an example template in the following directory : src/main/resources/jnt_thymeleafNode/html/thymeleafNode.html
with the following content :

    <!DOCTYPE html SYSTEM "http://www.thymeleaf.org/dtd/xhtml1-strict-thymeleaf-4.dtd">
    <div xmlns="http://www.w3.org/1999/xhtml"
       xmlns:th="http://www.thymeleaf.org">
        <p th:text="${currentNode.getProperty('thymeleafText').getValue().getString()}">Welcome to our grocery store!</p>
        <p th:text="#{home.welcome}">Message test</p>
    </div>

As you can see this will simply display the node property as well as display a message coming from the module's resource
bundle file located in src/main/resources/resources/scriptlanguages-thymeleaf.properties.

## Limitations
- Currently only this module can contain Thymeleaf templates. If they are added in other modules they will not be
detected by the engine

## TODO
- Remove all hacks by modifying Jahia core to enable dynamic deployment of new scripting language support
- Make it possible for other modules to deploy Thymeleaf templates (probably requires changes in Jahia core or
  implementing a custom bundle extender).
- Add support for custom dialects (generally in seperate modules), possibly requiring a bundle extender of some kind
- Remove hack to access localization context in ThymeLeafMessageResolver
- Test and improve overall performance, as not much caching is present
- Test, test, test :)
