package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

/**
 *
 * @author delma
 */
public class Constant extends Variable {

    public Constant(String type, String name, String value) {
        super(type, name);
        init(value);
        addModifier(OptionalModifier.FINAL);
        addModifier(OptionalModifier.STATIC);
        setAccessModifier(AccessModifier.PUBLIC);
    }

}
