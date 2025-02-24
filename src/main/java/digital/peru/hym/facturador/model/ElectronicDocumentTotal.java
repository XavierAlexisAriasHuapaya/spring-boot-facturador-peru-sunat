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
public class ElectronicDocumentTotal {

    private BigDecimal total_export;
    
    private BigDecimal total_taxed_transactions;

    private BigDecimal total_unaffected_operations;

    private BigDecimal total_operaciones_exoneradas;

    private BigDecimal total_free_operations;

    private BigDecimal total_igv;

    private BigDecimal total_taxes;

    private BigDecimal total_value;

    private BigDecimal total_sale;

    private BigDecimal subtotal_venta;

    private BigDecimal total_igv_free_operations;

}
