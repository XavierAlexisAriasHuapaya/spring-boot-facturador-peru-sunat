package digital.peru.hym.facturador.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class ElectronicDocumentItem {

    private String internal_code;

    private String description;

    private String unit_of_measurement;

    private String amount;

    private BigDecimal unit_value;

    private String price_type_code;

    private BigDecimal unit_price;

    private String code_type_igv_affectation;

    private BigDecimal total_base_igv;

    private BigDecimal igv_percentage;

    private BigDecimal total_igv;

    private BigDecimal total_taxes;

    private BigDecimal total_item_value;

    private BigDecimal total_item;

}
