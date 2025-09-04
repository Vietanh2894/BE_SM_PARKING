package nckh.felix.StupidParking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.Price;
import nckh.felix.StupidParking.domain.VehicleType;
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

    /**
     * Lấy giá theo giờ cho loại xe (mặc định lấy giá theo giờ)
     */
    public BigDecimal getHourlyRateByVehicleType(String maLoaiXe) {
        VehicleType vehicleType = new VehicleType(maLoaiXe);

        // Tìm giá theo loại xe và hình thức đỗ xe (giả sử có mã hình thức GIO cho đỗ
        // theo giờ)
        List<Price> prices = this.priceRepository.findByMaLoaiXe(vehicleType);

        if (!prices.isEmpty()) {
            // Lấy giá đầu tiên tìm được
            return prices.get(0).getGia();
        }

        // Giá mặc định nếu không tìm thấy
        return BigDecimal.valueOf(10000); // 10,000 VND/giờ
    }
}
