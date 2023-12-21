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

	public PamSVGIcon create(URL path, Color color, double lineWidth) throws Exception {

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
				NamedNodeMap map = svgPaths.item(i).getAttributes();

//				System.out.println("Attributes: "  + map.getLength()); 
//				for (int ii=0; ii<map.getLength(); ii++) {
//					System.out.println(map.item(ii).getNodeName() +  "  " + map.item(ii).getFirstChild().getNodeValue());
//				}

				shape.setContent(map.getNamedItem("d").getTextContent());

				//get the fx style form the svg data. 
				String style = convertShapeStyle(map); 

				shape.setStyle(style);

				//				if(map.getNamedItem("style") != null) {
				//					shape.setStyle(convertStyle(map.getNamedItem("style").getTextContent()));
				//				} else {
				//					shape.setStyle("-fx-fill: red;" + "-fx-stroke-width: " + lineWidth + ";-fx-stroke: "+col);
				////					shape.setStyle("-fx-fill: "+col+"-fx-stroke: "+col);
				//				}
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

	
	/*
	 * Convert SVG properties to an fx css style. 
	 */
	private String convertShapeStyle(NamedNodeMap map) {

		//Example of attributes map in SVG. 
		//		fill  none
		//		opacity  1
		//		stroke  #000000
		//		stroke-linecap  round
		//		stroke-linejoin  round
		//		stroke-width  2.0144

		//"-fx-fill: red;" + "-fx-stroke-width: " + lineWidth + ";-fx-stroke: "+col

		String style = "";

		for (int ii=0; ii<map.getLength(); ii++) {
			String col;
//			System.out.println(map.item(ii).getNodeName() +  "  " + map.item(ii).getFirstChild().getNodeValue());
			switch (map.item(ii).getNodeName()) {
			
			case "fill":
				col = map.item(ii).getFirstChild().getNodeValue();
				style += "-fx-fill: " + col+";";
				break;
			case "stroke":
				col = map.item(ii).getFirstChild().getNodeValue();
				style += ("-fx-stroke: " + col+";");
				break;
			case "stroke-linecap":
				style += "-fx-stroke-line-cap: "+map.item(ii).getFirstChild().getNodeValue()+";";
				break;
			case "stroke-width":
				style += ("-fx-stroke-width: " +  map.item(ii).getFirstChild().getNodeValue()+";");
				break;

			}
		}

		return style;
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
			expression = xpath.compile("//path");
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