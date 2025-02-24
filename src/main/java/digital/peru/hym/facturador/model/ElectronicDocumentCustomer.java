package digital.peru.hym.facturador.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class ElectronicDocumentCustomer {
    
    private String identity_document_type_code;

    private String document_number;

    private String name_company_name;

    private String country_code;

    private String ubigeo;

    private String address;

    private String email;

    private String phone;
}
