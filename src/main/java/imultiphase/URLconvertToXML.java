package imultiphase;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class URLconvertToXML {
	public static boolean isPrintLog = false;
	public static boolean isPrintException = true;
	public static final String IP = "IP";
	public static final String LOCATION = "Location";
	public static final String TYPE = "Type";
	public static final String MESSAGE = "Message";
	public static final String FILE_PATH = "C:\\Temp\\";
	public static final String pattern = "yyyyMMdd_hhmm";

	public static void main(String[] arg) {
		try {
			if(arg != null && arg.length >= 2 && StringUtils.isNotBlank(arg[1])) {
				isPrintLog = StringUtils.equalsIgnoreCase("Y", arg[1]);
				printLog("[Start Application]");
			}
			// validation
			printLog("[Start validation]");
			if (arg == null || arg.length == 0 || StringUtils.isBlank(arg[0])) {
				throw new Exception("[20001]: input URL cannot be blank.");
			}
			printLog("arg.length"+arg.length);
			
			
			File folder = new File(FILE_PATH);
			if (!folder.exists()) {
				folder.mkdir();
				printLog(FILE_PATH + "is created.");
			}
			URLconvertToXML coreLogic = new URLconvertToXML();
			String query = coreLogic.validation(arg[0]);
			printLog("[End validation]");

			printLog("[Start Logic]");
			coreLogic.mainLogic(query);
			printLog("[End Logic]");
		} catch (Throwable ex) {
			printException(ex.getMessage());
		}

	}

	public String validation(String inputURLString) throws Throwable {
		URL url = null;
		String result = StringUtils.EMPTY;
		if (StringUtils.isBlank(inputURLString)) {
			throw new Exception("[20001]: input URL cannot be blank.");
		}
		try {
			url = new URL(inputURLString);
			result = url.getQuery();
			if (StringUtils.isBlank(result)) {
				throw new Exception("[20002]: input query cannot be blank.");
			}
			if (StringUtils.containsNone("IP", result.toUpperCase())
					|| StringUtils.containsNone("LOCATION", result.toUpperCase())
					|| StringUtils.containsNone("TYPE", result.toUpperCase())
					|| StringUtils.containsNone("MESSAGE", result.toUpperCase())) {
				throw new Exception("[20003]: IP/Location/Type/Message cannot be found.");
			}
		} catch (MalformedURLException me) {
			throw new Exception("[20002]: input URL is not vaild.");
		}
		return result;
	}

	public void mainLogic(String query) throws Throwable {
		Map<String, String> queryMap = getQueryMap(query);
		String ipValue = queryMap.get(IP.toUpperCase());
		String locationValue = queryMap.get(LOCATION.toUpperCase());
		String typeValue = queryMap.get(TYPE.toUpperCase());
		String messageValue = queryMap.get(MESSAGE.toUpperCase());
		this.makeXML(ipValue, locationValue, typeValue, messageValue);
	}

	public Map<String, String> getQueryMap(String query) {
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();

		for (String param : params) {
			String name = param.split("=")[0].toUpperCase();
			String value = param.split("=")[1];
			printLog("name:" + name + ", value:" + value);
			map.put(name, value);
		}
		return map;
	}

	public static void printLog(String msg) {
		if (isPrintLog) {
			System.out.println(msg);
		}
	}

	public static void printException(String msg) {
		if (isPrintException) {
			System.err.println(msg);
		}
	}

	public void makeXML(String ipValue, String locationValue, String typeValue, String messageValue) throws Throwable {
		try {
			printLog("ipValue:" + ipValue + ", locationValue:" + locationValue + ", typeValue:" + typeValue
					+ ", messageValue:" + messageValue);
			if (StringUtils.isBlank(ipValue) || StringUtils.isBlank(locationValue) || StringUtils.isBlank(typeValue)
					|| StringUtils.isBlank(messageValue)) {
				throw new Exception("[20003]: IP/Location/Type/Message cannot be found.");
			}
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("BSHEvent");
			doc.appendChild(rootElement);
			Element detail = doc.createElement("Detail");
			rootElement.appendChild(detail);
			Element ip = doc.createElement(IP);
			ip.appendChild(doc.createTextNode(ipValue));
			detail.appendChild(ip);
			Element location = doc.createElement(LOCATION);
			location.appendChild(doc.createTextNode(locationValue));
			detail.appendChild(location);
			Element type = doc.createElement(TYPE);
			type.appendChild(doc.createTextNode(typeValue));
			detail.appendChild(type);
			Element message = doc.createElement(MESSAGE);
			message.appendChild(doc.createTextNode(messageValue));
			detail.appendChild(message);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			printLog("XML result:" + source.toString());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			String filename = simpleDateFormat.format(new Date());
			printLog("filename :" + filename);
			File resultFile = new File(FILE_PATH + filename);
			if (!resultFile.exists()) {
				StreamResult result = new StreamResult(resultFile);
				transformer.transform(source, result);
				printLog("File saved!");
			} else {
				printLog("File is already exist!");
			}
		} catch (ParserConfigurationException pce) {
			throw new Exception("[99991] File creation failure");
		} catch (TransformerException tfe) {
			throw new Exception("[99992] File creation failure");
		}
	}

}
