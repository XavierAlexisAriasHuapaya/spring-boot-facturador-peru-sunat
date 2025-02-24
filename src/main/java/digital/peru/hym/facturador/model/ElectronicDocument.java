package digital.peru.hym.facturador.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class ElectronicDocument {

    private String serie_document;

    private String number_documento;

    private String issue_date;

    private String issue_time;

    private String operation_type_code;

    private String document_type_code;

    private String currency_type_code;

    private String due_date;

    private String purchase_order_number;

    private ElectronicDocumentCustomer customer_or_company_name;

    private ElectronicDocumentTotal totales;

    private List<ElectronicDocumentTotal> items;

    private String additional_information;

}
