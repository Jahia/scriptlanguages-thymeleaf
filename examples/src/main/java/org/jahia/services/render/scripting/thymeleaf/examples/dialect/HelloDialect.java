package org.jahia.services.render.scripting.thymeleaf.examples.dialect;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by loom on 07.05.15.
 */
public class HelloDialect extends AbstractDialect {

    public HelloDialect() {
        super();
    }

    //
    // All of this dialect's attributes and/or tags
    // will start with 'hello:'
    //
    public String getPrefix() {
        return "hello";
    }


    //
    // The processors.
    //
    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new SayToAttrProcessor());
        return processors;
    }

}
