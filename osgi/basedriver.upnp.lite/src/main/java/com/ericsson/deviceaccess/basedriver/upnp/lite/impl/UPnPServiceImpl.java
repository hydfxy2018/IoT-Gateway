package com.ericsson.deviceaccess.basedriver.upnp.lite.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Vector;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import org.kxml2.io.KXmlParser;
import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPService;
import org.osgi.service.upnp.UPnPStateVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;

public class UPnPServiceImpl implements UPnPService {
	private static final Logger log = LoggerFactory.getLogger(UPnPServiceImpl.class);
	private UPnPDeviceImpl m_parent;
	protected String m_serviceType;
	protected String m_scpdUrl;
	protected String m_controlUrl;
	protected String m_eventSubUrl;
	protected String m_serviceId;
	protected HashMap m_actions = new HashMap();
	protected HashMap m_variables = new HashMap();
	private UPnPEventHandler m_eventHandler = null;
	protected Properties m_properties = new Properties();

	protected UPnPServiceImpl(UPnPDeviceImpl parent, String serviceType, String serviceId, String scpdUrl, String controlUrl, String eventSubUrl, UPnPEventHandler eventHandler) {
		m_parent = parent;
		m_controlUrl = controlUrl;
		m_serviceType = serviceType;
		m_eventSubUrl = eventSubUrl;
		m_serviceId = serviceId;
		m_scpdUrl = scpdUrl;
		m_eventHandler  = eventHandler;
		m_properties.put(UPnPDevice.TYPE, m_parent.getDescriptions(null).get(UPnPDevice.TYPE));
		m_properties.put(UPnPDevice.ID, m_parent.getUuid());
		m_properties.put(UPnPService.TYPE, m_serviceType);
		m_properties.put(UPnPService.ID, m_serviceId);
	}

	public String getId() {
		return m_serviceId;
	}

	public String getType() {
		return m_serviceType;
	}

	public String getVersion() {
		int idx = m_serviceType.lastIndexOf(':');
		if (idx > 0)
			return m_serviceType.substring(idx + 1);
		else
			return "1";
	}

	public UPnPAction getAction(String name) {
		return (UPnPAction) m_actions.get(name);
	}

	public UPnPAction[] getActions() {
		return (UPnPAction[]) m_actions.values().toArray(new UPnPAction[0]);
	}

	public UPnPStateVariable[] getStateVariables() {
		return null;
	}

	public UPnPStateVariable getStateVariable(String name) {
		return (UPnPStateVariable) m_variables.get(name);
	}
	
	protected UPnPDeviceImpl getDevice() {
		return m_parent;
	}

	protected void start() {
		// Get the Service XML from the device
		try {
			URL url = new URL(m_scpdUrl);
			XmlPullParser p = new KXmlParser();
			p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			p.setInput(url.openConnection().getInputStream(), null);

			while (p.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("actionList")) {
					parseActionList(p);
				} else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("serviceStateTable")) {
					parseStateVariables(p);
				}
				p.next();
			}
			
			m_eventHandler.registerService(this);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}
	
	protected void stop() {
		m_eventHandler.unregisterService(this);
	}

	private void parseStateVariables(XmlPullParser p) throws Exception {
		while (!(p.next() == XmlPullParser.END_TAG && p.getName().equalsIgnoreCase("serviceStateTable"))) {
			if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("stateVariable")) {
				String variableName = "";
				String dataType = "";
				boolean sendEvents = "yes".equals(p.getAttributeValue(null, "sendEvents"));
				Vector allowedValueList = new Vector();
				String[] allowedValues = null;
				Number maximum = null;
				Number minimum = null;
				Number step = null;
				String defaultValue = null;
				while (!(p.next() == XmlPullParser.END_TAG && p.getName().equalsIgnoreCase("stateVariable"))) {
					if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("name")) {
						variableName = p.nextText();
					} else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("dataType")) {
						dataType = p.nextText();
					} else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("defaultValue")) {
						defaultValue = p.nextText();
					} else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("allowedValueList")) {
						while (!(p.next() == XmlPullParser.END_TAG && p.getName().equalsIgnoreCase("allowedValueList"))) {
							if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("allowedValue")) {
								allowedValueList.add(p.nextText());
							}
						}
					} else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("allowedValueRange")) {
						while (!(p.next() == XmlPullParser.END_TAG && p.getName().equalsIgnoreCase("allowedValueRange"))) {
							if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("minimum")) {
								try {minimum = new Long(p.nextText());} catch (Exception e) {}
							} else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("maximum")) {
								try {maximum = new Long(p.nextText());} catch (Exception e) {}
							} else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("step")) {
								try {step = new Long(p.nextText());} catch (Exception e) {}
							}
						}
					}
				}

				if (allowedValueList.size() > 0)
					allowedValues = (String[]) allowedValueList.toArray(new String[0]);
				
				m_variables.put(variableName, new UPnPStateVariableImpl(variableName, dataType, sendEvents, allowedValues, maximum, minimum, step, defaultValue));
			}
		}
	}

	private void parseActionList(XmlPullParser p) throws Exception {
		while (!(p.next() == XmlPullParser.END_TAG && p.getName().equalsIgnoreCase("actionList"))) {
			if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("action")) {
				String actionName = "";
				LinkedHashMap inputArgumentNames = new LinkedHashMap();
				LinkedHashMap outputArgumentNames = new LinkedHashMap();
				while (!(p.next() == XmlPullParser.END_TAG && p.getName().equalsIgnoreCase("action"))) {
					if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("name")) {
						actionName = p.nextText();
					} else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("argument")) {
						String argName = "";
						String argDirection = "";
						String argStateVariable = "";
						while (!(p.next() == XmlPullParser.END_TAG && p.getName().equalsIgnoreCase("argument"))) {
							if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("name")) {
								argName = p.nextText();
							} else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("direction")) {
								argDirection = p.nextText();
							} else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equalsIgnoreCase("relatedStateVariable")) {
								argStateVariable = p.nextText();
							}
						}

						if ("in".equals(argDirection))
							inputArgumentNames.put(argName, argStateVariable);
						else
							outputArgumentNames.put(argName, argStateVariable);
					}
				}

				m_actions.put(actionName, new UPnPActionImpl(this, actionName, inputArgumentNames, outputArgumentNames));
			}
		}
	}

	protected String getEventUrl() {
		return m_eventSubUrl;
	}
	
	protected Properties getProperties() {
		return m_properties;
	}
}