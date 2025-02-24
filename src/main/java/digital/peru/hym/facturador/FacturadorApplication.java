package digital.peru.hym.facturador;

import org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
// import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.zip.ZipEntry;
// import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@SpringBootApplication
public class FacturadorApplication {

    private final static String ruc = "20607599211";
    private static String codeDocument = "";
    private static String serieDocument = "";
    private static String numberDocument = "";
    private final static String userSol = "MODDATOS";
    private final static String passwordSol = "MODDATOS";
    private static String endPointSunat = "";
    private static String pfxPath = "";
    private final static String pfxPassword = "20607599211";
    private static String xmlPath = "";
    private static boolean invoice = true;
    private static boolean production = false;

    public static void main(String[] args) {
		SpringApplication.run(FacturadorApplication.class, args);
        // this.execute();
    }

    public void execute(){
        endPointSunat = production ? "https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService"
                : "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService";
        codeDocument = invoice ? "01" : "03";
        serieDocument = invoice ? "F001" : "B001";
        numberDocument = invoice ? "54127" : "9139";

        String XmlName = ruc + "-" + codeDocument + "-" + serieDocument + "-" + numberDocument;

        ClassLoader classLoader = FacturadorApplication.class.getClassLoader();
        URL resourceXml = classLoader.getResource(XmlName + ".xml");
        URL resourcePfx = classLoader.getResource("LLAMA-PE-CERTIFICADO-DEMO-20607599211.pfx");

        if (resourceXml == null) {
            System.out.println("XML file not found");
            return;
        }
        if (resourcePfx == null) {
            System.out.println("Pfx file not found");
            return;
        }
        pfxPath = resourcePfx.getPath();
        xmlPath = resourceXml.getPath();

        signXml(XmlName);
        generateZip(XmlName);
        String contentBase64 = convertBase64Zip(XmlName);
        SendSunat(XmlName, contentBase64);
    }

    private static void signXml(String XmlName){
        try {
            System.setProperty("org.jcp.xml.dsig.internal.dom.XMLDSigRI", "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI");
            Security.addProvider(new XMLDSigRI());

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(pfxPath), pfxPassword.toCharArray());
            String alias = keyStore.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, pfxPassword.toCharArray());
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setIgnoringComments(true);
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlPath));
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            Element ublExtensions = doc.createElement("ext:UBLExtensions");
            Element ublExtension = doc.createElement("ext:UBLExtension");
            Element extensionContent = doc.createElement("ext:ExtensionContent");
            ublExtension.appendChild(extensionContent);
            ublExtensions.appendChild(ublExtension);
            root.insertBefore(ublExtensions, root.getFirstChild());
            XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM", "XMLDSig");

            Reference reference = signatureFactory.newReference(
                    "",
                    signatureFactory.newDigestMethod(DigestMethod.SHA1, null),
                    Collections.singletonList(
                            signatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)
                    ),
                    null,
                    null
            );

            SignedInfo signedInfo = signatureFactory.newSignedInfo(
                    signatureFactory.newCanonicalizationMethod(
                            CanonicalizationMethod.INCLUSIVE,
                            (C14NMethodParameterSpec) null
                    ),
                    signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    Collections.singletonList(reference)
            );

            KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();
            X509Data x509Data = keyInfoFactory.newX509Data(Collections.singletonList(certificate));
            KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));

            DOMSignContext domSignContext = new DOMSignContext(privateKey, extensionContent);
            domSignContext.setDefaultNamespacePrefix("ds");
            XMLSignature signature = signatureFactory.newXMLSignature(signedInfo, keyInfo);
            signature.sign(domSignContext);

            removeLineBreaks(doc, "ds:SignatureValue");
            removeLineBreaks(doc, "ds:X509Certificate");

            FileOutputStream fos = new FileOutputStream(XmlName + ".xml");
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(doc), new StreamResult(fos));

            fos.close();

            System.out.println("XML firmado correctamente y guardado como" + XmlName +".xml ");


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private static void generateZip(String XmlName){
        try {
            FileOutputStream fos = new FileOutputStream(XmlName + ".zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(XmlName + ".xml");
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }

            zipOut.close();
            fis.close();
            fos.close();

            System.out.println("ZIP generado correctamente y guardado como " + XmlName +".zip ");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static String convertBase64Zip(String XmlName){
        try{
            byte[] fileBytes = Files.readAllBytes(Paths.get(XmlName +".zip"));
            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    private static void SendSunat(String XmlName, String contentBase64){
        try {
        String contentXml = generateSoap( XmlName + ".zip", contentBase64);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/xml; charset=utf-8"));
        headers.set("Accept", "text/xml");
        headers.setCacheControl("no-cache");
        headers.add("Pragma", "no-cache");
        headers.add("SOAPAction", "");
        headers.add("Content-Length", String.valueOf(contentXml.getBytes(StandardCharsets.UTF_8).length));

        HttpEntity<String> requestEntity = new HttpEntity<>(contentXml, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endPointSunat,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            System.out.println("HTTP Status: " + response.getStatusCode());
            System.out.println("Respuesta:\n" + response.getBody());

            parseSoapResponse(response.getBody(), XmlName);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static String generateSoap(String nameFile, String contentBase64){
        return
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<soapenv:Envelope " +
                        "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                        "    xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" " +
                        "    xmlns:ser=\"http://service.sunat.gob.pe\">\n" +
                        "  <soapenv:Header>\n" +
                        "    <wsse:Security>\n" +
                        "      <wsse:UsernameToken>\n" +
                        "        <wsse:Username>" + ruc + userSol + "</wsse:Username>\n" +
                        "        <wsse:Password>" + passwordSol + "</wsse:Password>\n" +
                        "      </wsse:UsernameToken>\n" +
                        "    </wsse:Security>\n" +
                        "  </soapenv:Header>\n" +
                        "  <soapenv:Body>\n" +
                        "    <ser:sendBill>\n" +
                        "      <fileName>" + nameFile + "</fileName>\n" +
                        "      <contentFile>" + contentBase64 + "</contentFile>\n" +
                        "    </ser:sendBill>\n" +
                        "  </soapenv:Body>\n" +
                        "</soapenv:Envelope>";
    }

    private static void parseSoapResponse(String soapResponse, String XmlName) {
        try {
            String resourcePath = new File("src/main/resources/static/").getAbsolutePath();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(soapResponse.getBytes(StandardCharsets.UTF_8)));
            NodeList appRespNodes = doc.getElementsByTagName("applicationResponse");
            if (appRespNodes.getLength() > 0 && appRespNodes.item(0).getTextContent() != null) {
                String cdrBase64 = appRespNodes.item(0).getTextContent();
                byte[] cdrBytes = Base64.getDecoder().decode(cdrBase64);

                String zipFilePath = "R-" + XmlName + ".zip";
                Files.write(Path.of((resourcePath + "/" + zipFilePath)), cdrBytes);
//                unzipAndFormatXML(zipFilePath, "R-" + XmlName);
                System.out.println("FACTURA ENVIADA CORRECTAMENTE");
            } else {
                NodeList faultCodeNodes = doc.getElementsByTagName("faultcode");
                NodeList faultStringNodes = doc.getElementsByTagName("faultstring");
                String codigo = faultCodeNodes.getLength() > 0 ? faultCodeNodes.item(0).getTextContent() : "N/A";
                String mensaje = faultStringNodes.getLength() > 0 ? faultStringNodes.item(0).getTextContent() : "Error desconocido";
                System.out.println("Error " + codigo + ": " + mensaje);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // private static void unzipAndFormatXML(String zipFilePath, String outputDir) throws IOException {
    //     try {
    //         File destDir = new File(outputDir);
    //         if (!destDir.exists()) destDir.mkdirs();

    //         try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
    //             ZipEntry entry;
    //             while ((entry = zis.getNextEntry()) != null) {
    //                 String fileName = entry.getName();
    //                 File newFile = new File(destDir, fileName);

    //                 try (FileOutputStream fos = new FileOutputStream(newFile)) {
    //                     byte[] buffer = new byte[1024];
    //                     int len;
    //                     while ((len = zis.read(buffer)) > 0) {
    //                         fos.write(buffer, 0, len);
    //                     }
    //                 }
    //                 zis.closeEntry();

    //                 if (fileName.endsWith(".xml")) {
    //                     formatXML(newFile);
    //                 }

    //                 System.out.println("Archivo extraído: " + newFile.getAbsolutePath());
    //             }
    //         }
    //     } catch (Exception e) {
    //         System.out.println("Error: " + e.getMessage());
    //     }
    // }

    // private static void formatXML(File xmlFile) {
    //     try {
    //         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    //         DocumentBuilder db = dbf.newDocumentBuilder();
    //         Document doc = db.parse(xmlFile);

    //         TransformerFactory tf = TransformerFactory.newInstance();
    //         Transformer transformer = tf.newTransformer();
    //         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    //         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    //         DOMSource source = new DOMSource(doc);
    //         StreamResult result = new StreamResult(xmlFile);
    //         transformer.transform(source, result);
    //         System.out.println("XML formateado correctamente: " + xmlFile.getAbsolutePath());
    //     } catch (Exception e) {
    //         System.out.println("Error formateando XML: " + xmlFile.getName());
    //     }
    // }

    private static void removeLineBreaks(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node != null && node.getTextContent() != null) {
                String nonSkipContent = node.getTextContent().replaceAll("\\s+", "").trim();
                node.setTextContent(nonSkipContent);
            }
        }
    }

}
