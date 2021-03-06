package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;

/**
 * Implementers code can have {@link OptionalModifier}s.
 *
 * @author delma
 */
public interface Modifierable {

    /**
     * Adds optional modifier
     *
     * @param modifier modfier to be added
     * @return this
     */
    Modifierable addModifier(OptionalModifier modifier);

}
