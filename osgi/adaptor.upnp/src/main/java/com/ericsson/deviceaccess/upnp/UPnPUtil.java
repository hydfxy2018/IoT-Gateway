package com.ericsson.deviceaccess.upnp;

import org.apache.regexp.RE;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.osgi.service.upnp.UPnPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Dictionary;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

public class UPnPUtil {
	private static final Logger logger = LoggerFactory.getLogger(UPnPDeviceAgent.class);
    public static final String DEVICE_TYPE_MEDIA_RENDERER = "urn:schemas-upnp-org:device:MediaRenderer";
    public static final String DEVICE_TYPE_MEDIA_SERVER = "urn:schemas-upnp-org:device:MediaServer";
    public static final String DEVICE_TYPE_BINARY_LIGHT = "urn:schemas-upnp-org:device:BinaryLight";
    // public static final String SERVICE_ID_RENDERING_CONTROL =
    // "urn:upnp-org:serviceId:RenderingControlServiceID";
    public static final String SRV_RENDERING_CONTROL = "RenderingControl";
    public static final String SRV_AV_TRANSPORT = "AVTransport";
    public static final String SRV_CONTENT_DIRECTORY = "ContentDirectory";
    private static final String BROWSE_ACTION = "Browse";
    private static final String RESULT = "Result";
    static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static String getCurrentMediaUri(UPnPDevice dev) throws Exception {
        Properties args = new Properties();
        args.put("InstanceID", "0");
        Dictionary result = getUPnPAction(dev, "GetMediaInfo").invoke(args);
        if (result != null) {
            return (String) result.get("CurrentURI");
        }
        throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,
                "Could not get result for action");
    }

    public static void stopMedia(UPnPDevice dev) throws Exception {
        info("Going to stop playing on " + getFriendlyName(dev));
        Properties args = new Properties();
        args.put("InstanceID", "0");
        getUPnPAction(dev, "Stop").invoke(args);
    }

    public static void pauseMedia(UPnPDevice dev) throws Exception {
        Properties args = new Properties();
        args.put("InstanceID", "0");
        getUPnPAction(dev, "Pause").invoke(args);
    }

    public static void resumeMedia(UPnPDevice dev) throws Exception {
        Properties args = new Properties();
        args.put("InstanceID", "0");
        args.put("Speed", "1");
        getUPnPAction(dev, "Play").invoke(args);
    }

    public static String playMedia(UPnPDevice dev, String url) throws Exception {
        return playMedia(dev, url, "");
    }

    public static String playMedia(UPnPDevice dev, String url, String title) throws Exception {
        info("Going to play " + url + " on " + UPnPUtil.getFriendlyName(dev));
        url = url.trim();
        Properties args = new Properties();
        args.put("InstanceID", "0");
        args.put("CurrentURI", url);
        if (title == null) title = "";

        String contentType = getContentType(url);
        args.put("CurrentURIMetaData", getCurrentURIMetadata(url, title, contentType));
        debug(args.toString());

        getUPnPAction(dev, "SetAVTransportURI").invoke(args);
        debug("SetAVTransportURI successfully invoked");

        args = new Properties();
        args.put("InstanceID", "0");
        args.put("Speed", "1");
        getUPnPAction(dev, "Play").invoke(args);
        debug("Play successfully invoked");
        return contentType;
    }

    private static String getContentType(String url) throws UPnPUtilException {
        String contentType;
        try {
            contentType = HttpClient.getHeader(url, HEADER_CONTENT_TYPE);
            if (contentType == null || contentType.indexOf("/") < 0) {
                throw new UPnPUtilException(404, "Could not determine content type");
            }
            debug("Content type is " + contentType);
            if (contentType.indexOf(";") > 0) {
                /*
                     * remove optional data such as character encoding.
                     * e.g. video/mp4;UTF-8
                     */
                contentType = contentType.substring(0, contentType.indexOf(";"));
            }
        } catch (IOException e) {
            throw new UPnPUtilException(500, e.getMessage());
        }
        /*
           * Workaround for ustream mp4 file which returns audio/mp4
           * 20100917 Kenta
           */
        if (url.indexOf("ustream") > 0) {
            contentType = "video/mp4";
        }
        return contentType;
    }

    protected static String getCurrentURIMetadata(String url, String title, String contentType) throws UPnPUtilException {
        String itemType = contentType.substring(0, contentType.indexOf("/"));
        String md = "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">";
        md += "<item>";
        md += "<dc:title>" + title + "</dc:title>";
        md += "<upnp:class>object.item." + itemType + "Item</upnp:class>";
        md += "<res protocolInfo=\"http-get:*:" + contentType + ":*\">";
        md += url;
        md += "</res>";
        md += "</item>";
        md += "</DIDL-Lite>";
        return md;
    }

    protected static class HttpClient {

        public static String getHeader(String urlStr, String header) throws IOException {
            URL url = new URL(urlStr);

            HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
            urlconn.setRequestMethod("HEAD");
            urlconn.setInstanceFollowRedirects(true);

            urlconn.connect();
            String value = urlconn.getHeaderField(header);
            urlconn.disconnect();
            return value;
        }
    }

    public static void setVolume(UPnPDevice dev, String volume)
            throws Exception {
        Properties args = new Properties();
        args.put("InstanceID", "0");
        args.put("Channel", "Master");

        args.put("DesiredVolume", volume);
        getUPnPAction(dev, "SetVolume").invoke(args);

    }

    public static Dictionary browse(UPnPDevice dev, Properties givenArgs) throws UPnPException {
        UPnPAction action = getUPnPAction(dev, BROWSE_ACTION);
        if (action == null) throw new UPnPException(404, "No such action supported by " + getFriendlyName(dev));
        Properties args = getDefaultBrowseArguments();
        String[] argNames = action.getInputArgumentNames();
        for (int i = 0; i < argNames.length; i++) {
            if (givenArgs.containsKey(argNames[i])) {
                debug("Setting " + argNames[i] + " to " + givenArgs.get(argNames[i]));
                args.put(argNames[i], givenArgs.get(argNames[i]).toString());
            }
        }
        try {
            debug(args.toString());
            return action.invoke(args);
        } catch (Exception e) {
            String msg = ("Failed to invoke UPnP action." + e);
            error(msg);
            throw new UPnPException(500, msg);
        }
    }

    private static Properties getDefaultBrowseArguments() {
        Properties args = new Properties();
        args.put("ObjectID", "0");
        args.put("BrowseFlag", "BrowseDirectChildren");
        args.put("Filter", "*");
        args.put("StartingIndex", "0");
        args.put("RequestedCount", "0");
        args.put("SortCriteria", "");
        return args;
    }

    public static Properties parseLastChangeEvent(String value) {
        Properties result = new Properties();

        RE varRE = new RE("<(\\w+)\\s.*val=\"(.*)\".*/");
        String[] tags = new RE(">").split(value);

        for (int i = 0; i < tags.length; i++) {
            if (varRE.match(tags[i])) {
                result.setProperty(varRE.getParen(1), varRE.getParen(2));
            }
        }

        return result;
    }

    private static UPnPAction getUPnPAction(UPnPDevice device, String actionName)
            throws UPnPException {
        UPnPService[] services = device.getServices();

        for (int i = 0; i < services.length; ++i) {
            UPnPAction action = services[i].getAction(actionName);
            if (action != null) {
                /*
                     * log.debug("invoking " + action.getName() + " on " +
                     * device.getDescriptions(null).get( UPnPDevice.FRIENDLY_NAME));
                     */
                return action;
            }
        }
        throw new UPnPException(UPnPException.INVALID_ACTION,
                "No such action supported " + actionName);
    }

    public static String getUDN(UPnPDevice dev) {
        return getProperty(dev, UPnPDevice.UDN);
    }

    public static String getDeviceType(UPnPDevice dev) {
        return getProperty(dev, UPnPDevice.TYPE);
    }

    public static String getFriendlyName(UPnPDevice dev) {
        return getProperty(dev, UPnPDevice.FRIENDLY_NAME);
    }
    
    public static String getModelName(UPnPDevice dev) {
        return getProperty(dev, UPnPDevice.MODEL_NAME);
    }

    public static boolean isMediaRenderer(UPnPDevice dev) {
        String type = getDeviceType(dev);
        if (type != null && type.startsWith(DEVICE_TYPE_MEDIA_RENDERER)) {
            return true;
        }
        return false;
    }

    public static boolean isMediaServer(UPnPDevice dev) {
        String type = getDeviceType(dev);
        if (type != null && type.startsWith(DEVICE_TYPE_MEDIA_SERVER)) {
            return true;
        }
        return false;
    }

    public static boolean isDimmableLight(UPnPDevice dev) {
        String type = getDeviceType(dev);
        String model = getModelName(dev);
        if (type != null && type.startsWith(DEVICE_TYPE_BINARY_LIGHT) &&
        	model != null && model.startsWith("Intel")) { // "Intel CLR Emulated Light Bulb
            return true;
        }
        return false;
    }


    private static String getProperty(UPnPDevice dev, String name) {
        if (dev != null) {
            String value = (String) dev.getDescriptions(null).get(name);
            debug("Property " + name + " = " + value);
            return value;
        }
        return null;
    }

    public static UPnPDevice getTargetDevice(String uuid, BundleContext context)
            throws UPnPUtilException {
        if (uuid == null) {
            String msg = "Device UUID is not specified";
            throw new UPnPUtilException(400, msg);
        }
        UPnPDevice target;

        try {
            // String uuidFilter = "(" + UPnPDevice.UDN + "=" + uuid + ")";
            ServiceReference[] refs = context.getServiceReferences(
                    UPnPDevice.class.getName(), null);
            // context.getServiceReferences(UPnPDevice.class.getName(),
            // uuidFilter);

            if (refs != null) {
                for (int i = 0; i < refs.length; i++) {
                    UPnPDevice dev = (UPnPDevice) context.getService(refs[i]);
                    if (uuid.equalsIgnoreCase((String) dev
                            .getDescriptions(null).get(UPnPDevice.UDN))) {
                        return dev;
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new UPnPUtilException(500, e.getMessage());
        }
        String msg = "No such device found";
        throw new UPnPUtilException(404, msg);
    }

    private static void debug(String msg) {
        if (logger != null)
            logger.debug(msg);
    }

    private static void info(String msg) {
        if (logger != null)
            logger.info(msg);
    }

    private static void error(String msg) {
        if (logger != null)
            logger.error(msg);
    }
    
    static public String[] parseServiceType(String serviceType) {
    	return UPnPUtil.stringSplit(serviceType, ":");
    }
    
	static private String[] stringSplit(String str, String delimiter) {
		StringTokenizer tokenizer = new StringTokenizer(str, delimiter);
		Vector tokenList = new Vector();
		
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			if (token == null) {
				continue;
			}
			tokenList.add(token);
		}
		
		return (String[])tokenList.toArray(new String[0]);
	}
}
