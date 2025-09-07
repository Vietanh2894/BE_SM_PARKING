package nckh.felix.StupidParking.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import nckh.felix.StupidParking.service.DangKyThangService;

@Component
public class DangKyThangScheduledTasks {

    private final DangKyThangService dangKyThangService;

    public DangKyThangScheduledTasks(DangKyThangService dangKyThangService) {
        this.dangKyThangService = dangKyThangService;
    }

    /**
     * Chạy hàng ngày lúc 00:01 để cập nhật trạng thái đăng ký tháng hết hạn
     * Cron format: second minute hour day month dayOfWeek
     */
    @Scheduled(cron = "0 1 0 * * *")
    public void updateExpiredDangKyThang() {
        try {
            dangKyThangService.updateExpiredDangKyThang();
            System.out.println("Successfully updated expired monthly registrations at: " +
                    java.time.LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Error updating expired monthly registrations: " + e.getMessage());
        }
    }

    /**
     * Chạy mỗi 6 giờ để cập nhật trạng thái đăng ký tháng hết hạn
     * Backup job để đảm bảo dữ liệu được cập nhật thường xuyên
     */
    @Scheduled(fixedRate = 21600000) // 6 hours = 6 * 60 * 60 * 1000 = 21600000 milliseconds
    public void updateExpiredDangKyThangBackup() {
        try {
            dangKyThangService.updateExpiredDangKyThang();
        } catch (Exception e) {
            System.err.println("Error in backup update expired monthly registrations: " + e.getMessage());
        }
    }
}
