package nckh.felix.StupidParking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import nckh.felix.StupidParking.domain.Price;
import nckh.felix.StupidParking.service.PriceService;
import nckh.felix.StupidParking.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PriceController {
    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @PostMapping("/prices")
    public ResponseEntity<Price> createNewPrice(@RequestBody Price xPrice) {
        Price vPrice = this.priceService.handleCreatePrice(xPrice);
        return ResponseEntity.status(HttpStatus.CREATED).body(vPrice);
    }

    @GetMapping("/prices/{maBangGia}")
    public ResponseEntity<Price> getPriceByMaBangGia(@PathVariable("maBangGia") String maBangGia) {
        Price vPrice = this.priceService.fetchPriceByMaBangGia(maBangGia);
        return ResponseEntity.status(HttpStatus.OK).body(vPrice);
    }

    @GetMapping("/prices")
    public ResponseEntity<List<Price>> getAllPrices() {
        return ResponseEntity.status(HttpStatus.OK).body(this.priceService.fetchAllPrices());
    }

    @PutMapping("/prices")
    public ResponseEntity<Price> updatePrice(@RequestBody Price xPrice) {
        Price vPrice = this.priceService.handleUpdatePrice(xPrice);
        return ResponseEntity.ok(vPrice);
    }

    @DeleteMapping("/prices/{maBangGia}")
    public ResponseEntity<String> deletePrice(@PathVariable("maBangGia") String maBangGia) throws IdInvalidException {
        this.priceService.handleDeletePrice(maBangGia);
        return ResponseEntity.ok("Xóa bảng giá thành công");
    }
}
