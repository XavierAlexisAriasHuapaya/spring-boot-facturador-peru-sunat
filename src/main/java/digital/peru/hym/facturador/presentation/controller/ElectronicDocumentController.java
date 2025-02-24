package digital.peru.hym.facturador.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import digital.peru.hym.facturador.model.ElectronicDocument;
import digital.peru.hym.facturador.service.interfaces.ElectronicDocumentService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping(path = "document-electronic")
@AllArgsConstructor
public class ElectronicDocumentController {
    
    private final ElectronicDocumentService service;

    @PostMapping(path = "")
    private void createElectronicDocument(@RequestBody ElectronicDocument electronicDocument){
        this.service.create(electronicDocument);
    }

}
