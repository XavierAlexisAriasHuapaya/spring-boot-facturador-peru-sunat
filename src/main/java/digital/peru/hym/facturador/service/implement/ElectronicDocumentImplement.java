package digital.peru.hym.facturador.service.implement;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Service;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital.peru.hym.facturador.model.ElectronicDocument;
import digital.peru.hym.facturador.service.interfaces.ElectronicDocumentService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ElectronicDocumentImplement implements ElectronicDocumentService {

    @Override
    public void create(ElectronicDocument electronicDocument) {
        try {
            // Crear el documento XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element invoice = document.createElement("Invoice");
            invoice.setAttribute("xmlns", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2");
            invoice.setAttribute("xmlns:cac",
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
            invoice.setAttribute("xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
            invoice.setAttribute("xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
            invoice.setAttribute("xmlns:ext",
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
            document.appendChild(invoice);

            Element UBLVersionID = document.createElement("cbc:UBLVersionID");
            UBLVersionID.appendChild(document.createTextNode("2.1"));
            invoice.appendChild(UBLVersionID);

            Element CustomizationID = document.createElement("cbc:CustomizationID");
            CustomizationID.appendChild(document.createTextNode("2.0"));
            invoice.appendChild(CustomizationID);

            Element Id = document.createElement("cbc:ID");
            Id.appendChild(document.createTextNode(
                    electronicDocument.getSerie_document() + "-" + electronicDocument.getNumber_documento()));
            invoice.appendChild(Id);

            Element IssueDate = document.createElement("cbc:IssueDate");
            IssueDate.appendChild(document.createTextNode(
                    electronicDocument.getIssue_date()));
            invoice.appendChild(IssueDate);

            Element IssueTime = document.createElement("cbc:IssueTime");
            IssueTime.appendChild(document.createTextNode(
                    electronicDocument.getIssue_time()));
            invoice.appendChild(IssueTime);

            Element DueDate = document.createElement("cbc:DueDate");
            DueDate.appendChild(document.createTextNode(
                    electronicDocument.getDue_date()));
            invoice.appendChild(DueDate);

            Element InvoiceTypeCode = document.createElement("cbc:InvoiceTypeCode");
            InvoiceTypeCode.setAttribute("listID", "0101");
            InvoiceTypeCode.appendChild(document.createTextNode(
                    electronicDocument.getDocument_type_code()));
            invoice.appendChild(InvoiceTypeCode);

            Element Note = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:Note");
            Note.setAttribute("languageLocaleID", "1000");
            CDATASection cdataSection = document.createCDATASection("Cien  con 00/100");
            Note.appendChild(cdataSection);
            invoice.appendChild(Note);

            Element DocumentCurrencyCode = document.createElement("cbc:DocumentCurrencyCode");
            DocumentCurrencyCode.appendChild(document.createTextNode(
                    electronicDocument.getCurrency_type_code()));
            invoice.appendChild(DocumentCurrencyCode);

            Element Signature = document.createElement("cac:Signature");
            invoice.appendChild(Signature);

            Id = document.createElement("cbc:ID");
            Id.appendChild(document.createTextNode("signatureDIGITALPERU"));
            Signature.appendChild(Id);

            Note = document.createElement("cbc:Note");
            Note.appendChild(document.createTextNode("FACTURALO"));
            Signature.appendChild(Note);

            Element SignatoryParty = document.createElement("cac:SignatoryParty");
            Signature.appendChild(SignatoryParty);

            Element PartyIdentification = document.createElement("cac:PartyIdentification");
            SignatoryParty.appendChild(PartyIdentification);

            Id = document.createElement("cbc:ID");
            Id.appendChild(document.createTextNode("20607599211"));
            PartyIdentification.appendChild(Id);

            Element PartyName = document.createElement("cac:PartyName");
            SignatoryParty.appendChild(PartyName);

            Element Name = document.createElement("cbc:Name");
            cdataSection = document.createCDATASection("Digital Perú");
            Name.appendChild(cdataSection);
            PartyName.appendChild(Name);

            Element DigitalSignatureAttachment = document.createElement("cac:DigitalSignatureAttachment");
            Signature.appendChild(DigitalSignatureAttachment);

            Element ExternalReference = document.createElement("cac:ExternalReference");
            DigitalSignatureAttachment.appendChild(ExternalReference);

            Element URI = document.createElement("cbc:URI");
            URI.appendChild(document.createTextNode("#signatureDIGITALPERU"));
            ExternalReference.appendChild(URI);

            Element AccountingSupplierParty = document.createElement("cac:AccountingSupplierParty");
            invoice.appendChild(AccountingSupplierParty);

            Element Party = document.createElement("cac:Party");
            AccountingSupplierParty.appendChild(Party);

            PartyIdentification = document.createElement("cac:PartyIdentification");
            Party.appendChild(PartyIdentification);

            Id = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:ID");
            Id.setAttribute("schemeID", "6");
            Id.appendChild(document.createTextNode("20607599211"));
            PartyIdentification.appendChild(Id);

            PartyName = document.createElement("cac:PartyName");
            Party.appendChild(PartyName);

            Name = document.createElement("cbc:Name");
            cdataSection = document.createCDATASection("Digital Perú");
            Name.appendChild(cdataSection);
            PartyName.appendChild(Name);

            Element PartyLegalEntity = document.createElement("cac:PartyLegalEntity");
            Party.appendChild(PartyLegalEntity);

            Element RegistrationName = document.createElement("cbc:RegistrationName");
            cdataSection = document.createCDATASection("Digital Perú");
            RegistrationName.appendChild(cdataSection);
            PartyLegalEntity.appendChild(RegistrationName);

            Element RegistrationAddress = document.createElement("cac:RegistrationAddress");
            PartyLegalEntity.appendChild(RegistrationAddress);

            Id = document.createElement("cbc:ID");
            Id.appendChild(document.createTextNode("140101"));
            RegistrationAddress.appendChild(Id);

            Element AddressTypeCode = document.createElement("cbc:AddressTypeCode");
            AddressTypeCode.appendChild(document.createTextNode("0000"));
            RegistrationAddress.appendChild(AddressTypeCode);

            Element CityName = document.createElement("cbc:CityName");
            CityName.appendChild(document.createTextNode("Chiclayo"));
            RegistrationAddress.appendChild(CityName);

            Element CountrySubentity = document.createElement("cbc:CountrySubentity");
            CountrySubentity.appendChild(document.createTextNode("LAMBAYEQUE"));
            RegistrationAddress.appendChild(CountrySubentity);

            Element District = document.createElement("cbc:District");
            District.appendChild(document.createTextNode("Chiclayo"));
            RegistrationAddress.appendChild(District);

            Element AddressLine = document.createElement("cac:AddressLine");
            RegistrationAddress.appendChild(AddressLine);

            Element Line = document.createElement("cbc:Line");
            cdataSection = document.createCDATASection("Jr. Conde de la Vega Baja #455");
            Line.appendChild(cdataSection);
            AddressLine.appendChild(Line);

            Element Country = document.createElement("cac:Country");
            RegistrationAddress.appendChild(Country);

            Element IdentificationCode = document.createElement("cbc:IdentificationCode");
            IdentificationCode.appendChild(document.createTextNode("PE"));
            Country.appendChild(IdentificationCode);

            Element Contact = document.createElement("cac:Contact");
            Party.appendChild(Contact);

            Element Telephone = document.createElement("cbc:Telephone");
            Telephone.appendChild(document.createTextNode("961842760"));
            Contact.appendChild(Telephone);

            Element ElectronicMail = document.createElement("cbc:ElectronicMail");
            ElectronicMail.appendChild(document.createTextNode("alexis@gmail.com"));
            Contact.appendChild(ElectronicMail);

            Element AccountingCustomerParty = document.createElement("cac:AccountingCustomerParty");
            invoice.appendChild(AccountingCustomerParty);

            Party = document.createElement("cac:Party");
            AccountingCustomerParty.appendChild(Party);

            PartyIdentification = document.createElement("cac:PartyIdentification");
            Party.appendChild(PartyIdentification);

            Id = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:ID");
            Id.setAttribute("schemeID", "6");
            Id.appendChild(document.createTextNode("20607599211"));
            PartyIdentification.appendChild(Id);

            PartyLegalEntity = document.createElement("cac:PartyLegalEntity");
            Party.appendChild(PartyLegalEntity);

            RegistrationName = document.createElement("cbc:RegistrationName");
            cdataSection = document.createCDATASection("CORPORACION GMA LENT&#039;S S.R.L.");
            RegistrationName.appendChild(cdataSection);
            PartyLegalEntity.appendChild(RegistrationName);

            RegistrationAddress = document.createElement("cac:RegistrationAddress");
            PartyLegalEntity.appendChild(RegistrationAddress);

            Id = document.createElement("cbc:ID");
            Id.appendChild(document.createTextNode("060801"));
            RegistrationAddress.appendChild(Id);

            AddressLine = document.createElement("cac:AddressLine");
            RegistrationAddress.appendChild(AddressLine);

            Line = document.createElement("cbc:Line");
            cdataSection = document.createCDATASection("CAL. VILLANUEVA PINILLOS NRO. 135 INT. 1 CENTRO DE JAEN");
            Line.appendChild(cdataSection);
            AddressLine.appendChild(Line);

            Country = document.createElement("cac:Country");
            RegistrationAddress.appendChild(Country);

            IdentificationCode = document.createElement("cbc:IdentificationCode");
            IdentificationCode.appendChild(document.createTextNode("PE"));
            Country.appendChild(IdentificationCode);

            Element PaymentTerms = document.createElement("cac:PaymentTerms");
            invoice.appendChild(PaymentTerms);

            Id = document.createElement("cbc:ID");
            Id.appendChild(document.createTextNode("FormaPago"));
            PaymentTerms.appendChild(Id);

            Element PaymentMeansID = document.createElement("cbc:PaymentMeansID");
            PaymentMeansID.appendChild(document.createTextNode("Contado"));
            PaymentTerms.appendChild(PaymentMeansID);

            Element TaxTotal = document.createElement("cac:TaxTotal");
            invoice.appendChild(TaxTotal);

            Element TaxAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:TaxAmount");
            TaxAmount.setAttribute("currencyID", "PEN");
            TaxAmount.appendChild(document.createTextNode("15.25"));
            TaxTotal.appendChild(TaxAmount);

            Element TaxSubtotal = document.createElement("cac:TaxSubtotal");
            TaxTotal.appendChild(TaxSubtotal);

            Element TaxableAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:TaxableAmount");
            TaxableAmount.setAttribute("currencyID", "PEN");
            TaxableAmount.appendChild(document.createTextNode("84.75"));
            TaxSubtotal.appendChild(TaxableAmount);

            TaxAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:TaxAmount");
            TaxAmount.setAttribute("currencyID", "PEN");
            TaxAmount.appendChild(document.createTextNode("15.25"));
            TaxSubtotal.appendChild(TaxAmount);

            Element TaxCategory = document.createElement("cac:TaxCategory");
            TaxSubtotal.appendChild(TaxCategory);
            Element TaxScheme = document.createElement("cac:TaxScheme");
            TaxCategory.appendChild(TaxScheme);

            Id = document.createElement("cbc:ID");
            Id.appendChild(document.createTextNode("1000"));
            TaxScheme.appendChild(Id);

            Name = document.createElement("cbc:Name");
            Name.appendChild(document.createTextNode("IGV"));
            TaxScheme.appendChild(Name);

            Element TaxTypeCode = document.createElement("cbc:TaxTypeCode");
            TaxTypeCode.appendChild(document.createTextNode("VAT"));
            TaxScheme.appendChild(TaxTypeCode);

            Element LegalMonetaryTotal = document.createElement("cac:LegalMonetaryTotal");
            invoice.appendChild(LegalMonetaryTotal);

            Element LineExtensionAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:LineExtensionAmount");
            LineExtensionAmount.setAttribute("currencyID", "PEN");
            LineExtensionAmount.appendChild(document.createTextNode("84.75"));
            LegalMonetaryTotal.appendChild(LineExtensionAmount);

            Element TaxInclusiveAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:TaxInclusiveAmount");
            TaxInclusiveAmount.setAttribute("currencyID", "PEN");
            TaxInclusiveAmount.appendChild(document.createTextNode("100.00"));
            LegalMonetaryTotal.appendChild(TaxInclusiveAmount);

            Element PayableAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:PayableAmount");
            PayableAmount.setAttribute("currencyID", "PEN");
            PayableAmount.appendChild(document.createTextNode("100.00"));
            LegalMonetaryTotal.appendChild(PayableAmount);

            Element InvoiceLine = document.createElement("cac:InvoiceLine");
            invoice.appendChild(InvoiceLine);

            Id = document.createElement("cbc:ID");
            Id.appendChild(document.createTextNode("1"));
            InvoiceLine.appendChild(Id);

            Element InvoicedQuantity = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:InvoicedQuantity");
            InvoicedQuantity.setAttribute("unitCode", "NIU");
            InvoicedQuantity.appendChild(document.createTextNode("1.0000"));
            InvoiceLine.appendChild(InvoicedQuantity);

            LineExtensionAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:LineExtensionAmount");
            LineExtensionAmount.setAttribute("currencyID", "PEN");
            LineExtensionAmount.appendChild(document.createTextNode("84.75"));
            InvoiceLine.appendChild(LineExtensionAmount);

            Element PricingReference = document.createElement("cac:PricingReference");
            InvoiceLine.appendChild(PricingReference);

            Element AlternativeConditionPrice = document.createElement("cac:AlternativeConditionPrice");
            PricingReference.appendChild(AlternativeConditionPrice);

            Element PriceAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:PriceAmount");
            PriceAmount.setAttribute("currencyID", "PEN");
            PriceAmount.appendChild(document.createTextNode("100.000000"));
            AlternativeConditionPrice.appendChild(PriceAmount);

            Element PriceTypeCode = document.createElement("cbc:PriceTypeCode");
            PriceTypeCode.appendChild(document.createTextNode("01"));
            AlternativeConditionPrice.appendChild(PriceTypeCode);

            TaxTotal = document.createElement("cac:TaxTotal");
            InvoiceLine.appendChild(TaxTotal);

            TaxAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:TaxAmount");
            TaxAmount.setAttribute("currencyID", "PEN");
            TaxAmount.appendChild(document.createTextNode("15.25"));
            TaxTotal.appendChild(TaxAmount);

            TaxSubtotal = document.createElement("cac:TaxSubtotal");
            TaxTotal.appendChild(TaxSubtotal);

            TaxableAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:TaxableAmount");
            TaxableAmount.setAttribute("currencyID", "PEN");
            TaxableAmount.appendChild(document.createTextNode("84.75"));
            TaxSubtotal.appendChild(TaxableAmount);

            TaxAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:TaxAmount");
            TaxAmount.setAttribute("currencyID", "PEN");
            TaxAmount.appendChild(document.createTextNode("15.25"));
            TaxSubtotal.appendChild(TaxAmount);

            TaxCategory = document.createElement("cac:TaxCategory");
            TaxSubtotal.appendChild(TaxCategory);

            Element Percent = document.createElement("cbc:Percent");
            Percent.appendChild(document.createTextNode("18.00"));
            TaxCategory.appendChild(Percent);

            Element TaxExemptionReasonCode = document.createElement("cbc:TaxExemptionReasonCode");
            TaxExemptionReasonCode.appendChild(document.createTextNode("10"));
            TaxCategory.appendChild(TaxExemptionReasonCode);

            TaxScheme = document.createElement("cac:TaxScheme");
            TaxCategory.appendChild(TaxScheme);

            Id = document.createElement("cbc:ID");
            Id.appendChild(document.createTextNode("1000"));
            TaxScheme.appendChild(Id);

            Name = document.createElement("cbc:Name");
            Name.appendChild(document.createTextNode("IGV"));
            TaxScheme.appendChild(Name);

            TaxTypeCode = document.createElement("cbc:TaxTypeCode");
            TaxTypeCode.appendChild(document.createTextNode("VAT"));
            TaxScheme.appendChild(TaxTypeCode);

            Element Item = document.createElement("cac:Item");
            InvoiceLine.appendChild(Item);

            Element Description = document.createElement("cbc:Description");
            cdataSection = document.createCDATASection("BIOGROW CITONE 1 LT");
            Description.appendChild(cdataSection);
            Item.appendChild(Description);

            Element SellersItemIdentification = document.createElement("cac:SellersItemIdentification");
            Item.appendChild(SellersItemIdentification);

            Id = document.createElement("cbc:ID");
            Id.appendChild(document.createTextNode("V075"));
            SellersItemIdentification.appendChild(Id);

            Element Price = document.createElement("cac:Price");
            InvoiceLine.appendChild(Price);

            PriceAmount = document.createElementNS(
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "cbc:PriceAmount");
            PriceAmount.setAttribute("currencyID", "PEN");
            PriceAmount.appendChild(document.createTextNode("84.745763"));
            Price.appendChild(PriceAmount);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Formatear el XML
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File("empleados.xml"));
            transformer.transform(source, result);



            System.out.println("Archivo XML generado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
