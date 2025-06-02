package com.apartmentservice.view.admin;
/**
 *
 * @author Nguyen Van Thang, Pham Van Su, Dang Anh Tuyen
 */

import com.apartmentservice.controller.ApartmentController;
import com.apartmentservice.controller.DashboardController;
import com.apartmentservice.manager.InvoiceManager;
import com.apartmentservice.manager.ServiceManager;
import com.apartmentservice.model.Apartment;
import com.apartmentservice.model.Invoice;
import com.apartmentservice.model.Service;
import com.apartmentservice.utils.FormSwitcher;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;


public class ThongKeForm extends javax.swing.JFrame {
    private DashboardController controller;
    private JPanel chartPanel;
    private String loggedInUsername;
    public ThongKeForm(String username) {
        initComponents();
        setTitle("Thống Kê");
        loggedInUsername = username;
        // Khởi tạo controller chính
        controller = new DashboardController();

        // Khởi tạo và cấu hình biểu đồ (chartPanel là vùng vẽ biểu đồ chính)
        chartPanel = new JPanel();
        chartPanel.setPreferredSize(new Dimension(800, 400));
        chartPanel.setLayout(new BorderLayout());

        // Gán vùng biểu đồ vào panel chứa (PanelBieuDo đã có trong giao diện)
        PanelBieuDo.setLayout(new BorderLayout());
        PanelBieuDo.add(chartPanel, BorderLayout.CENTER);

        // Hiển thị thông tin tổng quan ban đầu (toàn bộ hệ thống)
        hienThiThongTinTongQuan();

        // Vẽ biểu đồ doanh thu theo dịch vụ cho tất cả các hóa đơn đã thanh toán
        veBieuDoDoanhThu12ThangGanNhat();
    }
    
    private void veBieuDoDoanhThuTheoThang(int thang, int nam) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Service> allServices = new ServiceManager().getAll();
        List<Invoice> allInvoices = new InvoiceManager().getAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Service service : allServices) {
            double revenue = 0;
            for (Invoice invoice : allInvoices) {
                if (!invoice.isPaidAsBoolean()) continue;
                try {
                    LocalDate date = LocalDate.parse(invoice.getInvoiceDate(), formatter);
                    if (date.getMonthValue() == thang && date.getYear() == nam) {
                        revenue += invoice.getService().stream()
                                .filter(sf -> sf.getServiceID().equals(service.getServiceID()))
                                .mapToDouble(sf -> service.getUnitPrice())
                                .sum();
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi định dạng ngày: " + invoice.getInvoiceDate());
                }
            }
            if (revenue > 0) {
                dataset.addValue(revenue, "Doanh thu", service.getServiceName());
            }
        }

        String monthYear = String.format("%02d/%d", thang, nam);
        JFreeChart chart = ChartFactory.createBarChart(
            "Doanh thu theo dịch vụ - Tháng " + monthYear,
            "Dịch vụ", "Doanh thu (VND)", dataset
        );
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        PanelBieuDo.removeAll();
        PanelBieuDo.setLayout(new BorderLayout());
        PanelBieuDo.add(chartPanel, BorderLayout.CENTER);
        PanelBieuDo.validate();
    }

    private void veBieuDoDoanhThu12ThangGanNhat() {
        List<Invoice> allInvoices = new InvoiceManager().getAll();
        List<Service> allServices = new ServiceManager().getAll();
        List<Apartment> allApartments = new ApartmentController().getAllApartments();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate now = LocalDate.now();
        Map<String, Double> doanhThuTheoThang = new TreeMap<>();
        for (Invoice invoice : allInvoices) {
            if (!invoice.isPaidAsBoolean()) continue;
            try {
                LocalDate date = LocalDate.parse(invoice.getInvoiceDate(), formatter);
                if (!date.isBefore(now.minusMonths(12).withDayOfMonth(1))) {
                    String key = String.format("%02d/%d", date.getMonthValue(), date.getYear());
                    // Tìm đúng căn hộ của hóa đơn
                    Apartment matchedApartment = allApartments.stream()
                            .filter(a -> a.getApartmentID().equals(invoice.getApartmentID()))
                            .findFirst()
                            .orElse(null);
                    double tien = (matchedApartment != null)
                            ? Invoice.getTotalAmountRaw(matchedApartment, allServices)
                            : 0;
                    doanhThuTheoThang.merge(key, tien, Double::sum);
                }
            } catch (Exception e) {
                System.err.println("Lỗi ngày: " + invoice.getInvoiceDate());
            }
        }
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : doanhThuTheoThang.entrySet()) {
            dataset.addValue(entry.getValue(), "Doanh thu", entry.getKey());
        }
        JFreeChart chart = ChartFactory.createBarChart(
            "Doanh thu 12 tháng gần nhất",
            "Tháng/Năm", "Doanh thu (VND)", dataset
        );
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        PanelBieuDo.removeAll();
        PanelBieuDo.setLayout(new BorderLayout());
        PanelBieuDo.add(chartPanel, BorderLayout.CENTER);
        PanelBieuDo.validate();
    }
    private void hienThiThongTinTongQuan() {
        List<Service> allServices = new ServiceManager().getAll();
        List<Apartment> allApartments = new ApartmentController().getAllApartments(); // cần thêm danh sách căn hộ

        txtTongSoCanHo.setText(String.valueOf(controller.getTotalApartments()));
        txtTongSoCuDan.setText(String.valueOf(controller.getTotalResidents()));
        txtTongDichVu.setText(String.valueOf(controller.getTotalServices()));
        txtTongHoaDonTheoThang.setText(String.valueOf(controller.getTotalInvoices()));
        txtTongDoanhThu.setText(controller.getTotalRevenueFormatted(allServices, allApartments)); // sửa ở đây
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        butQuanLyCanHo = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        butDanhSachCuDan = new javax.swing.JButton();
        butQuanLyHoaDon = new javax.swing.JButton();
        butQuanLyDichVu = new javax.swing.JButton();
        butThongTinToaNha = new javax.swing.JButton();
        butHinhThucThanhToan = new javax.swing.JButton();
        butDangXuat = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        butThongKe = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lbHoVaTen = new javax.swing.JLabel();
        lbNgaySinh = new javax.swing.JLabel();
        butLocDuLieu = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        chooseThang = new com.toedter.calendar.JMonthChooser();
        chooseNam = new com.toedter.calendar.JYearChooser();
        txtTongSoCanHo = new javax.swing.JTextField();
        txtTongHoaDonTheoThang = new javax.swing.JTextField();
        txtTongDoanhThu = new javax.swing.JTextField();
        txtTongSoCuDan = new javax.swing.JTextField();
        txtTongDichVu = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        butDoanhThu = new javax.swing.JButton();
        PanelBieuDo = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1265, 720));

        jPanel3.setBackground(new java.awt.Color(0, 51, 102));
        jPanel3.setPreferredSize(new java.awt.Dimension(225, 720));

        butQuanLyCanHo.setBackground(new java.awt.Color(0, 51, 102));
        butQuanLyCanHo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        butQuanLyCanHo.setForeground(new java.awt.Color(255, 255, 255));
        butQuanLyCanHo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/apartmentservice/icons/1.png"))); // NOI18N
        butQuanLyCanHo.setText(" Quản Lý Căn Hộ");
        butQuanLyCanHo.setBorderPainted(false);
        butQuanLyCanHo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        butQuanLyCanHo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butQuanLyCanHoActionPerformed(evt);
            }
        });

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 102, 51));
        jLabel3.setText("Hello, Admin!");

        butDanhSachCuDan.setBackground(new java.awt.Color(0, 51, 102));
        butDanhSachCuDan.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        butDanhSachCuDan.setForeground(new java.awt.Color(255, 255, 255));
        butDanhSachCuDan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/apartmentservice/icons/3.png"))); // NOI18N
        butDanhSachCuDan.setText("  Danh Sách Cư Dân");
        butDanhSachCuDan.setBorderPainted(false);
        butDanhSachCuDan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        butDanhSachCuDan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDanhSachCuDanActionPerformed(evt);
            }
        });

        butQuanLyHoaDon.setBackground(new java.awt.Color(0, 51, 102));
        butQuanLyHoaDon.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        butQuanLyHoaDon.setForeground(new java.awt.Color(255, 255, 255));
        butQuanLyHoaDon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/apartmentservice/icons/2.png"))); // NOI18N
        butQuanLyHoaDon.setText("  Quản Lý Hóa Đơn");
        butQuanLyHoaDon.setBorderPainted(false);
        butQuanLyHoaDon.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        butQuanLyHoaDon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butQuanLyHoaDonActionPerformed(evt);
            }
        });

        butQuanLyDichVu.setBackground(new java.awt.Color(0, 51, 102));
        butQuanLyDichVu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        butQuanLyDichVu.setForeground(new java.awt.Color(255, 255, 255));
        butQuanLyDichVu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/apartmentservice/icons/4.png"))); // NOI18N
        butQuanLyDichVu.setText("   Quản Lý Dịch Vụ");
        butQuanLyDichVu.setBorderPainted(false);
        butQuanLyDichVu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        butQuanLyDichVu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butQuanLyDichVuActionPerformed(evt);
            }
        });

        butThongTinToaNha.setBackground(new java.awt.Color(0, 51, 102));
        butThongTinToaNha.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        butThongTinToaNha.setForeground(new java.awt.Color(255, 255, 255));
        butThongTinToaNha.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/apartmentservice/icons/5.png"))); // NOI18N
        butThongTinToaNha.setText(" Thông Tin Tòa Nhà");
        butThongTinToaNha.setBorderPainted(false);
        butThongTinToaNha.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        butThongTinToaNha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butThongTinToaNhaActionPerformed(evt);
            }
        });

        butHinhThucThanhToan.setBackground(new java.awt.Color(0, 51, 102));
        butHinhThucThanhToan.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        butHinhThucThanhToan.setForeground(new java.awt.Color(255, 255, 255));
        butHinhThucThanhToan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/apartmentservice/icons/6.png"))); // NOI18N
        butHinhThucThanhToan.setText("Hình Thức Thanh Toán");
        butHinhThucThanhToan.setBorderPainted(false);
        butHinhThucThanhToan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        butHinhThucThanhToan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butHinhThucThanhToanActionPerformed(evt);
            }
        });

        butDangXuat.setText("Đăng Xuất");
        butDangXuat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDangXuatActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/apartmentservice/icons/logo.png"))); // NOI18N

        butThongKe.setBackground(new java.awt.Color(0, 51, 102));
        butThongKe.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        butThongKe.setForeground(new java.awt.Color(255, 255, 255));
        butThongKe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/apartmentservice/icons/7.png"))); // NOI18N
        butThongKe.setText("Thống Kê");
        butThongKe.setBorderPainted(false);
        butThongKe.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        butThongKe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butThongKeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(butDangXuat)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(71, 71, 71))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(butThongKe, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(butHinhThucThanhToan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(butThongTinToaNha, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(butQuanLyDichVu, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(butDanhSachCuDan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(butQuanLyHoaDon, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(butQuanLyCanHo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(40, 40, 40)
                .addComponent(butQuanLyCanHo, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(butQuanLyHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(butDanhSachCuDan, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(butQuanLyDichVu, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(butThongTinToaNha, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(butHinhThucThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(butThongKe, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(butDangXuat)
                .addGap(65, 65, 65))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setPreferredSize(new java.awt.Dimension(920, 650));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Thống Kê Thu Phí Dịch Vụ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        lbHoVaTen.setText("Tháng:");

        lbNgaySinh.setText("Năm:");

        butLocDuLieu.setText("Lọc Dữ Liệu");
        butLocDuLieu.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        butLocDuLieu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLocDuLieuActionPerformed(evt);
            }
        });

        jLabel2.setText("Tổng số căn hộ:");

        jLabel4.setText("Tổng số cư dân:");

        jLabel5.setText("Tổng dịch vụ:");

        jLabel6.setText("Tổng hóa đơn:");

        jLabel7.setText("Doanh Thu :");

        txtTongHoaDonTheoThang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTongHoaDonTheoThangActionPerformed(evt);
            }
        });

        jLabel8.setText("VNĐ");

        butDoanhThu.setText("Doanh Thu");
        butDoanhThu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDoanhThuActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(92, 92, 92)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(txtTongHoaDonTheoThang, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(txtTongDoanhThu, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(txtTongSoCanHo, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(txtTongSoCuDan, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(71, 71, 71)
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(txtTongDichVu, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(92, 92, 92))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 817, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addComponent(lbHoVaTen)
                .addGap(18, 18, 18)
                .addComponent(chooseThang, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(lbNgaySinh)
                .addGap(18, 18, 18)
                .addComponent(chooseNam, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(butLocDuLieu, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(butDoanhThu, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chooseThang, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addComponent(chooseNam, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbHoVaTen, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbNgaySinh, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(butDoanhThu)
                        .addComponent(butLocDuLieu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(txtTongSoCanHo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTongSoCuDan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTongDichVu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(txtTongHoaDonTheoThang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTongDoanhThu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout PanelBieuDoLayout = new javax.swing.GroupLayout(PanelBieuDo);
        PanelBieuDo.setLayout(PanelBieuDoLayout);
        PanelBieuDoLayout.setHorizontalGroup(
            PanelBieuDoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        PanelBieuDoLayout.setVerticalGroup(
            PanelBieuDoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 450, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(0, 78, Short.MAX_VALUE)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(PanelBieuDo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(74, 74, 74))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(PanelBieuDo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(147, 147, 147))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 1017, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 720, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1248, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void butLocDuLieuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butLocDuLieuActionPerformed
        // Bước 1: Lấy tháng và năm từ người dùng chọn
        int selectedMonth = chooseThang.getMonth() + 1; // JMonthChooser: 0-11
        int selectedYear = chooseNam.getYear();

        // Bước 2: Lấy dữ liệu dịch vụ, căn hộ & hóa đơn đã thanh toán
        List<Service> allServices = new ServiceManager().getAll();
        List<Apartment> allApartments = new ApartmentController().getAllApartments();
        List<Invoice> allInvoices = new InvoiceManager().getAll();

        // Bước 3: Lọc hóa đơn theo tháng và năm được chọn
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Invoice> filteredInvoices = allInvoices.stream()
            .filter(Invoice::isPaidAsBoolean)
            .filter(inv -> {
                try {
                    LocalDate date = LocalDate.parse(inv.getInvoiceDate(), formatter);
                    return date.getMonthValue() == selectedMonth && date.getYear() == selectedYear;
                } catch (Exception e) {
                    return false;
                }
            })
            .toList();

        // Bước 4: Cập nhật tổng hóa đơn theo tháng
        txtTongHoaDonTheoThang.setText(String.valueOf(filteredInvoices.size()));

        // Bước 5: Tính tổng doanh thu đúng cách (phải tìm căn hộ tương ứng)
        double totalRevenue = filteredInvoices.stream()
            .mapToDouble(invoice -> {
                Apartment matchedApartment = allApartments.stream()
                    .filter(a -> a.getApartmentID().equals(invoice.getApartmentID()))
                    .findFirst()
                    .orElse(null);
                return (matchedApartment != null) ? Invoice.getTotalAmountRaw(matchedApartment, allServices) : 0;
            })
            .sum();

        // Bước 6: Hiển thị doanh thu đã định dạng
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(0);
        txtTongDoanhThu.setText(nf.format(totalRevenue).replace('.', ',')); // đổi dấu chấm thành dấu phẩy

        // ==== BỔ SUNG TÍNH TỔNG THEO DỮ LIỆU ĐÃ LỌC ====

        // Tổng số căn hộ có hóa đơn đã thanh toán trong tháng/năm
        Set<String> uniqueApartmentIDs = filteredInvoices.stream()
                .map(Invoice::getApartmentID)
                .collect(Collectors.toSet());
        txtTongSoCanHo.setText(String.valueOf(uniqueApartmentIDs.size()));

        // Tổng số cư dân (chủ hộ) có trong các căn hộ đã lọc
        Set<String> uniqueOwnerNames = allApartments.stream()
                .filter(a -> uniqueApartmentIDs.contains(a.getApartmentID()))
                .map(Apartment::getOwnerName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .collect(Collectors.toSet());
        txtTongSoCuDan.setText(String.valueOf(uniqueOwnerNames.size()));

        // Tổng số dịch vụ xuất hiện trong các căn hộ có hóa đơn đã thanh toán tháng/năm đó
        Set<String> uniqueServiceNames = new HashSet<>();
        allApartments.stream()
                .filter(a -> uniqueApartmentIDs.contains(a.getApartmentID()))
                .map(Apartment::getDichVu)
                .filter(dvs -> dvs != null && !dvs.trim().isEmpty())
                .forEach(dvs -> {
                    for (String name : dvs.split(",")) {
                        uniqueServiceNames.add(name.trim().toLowerCase());
                    }
                });
        txtTongDichVu.setText(String.valueOf(uniqueServiceNames.size()));

        // ==== (Nếu muốn vẽ lại biểu đồ hoặc cập nhật bảng dữ liệu...) ====
        veBieuDoDoanhThuTheoThang(selectedMonth, selectedYear);
    }//GEN-LAST:event_butLocDuLieuActionPerformed
    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;  
    }
    private void butThongKeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butThongKeActionPerformed
        FormSwitcher.switchForm(this, new ThongKeForm(loggedInUsername));
    }//GEN-LAST:event_butThongKeActionPerformed

    private void butDangXuatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDangXuatActionPerformed
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose(); // Đóng giao diện quản lý hiện tại
            new com.apartmentservice.view.login.LoginFrame().setVisible(true); // Quay về màn hình đăng nhập
        }
    }//GEN-LAST:event_butDangXuatActionPerformed

    private void butHinhThucThanhToanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butHinhThucThanhToanActionPerformed
        FormSwitcher.switchForm(this, new ThanhToanForm(loggedInUsername));
    }//GEN-LAST:event_butHinhThucThanhToanActionPerformed

    private void butThongTinToaNhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butThongTinToaNhaActionPerformed
        FormSwitcher.switchForm(this, new ToaNhaForm(loggedInUsername));
    }//GEN-LAST:event_butThongTinToaNhaActionPerformed

    private void butQuanLyDichVuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butQuanLyDichVuActionPerformed
        FormSwitcher.switchForm(this, new DichVuForm(loggedInUsername));
    }//GEN-LAST:event_butQuanLyDichVuActionPerformed

    private void butQuanLyHoaDonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butQuanLyHoaDonActionPerformed
        FormSwitcher.switchForm(this, new DanhSachThanhToanForm(loggedInUsername));
    }//GEN-LAST:event_butQuanLyHoaDonActionPerformed

    private void butDanhSachCuDanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDanhSachCuDanActionPerformed
        FormSwitcher.switchForm(this, new CuDanForm(loggedInUsername));
    }//GEN-LAST:event_butDanhSachCuDanActionPerformed

    private void butQuanLyCanHoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butQuanLyCanHoActionPerformed
        FormSwitcher.switchForm(this, new CanHoForm(loggedInUsername));
    }//GEN-LAST:event_butQuanLyCanHoActionPerformed

    private void txtTongHoaDonTheoThangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTongHoaDonTheoThangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTongHoaDonTheoThangActionPerformed

    private void butDoanhThuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDoanhThuActionPerformed
        hienThiThongTinTongQuan();
        veBieuDoDoanhThu12ThangGanNhat();
    }//GEN-LAST:event_butDoanhThuActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ThongKeForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ThongKeForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ThongKeForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ThongKeForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ThongKeForm("Admin").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelBieuDo;
    private javax.swing.JButton butDangXuat;
    private javax.swing.JButton butDanhSachCuDan;
    private javax.swing.JButton butDoanhThu;
    private javax.swing.JButton butHinhThucThanhToan;
    private javax.swing.JButton butLocDuLieu;
    private javax.swing.JButton butQuanLyCanHo;
    private javax.swing.JButton butQuanLyDichVu;
    private javax.swing.JButton butQuanLyHoaDon;
    private javax.swing.JButton butThongKe;
    private javax.swing.JButton butThongTinToaNha;
    private com.toedter.calendar.JYearChooser chooseNam;
    private com.toedter.calendar.JMonthChooser chooseThang;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lbHoVaTen;
    private javax.swing.JLabel lbNgaySinh;
    private javax.swing.JTextField txtTongDichVu;
    private javax.swing.JTextField txtTongDoanhThu;
    private javax.swing.JTextField txtTongHoaDonTheoThang;
    private javax.swing.JTextField txtTongSoCanHo;
    private javax.swing.JTextField txtTongSoCuDan;
    // End of variables declaration//GEN-END:variables
}
