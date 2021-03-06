/*
 * Copyright Ericsson AB 2011-2014. All Rights Reserved.
 *
 * The contents of this file are subject to the Lesser GNU Public License,
 *  (the "License"), either version 2.1 of the License, or
 * (at your option) any later version.; you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * License along with this software. If not, it can be
 * retrieved online at https://www.gnu.org/licenses/lgpl.html. Moreover
 * it could also be requested from Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
 * WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
 * OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY OF ANY KIND,

 * EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
 * LIBRARY IS WITH YOU. SHOULD THE LIBRARY PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE LIBRARY AS PERMITTED ABOVE, BE LIABLE TO YOU FOR
 * DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE LIBRARY
 * (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE
 * OF THE LIBRARY TO OPERATE WITH ANY OTHER SOFTWARE), EVEN IF SUCH
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.ericsson.deviceaccess.spi.schema.based;

import com.ericsson.deviceaccess.spi.impl.genericdevice.GDActionImpl;
import com.ericsson.deviceaccess.spi.impl.genericdevice.GDServiceImpl;
import com.ericsson.deviceaccess.spi.schema.ActionDefinition;
import com.ericsson.deviceaccess.spi.schema.ActionSchema;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchemaError;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A service implementation based on a schema.
 */
public class SBServiceBase extends GDServiceImpl implements SBService {

    public static final String REFRESH_PROPERTIES = "refreshProperties";
    private final ServiceSchema serviceSchema;
    private final Map<String, ActionDefinition> actionDefinitions = new HashMap<>();

    /**
     * Creates instance based on specified schema.
     *
     * @param serviceSchema the schema
     */
    public SBServiceBase(ServiceSchema serviceSchema) {
        super(serviceSchema.getName(), serviceSchema.getPropertiesSchemas());
        this.serviceSchema = serviceSchema;
        init(serviceSchema);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SBService defineAction(String name, ActionDefinition actionDefinition) {
        if (getAction(name) == null) {
            throw new ServiceSchemaError("The action: '" + name + "' is not specified in the service schema");
        }

        if (actionDefinitions.containsKey(name)) {
            throw new ServiceSchemaError("The action: '" + name + "' has already been defined");
        }

        actionDefinitions.put(name, actionDefinition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SBService defineCustomAction(final ActionSchema actionSchema, ActionDefinition actionDefinition) {
        String name = actionSchema.getName();
        if (getAction(name) != null) {
            throw new ServiceSchemaError("The action: '" + name + "' is already defined in the service schema");
        }

        // This is an action not defined in the schema.
        createAction(actionSchema);

        actionDefinitions.put(name, actionDefinition);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void validateSchema() {
        serviceSchema.getActionSchemas().forEach(action -> {
            String name = action.getName();
            if (action.isMandatory() && !actionDefinitions.containsKey(name)) {
                throw new ServiceSchemaError("The action: '" + name + "' in service: '" + getName() + "' is mandatory, but lacks definition.");
            }
        });
    }

    /**
     * Creates the actions and parameters based on the specified schema.
     *
     * @param serviceSchema
     */
    private void init(ServiceSchema serviceSchema) {
        serviceSchema.getActionSchemas().forEach(this::createAction);

        createAction(new ActionSchema.Builder().setName(REFRESH_PROPERTIES).setMandatory(true).build());

        serviceSchema.getPropertiesSchemas().forEach(schema -> {
            getProperties().setStringValue(schema.getName(), schema.getDefaultStringValue());
        });
    }

    /**
     * @param actionSchema
     */
    private void createAction(final ActionSchema actionSchema) {
        String name = actionSchema.getName();
        List<ParameterSchema> argumentsSchemas = actionSchema.getArgumentsSchemas();
        List<ParameterSchema> resultParametersSchemas = actionSchema.getResultSchema();
        GDActionImpl genericDeviceActionImpl = new SBAction(name, this, argumentsSchemas, resultParametersSchemas);
        putAction(genericDeviceActionImpl);
    }

    ActionDefinition getActionDefinitions(String name) {
        return actionDefinitions.get(name);
    }
}
