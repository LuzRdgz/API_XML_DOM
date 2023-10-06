import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

public class sales {

    static final String CLASS_NAME = sales.class.getSimpleName();
    static final Logger LOG = Logger.getLogger(CLASS_NAME);
    static Scanner scan = new Scanner(System.in);
    static String depto;
    static int por;

    public static void main(String[] args) {
        if (args.length != 1) {
            LOG.severe("Falta archivo XML en argumentos.");
            System.exit(1);
        }

        System.out.println("-> Indique el departamento:");
        depto = scan.nextLine();
        System.out.println("-> De cuanto es el incremento (5% - 15%)?");
        por = scan.nextInt();
        System.out.println("================================================================");
        System.out.println("Incremento del " + por + "% en las ventas del Departamento " + depto);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(args[0]));

            doc.getDocumentElement().normalize();

            reporteVentas(doc);
            saveDocument(doc, "new_sales.xml");

        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.severe(e.getMessage());
        }
    }
    public static void reporteVentas(Document doc) {

        Element root = doc.getDocumentElement();

        NodeList salesData = root.getElementsByTagName("sale_record");

        int n = salesData.getLength();

        HashMap<String,Double> ventasDepto = new HashMap<>();

        String sales;
        String department;


        for (int index = 0; index < n; index++) {
            Node node = salesData.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element) node;

                sales = element.getElementsByTagName("sales").item(0).getTextContent();
                department = element.getElementsByTagName("department").item(0).getTextContent();

                double val = Double.parseDouble(sales);

                if( ventasDepto.containsKey(department) ) {
                    double x = ventasDepto.get(department);

                    ventasDepto.put(department, ((val + x)+((val + x)*por/100)));
                } else {
                    ventasDepto.put(department,val);
                }
            }
        }
        if (ventasDepto.containsKey(depto)){
            System.out.printf("%-15.15s %,7.2f \n", depto ,ventasDepto.get(depto));
        }
    }

    public static void saveDocument(Document doc, String file) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);

            FileWriter writer = new FileWriter(file);
            StreamResult result = new StreamResult(writer);

            transformer.transform(source, result);

        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }
    }
}
