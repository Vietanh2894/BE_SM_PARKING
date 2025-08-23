package nckh.felix.StupidParking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.Price;
import nckh.felix.StupidParking.repository.PriceRepository;

@Service
public class PriceService {
    private PriceRepository priceRepository;

    public PriceService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    public Price handleCreatePrice(Price price) {
        return this.priceRepository.save(price);
    }

    public Price fetchPriceByMaBangGia(String maBangGia) {
        Optional<Price> priceOptional = this.priceRepository.findById(maBangGia);
        if (priceOptional.isPresent()) {
            return priceOptional.get();
        }
        return null;
    }

    public List<Price> fetchAllPrices() {
        return this.priceRepository.findAll();
    }

    public Price handleUpdatePrice(Price reqPrice) {
        Price currentPrice = this.fetchPriceByMaBangGia(reqPrice.getMaBangGia());
        if (currentPrice != null) {
            currentPrice.setMaLoaiXe(reqPrice.getMaLoaiXe());
            currentPrice.setMaHinhThuc(reqPrice.getMaHinhThuc());
            currentPrice.setGia(reqPrice.getGia());
            currentPrice = this.priceRepository.save(currentPrice);
        }
        return currentPrice;
    }

    public void handleDeletePrice(String maBangGia) {
        this.priceRepository.deleteById(maBangGia);
    }
}
