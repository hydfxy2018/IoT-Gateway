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
package com.ericsson.deviceaccess;

import com.ericsson.common.util.serialization.Format;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.spi.event.EventManager;
import com.ericsson.deviceaccess.spi.genericdevice.GDActivator;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceImpl;
import com.ericsson.deviceaccess.spi.impl.genericdevice.GDActionImpl;
import com.ericsson.deviceaccess.spi.impl.genericdevice.GDServiceImpl;
import com.ericsson.deviceaccess.spi.schema.ServiceSchema;
import com.ericsson.research.common.testutil.ReflectionTestUtil;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestSerialization {

    private JUnit4Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    // String template = "{\"action\":{\"test\":{\"arguments\":{\"requester\":null},\"name\":\"test\"}},\"name\":\"test\",\"parameter\":{\"math\":{\"name\":\"math\",\"value\":\"100\"}},\"status\":null}";
    String template = "{\"name\":\"test\",\"actions\":[{\"name\":\"action\",\"arguments\": [{\"name\":\"arg\",\"type\":\"java.lang.Integer\",\"minValue\":\"-10\",\"maxValue\":\"10\",\"defaultValue\":\"0\"},{\"name\":\"arg2\",\"type\":\"java.lang.Integer\",\"minValue\":\"-10\",\"maxValue\":\"10\",\"defaultValue\":\"0\"}],\"result\": [{\"name\":\"res1\",\"type\":\"java.lang.Integer\",\"minValue\":\"-2147483648\",\"maxValue\":\"2147483647\",\"defaultValue\":\"0\"}]}],\"properties\":[{\"prop1\":\"100\"}]}";
    ServiceSchema serviceSchema = new ServiceSchema.Builder("@@TEST@@")
            .addAction(a -> {
                a.setName("action");
                a.setMandatory(true);
                a.addArgument(p -> {
                    p.setName("arg");
                    p.setType(Integer.class);
                    p.setMinValue("-10");
                    p.setMaxValue("10");
                });
                a.addArgument(p -> {
                    p.setName("arg2");
                    p.setType(Integer.class);
                    p.setMinValue("-10");
                    p.setMaxValue("10");
                });
                a.addResult("res1", Integer.class);
            })
            .addAction("optionalAction")
            .addProperty("prop1", Integer.class)
            .build();

    /*
     * @Test public void serializeTestServiceWithJSONIC(){ TestService test =
     * new TestService(); GenericDeviceActionImpl act = new
     * GenericDeviceActionImpl(); act.setName("test"); test.putAction(act);
     * test.getParameter().setIntValue("math", 100); String json =
     * JSON.encode(test); System.out.println(json);
     *
     * TestService test2 = JSON.decode(json, TestService.class);
     * assertEquals(test2.getName(), test.getName());
     * assertEquals(test2.getParameter().getStringValue("math"),
     * test.getParameter().getStringValue("math"));
     * assert(test2.getAction("test") != null);
     * assertEquals(test2.getAction("test").getName(),
     * test.getAction("test").getName()); }
     */

    /*
     * @Test public void serializeTestDeviceWithJSONIC(){ GenericDeviceImpl dev
     * = new GenericDeviceImpl(); TestService test = new TestService();
     * dev.putService(test); GenericDeviceActionImpl act = new
     * GenericDeviceActionImpl(); act.setName("test"); GenericDeviceProperties
     * args = new GenericDevicePropertiesImpl(); args.setStringValue("testArg",
     * "testArgValue"); act.setArguments(args); test.putAction(act);
     * test.getParameter().setIntValue("math", 100); String json =
     * JSON.encode(dev); System.out.println(json);
     *
     * GenericDeviceImpl dev2 = JSON.decode(json, GenericDeviceImpl.class);
     * GenericDeviceService test2 = dev2.getService(test.getName());
     * assertEquals(test2.getName(), test.getName());
     * assertEquals(test2.getParameter().getStringValue("math"),
     * test.getParameter().getStringValue("math"));
     * assert(test2.getAction("test") != null);
     * assertEquals(test2.getAction("test").getName(),
     * test.getAction("test").getName());
     * assertEquals(test2.getAction("test").getArguments
     * ().getStringValue("testArg"),
     * test.getAction("test").getArguments().getStringValue("testArg")); }
     */
//    @Test
//    public void testInterfaceUnmarshal() {
//        GenericDeviceAction action = JSON.decode(template, GenericDeviceActionImpl.class);
//    }
    @Test
    public void testGetLeafNode() throws Exception {
        GenericDeviceImpl dev = new GenericDeviceImpl() {
        };
        TestService test = new TestService();
        test.getProperties().setIntValue("prop1", 10);
        dev.putService(test);
        String node = dev.getSerializedNode("services/test/properties/prop1/currentValue", Format.JSON);

        assertEquals("10", node);
    }

    @Test
    public void testGetServiceNode() throws Exception {
        GenericDeviceImpl dev = new GenericDeviceImpl() {
        };
        TestService test = new TestService();
        test.getProperties().setIntValue("prop1", 100);
        dev.putService(test);
        String node = dev.getSerializedNode("services/test", Format.JSON);
        System.out.println(node);

        assertTrue(node.contains("prop1"));

        node = dev.getSerializedNode("services/test/properties", Format.JSON);
        System.out.println(node);

        assertTrue(node.contains("prop1"));

        node = dev.getSerializedNode("services/test/action", Format.JSON);
        System.out.println(node);
        assertTrue(node.contains("arg2"));

        node = dev.getSerializedNode("services/test/action/arguments", Format.JSON);
        System.out.println(node);

        assertTrue(node.contains("arg2"));
        assertTrue(node.contains("res1"));
    }

    @Test
    public void testGetNonExistingNode() throws Exception {
        GenericDeviceImpl dev = new GenericDeviceImpl() {
        };
        final EventManager eventManager = context.mock(EventManager.class);
        ReflectionTestUtil.setField(GDActivator.class, "eventManager", eventManager);
        context.checking(new Expectations() {
            {
                oneOf(eventManager).addPropertyEvent(with(aNonNull(String.class)), with(aNonNull(String.class)), with(aNonNull(Map.class)));
            }
        });

        dev.setName("dev");
        Object node = null;
        try {
            node = dev.getSerializedNode("services/nonexist", Format.JSON);
        } catch (GDException e) {
        }

        assert node == null;
    }

    /*
     * @Test public void testServiceToJson() throws Exception{ TestService test
     * = new TestService(); GenericDeviceActionImpl act = new
     * GenericDeviceActionImpl(); act.setName("test"); test.putAction(act);
     * test.getParameter().setIntValue("math", 100); String json =
     * test.serialize(com.ericsson.deviceaccess.api.Constants.FORMAT_JSON);
     * System.out.println(json);
     *
     * TestService test2 = JSON.decode(json, TestService.class);
     * assertEquals(test2.getName(), test.getName());
     * assertEquals(test2.getParameter().getStringValue("math"),
     * test.getParameter().getStringValue("math"));
     * assert(test2.getAction("test") != null);
     * assertEquals(test2.getAction("test").getName(),
     * test.getAction("test").getName()); }
     */

    /*
     * @Test public void testDeviceToJson() throws Exception { GenericDevice dev
     * = new GenericDeviceImpl(); TestService test = new TestService();
     * GenericDeviceActionImpl act = new GenericDeviceActionImpl();
     * act.setName("test"); test.putAction(act);
     * test.getParameter().setIntValue("math", 100); dev.putService(test);
     * String json =
     * dev.serialize(com.ericsson.deviceaccess.api.Constants.FORMAT_JSON);
     * System.out.println(json);
     *
     * GenericDevice dev2 = JSON.decode(json, GenericDeviceImpl.class);
     * GenericDeviceService test2 = dev2.getService("test");
     * assertEquals(test2.getName(), test.getName());
     * assertEquals(test2.getParameter().getStringValue("math"),
     * test.getParameter().getStringValue("math"));
     * assert(test2.getAction("test") != null);
     * assertEquals(test2.getAction("test").getName(),
     * test.getAction("test").getName()); }
     */

    /*
     * @Test public void testDecodeActionContext(){ String encoded =
     * "{\"action\":\"play\",\"arguments\":{\"requester\":\"kenta\",\"url\":\"\"},\"authorized\":true,\"device\":\"5855CA21A8B7\",\"executed\":false,\"failed\":false,\"firstTime\":true,\"messageThreadId\":24451742,\"owner\":\"kenta\",\"requester\":\"kenta\",\"requesterContact\":\"warp://erlabs:gateway/731/1314098276490/context/kenta/device/5855CA21A8B7/service/renderingControl/action/play\",\"result\":{\"code\":0,\"reason\":null,\"value\":{}},\"service\":\"renderingControl\"}"
     * ; GenericDeviceActionContextImpl cont = JSON.decode(encoded,
     * GenericDeviceActionContextImpl.class); }
     */
    class TestService extends GDServiceImpl {

        TestService() {
            super("test",
                    serviceSchema.getPropertiesSchemas());
            init();
        }

        private void init() {
            putAction(new TestAction());
        }

        class TestAction extends GDActionImpl {

            TestAction() {
                super("action",
                        serviceSchema.getActionSchemas().get(0).getArgumentsSchemas(),
                        serviceSchema.getActionSchemas().get(0).getResultSchema());
            }
        }
    }
}
