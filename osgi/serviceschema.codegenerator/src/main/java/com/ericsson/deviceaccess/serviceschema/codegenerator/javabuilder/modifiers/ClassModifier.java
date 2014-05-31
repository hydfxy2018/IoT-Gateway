package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers;

/**
 * Defines type of class
 *
 * @author delma
 */
public enum ClassModifier {
    /**
     * Normal class
     */
    CLASS,
    /**
     * Interface
     */
    INTERFACE,
    /**
     * Enum aka class with limited instances.
     */
    ENUM,
    /**
     * Enum with single instance.
     */
    SINGLETON,
    /**
     * Hybrid between class and interface.
     * Can have normal and abtract methods
     */
    ABSTRACT;

    /**
     * Gets string representation of modifier.
     *
     * @return modifier
     */
    public String get() {
        if (this == SINGLETON) {
            return ENUM.get();
        }
        if (this == ABSTRACT) {
            return "abstract class";
        }
        return toString().toLowerCase();
    }
}
