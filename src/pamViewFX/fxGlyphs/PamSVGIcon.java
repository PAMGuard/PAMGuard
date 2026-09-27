package pamViewFX.fxGlyphs;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/**
 * Load SVG icons. These are preferable to images because they scale nicely between different resolution
 * displays. code adapted from https://github.com/DeskChan/DeskChan
 * 
 * <p>
 * Note on speed: This can be very slow loading icons. It seems that this is due to the metadata at the start of the SVG
 * file. Tried with more SVG focused builders but this leads to dependency issues with org.w3c.dom which is very 
 * hard to sort out. The fix is simply to replace the metadata at the start of the SVG file with generic metadata that
 * loads fast. Example which works:
 * <p>
 * <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
	 viewBox="0 0 512 512" style="enable-background:new 0 0 512 512;" xml:space="preserve">
 *<p>
 *This is a hack rather than a fix but works for now. 

 * @author JamieMacaulauy
 *
 */
public class PamSVGIcon {

	public static PamSVGIcon instance; 

	private String path = null;

	private XPathFactory xpf;

	private XPath xpath;

	private XPathExpression expression;

	public static boolean canRead(File path){
		return path.getName().endsWith(".svg");
	}

	public static PamSVGIcon getInstance() {
		if (instance ==null) {
			instance = new  PamSVGIcon(); 
		}
		return instance;
	}
	
	@Deprecated
	public PamSVGIcon create(URL path, Color color, double lineWidth) throws Exception {
		return create(path);
	}


	public PamSVGIcon create(URL path) throws Exception {

		//		System.out.println("Create icon start");

//		String col = PamUtilsFX.toRGBCode(color);

		//    	System.out.println("Create icon getDocument()");

		Document document = getDocument(path);

		//    	System.out.println("Create icon getMarginFromFile()");

		Insets margin = getMarginFromFile(document);

		//    	System.out.println("Create evaluate");

		NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

		//    	System.out.println("Create icon start: " + svgPaths.getLength());

		ArrayList<SVGPath> shapes = new ArrayList<>();
		for(int i=0; i<svgPaths.getLength(); i++) {
			try {
				SVGPath shape = new SVGPath();
				shape.setFillRule(FillRule.NON_ZERO);
				org.w3c.dom.Node node = svgPaths.item(i);
				NamedNodeMap map = node.getAttributes();

				// Get path data - either directly from 'd' attribute or by converting shape elements
				String pathData = getPathData(node);
				if (pathData == null || pathData.isEmpty()) {
					continue;
				}

				shape.setContent(pathData);

				//get the fx style from the svg data. 
				String style = convertShapeStyle(node); 

				shape.setStyle(style);

				shapes.add(shape);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//    	System.out.println("Create icon end: " + svgPaths.getLength());

		SVGPath[] shapesPaths = shapes.toArray(new SVGPath[shapes.size()]);

		String textStyle = getTextStyle(document);

		//    	System.out.println("Create icon finsihed");
		return new PamSVGIcon(shapesPaths, textStyle, margin, path);
	}

	/**
	 * Get the path data string for an SVG node. For path elements, this is
	 * simply the 'd' attribute. For other shape elements (line, rect, circle,
	 * ellipse, polygon, polyline), the geometry is converted to an equivalent
	 * path data string.
	 *
	 * @param node the SVG DOM node
	 * @return the path data string, or null if conversion is not possible
	 */
	private String getPathData(org.w3c.dom.Node node) {
		NamedNodeMap map = node.getAttributes();
		String nodeName = node.getNodeName();

		switch (nodeName) {
		case "path":
			if (map.getNamedItem("d") != null) {
				return map.getNamedItem("d").getTextContent();
			}
			return null;

		case "line": {
			double x1 = getDoubleAttr(map, "x1", 0);
			double y1 = getDoubleAttr(map, "y1", 0);
			double x2 = getDoubleAttr(map, "x2", 0);
			double y2 = getDoubleAttr(map, "y2", 0);
			return "M" + x1 + "," + y1 + " L" + x2 + "," + y2;
		}

		case "rect": {
			double x = getDoubleAttr(map, "x", 0);
			double y = getDoubleAttr(map, "y", 0);
			double w = getDoubleAttr(map, "width", 0);
			double h = getDoubleAttr(map, "height", 0);
			double rx = getDoubleAttr(map, "rx", 0);
			double ry = getDoubleAttr(map, "ry", 0);
			if (map.getNamedItem("rx") != null && map.getNamedItem("ry") == null) ry = rx;
			if (map.getNamedItem("ry") != null && map.getNamedItem("rx") == null) rx = ry;
			if (rx == 0 && ry == 0) {
				return "M" + x + "," + y
						+ " L" + (x + w) + "," + y
						+ " L" + (x + w) + "," + (y + h)
						+ " L" + x + "," + (y + h) + " Z";
			} else {
				rx = Math.min(rx, w / 2);
				ry = Math.min(ry, h / 2);
				return "M" + (x + rx) + "," + y
						+ " L" + (x + w - rx) + "," + y
						+ " A" + rx + "," + ry + " 0 0 1 " + (x + w) + "," + (y + ry)
						+ " L" + (x + w) + "," + (y + h - ry)
						+ " A" + rx + "," + ry + " 0 0 1 " + (x + w - rx) + "," + (y + h)
						+ " L" + (x + rx) + "," + (y + h)
						+ " A" + rx + "," + ry + " 0 0 1 " + x + "," + (y + h - ry)
						+ " L" + x + "," + (y + ry)
						+ " A" + rx + "," + ry + " 0 0 1 " + (x + rx) + "," + y + " Z";
			}
		}

		case "circle": {
			double cx = getDoubleAttr(map, "cx", 0);
			double cy = getDoubleAttr(map, "cy", 0);
			double r = getDoubleAttr(map, "r", 0);
			return "M" + (cx - r) + "," + cy
					+ " A" + r + "," + r + " 0 1 0 " + (cx + r) + "," + cy
					+ " A" + r + "," + r + " 0 1 0 " + (cx - r) + "," + cy + " Z";
		}

		case "ellipse": {
			double cx = getDoubleAttr(map, "cx", 0);
			double cy = getDoubleAttr(map, "cy", 0);
			double rx = getDoubleAttr(map, "rx", 0);
			double ry = getDoubleAttr(map, "ry", 0);
			return "M" + (cx - rx) + "," + cy
					+ " A" + rx + "," + ry + " 0 1 0 " + (cx + rx) + "," + cy
					+ " A" + rx + "," + ry + " 0 1 0 " + (cx - rx) + "," + cy + " Z";
		}

		case "polygon":
		case "polyline": {
			if (map.getNamedItem("points") == null) return null;
			String points = map.getNamedItem("points").getTextContent().trim();
			String[] coords = points.split("[\\s,]+");
			if (coords.length < 2) return null;
			StringBuilder sb = new StringBuilder();
			sb.append("M").append(coords[0]).append(",").append(coords[1]);
			for (int j = 2; j + 1 < coords.length; j += 2) {
				sb.append(" L").append(coords[j]).append(",").append(coords[j + 1]);
			}
			if ("polygon".equals(nodeName)) {
				sb.append(" Z");
			}
			return sb.toString();
		}

		default:
			return null;
		}
	}

	/**
	 * Get a double attribute value from an SVG element's attribute map.
	 * @param map the attribute map
	 * @param name the attribute name
	 * @param defaultVal the default value if the attribute is not present
	 * @return the double value
	 */
	private double getDoubleAttr(NamedNodeMap map, String name, double defaultVal) {
		if (map.getNamedItem(name) == null) return defaultVal;
		try {
			return Double.parseDouble(map.getNamedItem(name).getTextContent());
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	/*
	 * Convert SVG properties to an fx css style. Resolves attribute values 
	 * by checking the element's own attributes first, then any inline CSS 
	 * style attribute, then inherited attributes from parent elements.
	 */
	private String convertShapeStyle(org.w3c.dom.Node node) {

		// The SVG presentation attributes we care about
		String[] svgAttrs = {"fill", "stroke", "stroke-width", "stroke-linecap"};

		// Collect effective values: element attributes override inline style,
		// which overrides inherited parent attributes
		java.util.Map<String, String> resolved = new java.util.LinkedHashMap<>();

		for (String attr : svgAttrs) {
			String value = resolveAttribute(node, attr);
			if (value != null) {
				resolved.put(attr, value);
			}
		}

		// Build the JavaFX CSS style string
		StringBuilder style = new StringBuilder();
		for (java.util.Map.Entry<String, String> entry : resolved.entrySet()) {
			switch (entry.getKey()) {
			case "fill":
				style.append("-fx-fill: ").append(entry.getValue()).append(";");
				break;
			case "stroke":
				style.append("-fx-stroke: ").append(entry.getValue()).append(";");
				break;
			case "stroke-linecap":
				style.append("-fx-stroke-line-cap: ").append(entry.getValue()).append(";");
				break;
			case "stroke-width":
				style.append("-fx-stroke-width: ").append(entry.getValue()).append(";");
				break;
			}
		}

		return style.toString();
	}

	/**
	 * Resolve an SVG presentation attribute value for a node. Checks (in order):
	 * 1. Direct XML attribute on the element
	 * 2. Inline CSS style attribute on the element
	 * 3. Inherited from ancestor elements (walking up the DOM tree)
	 *
	 * @param node the SVG DOM node
	 * @param attrName the SVG attribute name (e.g. "fill", "stroke")
	 * @return the resolved value, or null if not found anywhere
	 */
	private String resolveAttribute(org.w3c.dom.Node node, String attrName) {
		org.w3c.dom.Node current = node;
		while (current != null && current.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
			NamedNodeMap attrs = current.getAttributes();
			if (attrs != null) {
				// Check direct XML attribute (only on the element itself, not parents, 
				// unless not found on the element)
				org.w3c.dom.Node attrNode = attrs.getNamedItem(attrName);
				if (attrNode != null) {
					return attrNode.getTextContent();
				}
				// Check inline style attribute
				org.w3c.dom.Node styleNode = attrs.getNamedItem("style");
				if (styleNode != null) {
					String val = getStyleProperty(styleNode.getTextContent(), attrName);
					if (val != null) {
						return val;
					}
				}
			}
			// Walk up to the parent to check for inherited attributes
			current = current.getParentNode();
		}
		return null;
	}

	/**
	 * Extract a property value from an inline CSS style string.
	 * @param styleStr the CSS style string, e.g. "fill:none;stroke-width:2.5"
	 * @param property the property name to look for
	 * @return the value, or null if not found
	 */
	private String getStyleProperty(String styleStr, String property) {
		if (styleStr == null || styleStr.isEmpty()) return null;
		String[] parts = styleStr.split(";");
		for (String part : parts) {
			part = part.trim();
			if (part.isEmpty()) continue;
			int colonIdx = part.indexOf(':');
			if (colonIdx < 0) continue;
			String name = part.substring(0, colonIdx).trim();
			if (name.equals(property)) {
				return part.substring(colonIdx + 1).trim();
			}
		}
		return null;
	}

	//	/**
	//	 * Get an SVG icon.
	//	 * @param resourcePath - the path from the src folder
	//	 * @return a node for the SVG icon. 
	//	 */
	//	public Node getSVGIcon(String resourcePath, int size) {
	//		try {
	//			PamSVGIcon svgsprite = PamSVGIcon.create(new File(getClass().getResource(resourcePath).toURI()));
	//			svgsprite.getSpriteNode().setStyle("-fx-text-color: black");				
	//			svgsprite.getSpriteNode().setStyle("-fx-fill: black");
	//			svgsprite.setFitHeight(size);
	//			svgsprite.setFitWidth(size);
	//			return svgsprite.getSpriteNode(); 
	//		}
	//		catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//		return null; 
	//	}

	protected static Document getDocument(URL path){
		try {

			//        	System.out.println("DocumentBuilderFactory.newInstance();");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setExpandEntityReferences(false);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setIgnoringComments(true);
			//            
			//            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(path);
			//            svgDocument = (SVGDocument) f.createDocument(..);

			//        	System.out.println("DocumentBuilderFactory.newDocumentBuilder();");

			//THIS BIT CAN BE VERY SLOW SOMETIMES...
			DocumentBuilder builder = factory.newDocumentBuilder();

			//        	System.out.println("DocumentBuilderFactory.parse();");

			//this takes a very long time!
			return builder.parse(path.toString());


		} catch (Exception e) {
			return null;
		}
	}


	private SVGPath[] svgParts;
	private double originWidth, originHeight;
	private Node sprite;
	private String contentStyke;
	private Insets margin;

	public PamSVGIcon(SVGPath[] shapes, String contentStyle, Insets margin, URL path) {
		this.sprite = new Group(shapes);
		this.contentStyke = contentStyle;
		this.margin = margin;
		svgParts = shapes;
		originWidth = getFitWidth();
		originHeight = getFitHeight();
		this.path = path != null ? path.toString() : null;
	}

	public PamSVGIcon() {
		xpf = XPathFactory.newInstance();
		xpath = xpf.newXPath();
		try {
			expression = xpath.compile("//path|//line|//rect|//circle|//ellipse|//polygon|//polyline");
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public double getOriginWidth(){
		return originWidth;
	}

	public double getOriginHeight(){
		return originHeight;
	}


	public void setFitWidth(double width)  {
		for (SVGPath path : svgParts)
			path.setScaleX(width / originWidth);
	}

	public void setFitHeight(double height){
		for (SVGPath path : svgParts)
			path.setScaleY(height / originHeight);
	}

	protected static Insets getMarginFromFile(Document document){
		Insets standard = new Insets(30, 30, 30, 30);
		if (document == null)
			return standard;

		try {
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xpath = xpf.newXPath();
			XPathExpression expression = xpath.compile("//margin");

			NamedNodeMap marginTags = ((NodeList) expression.evaluate(document, XPathConstants.NODESET)).item(0).getAttributes();
			return new Insets(
					Double.parseDouble(marginTags.getNamedItem("top").getTextContent()),
					Double.parseDouble(marginTags.getNamedItem("right").getTextContent()),
					Double.parseDouble(marginTags.getNamedItem("bottom").getTextContent()),
					Double.parseDouble(marginTags.getNamedItem("left").getTextContent())
					);
		} catch (Exception e) {
			return standard;
		}
	}

	public static String getTextStyle(Document document){
		String standard = "-fx-alignment: center; -fx-text-alignment: center; -fx-content-display: center;";
		if (document == null)
			return standard;

		try {
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xpath = xpf.newXPath();
			XPathExpression expression = xpath.compile("//text");

			NamedNodeMap colorTag = ((NodeList) expression.evaluate(document, XPathConstants.NODESET)).item(0).getAttributes();
			return convertStyle(colorTag.getNamedItem("style").getTextContent());
		} catch (Exception e) {
			return standard;
		}
	}

	protected static String convertStyle(String style){
		String[] styleLines = style.split(";");
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < styleLines.length; j++) {
			styleLines[j] = styleLines[j].trim();
			if (styleLines[j].length() == 0) continue;
			result.append("-fx-");
			result.append(styleLines[j].trim());
			result.append("; ");
		}
		return result.toString();
	}

	public String getSpritePath(){ return path; }

	public Node getSpriteNode(){ return sprite; }

	public double getFitWidth() {  return sprite.getLayoutBounds().getWidth();   }

	public double getFitHeight(){  return sprite.getLayoutBounds().getHeight();  }
}
