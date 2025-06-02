package com.apartmentservice.view.admin;
/**
 *
 * @author Nguyen Van Thang, Pham Van Su, Dang Anh Tuyen
 */
import com.apartmentservice.controller.ResidentController;
import com.apartmentservice.model.Apartment;
import com.apartmentservice.model.Resident;
import com.apartmentservice.utils.FormSwitcher;
import com.apartmentservice.utils.Validator;
import com.apartmentservice.utils.XMLUtil;
import com.apartmentservice.wrapper.ApartmentXML;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


public class CuDanForm extends javax.swing.JFrame {
    private ResidentController controller;
    private DefaultTableModel tableModel;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private String loggedInUsername;
    private boolean isUserSelectingCombo = true;
    public CuDanForm(String username) {
        initComponents();
        setTitle("Danh Sách Dân Cư");
        loggedInUsername = username;
        controller = new ResidentController();
        tableModel = (DefaultTableModel) DanhSachCuDan.getModel();
        try {
            loadCanHo(); // chỗ này nếu bị lỗi file sẽ gây crash
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi load dữ liệu căn hộ:\n" + e.getMessage());
        }
        checkNam.addActionListener(e -> {
            if (checkNam.isSelected()) checkNu.setSelected(false);
        });
        checkNu.addActionListener(e -> {
            if (checkNu.isSelected()) checkNam.setSelected(false);
        });
        loadTable();
        DanhSachCuDan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = DanhSachCuDan.getSelectedRow();
                if (selectedRow >= 0) {
                    String name = (String) DanhSachCuDan.getValueAt(selectedRow, 1);
                    String birthDateStr = (String) DanhSachCuDan.getValueAt(selectedRow, 2);
                    String sex = (String) DanhSachCuDan.getValueAt(selectedRow, 3);
                    String cccd = (String) DanhSachCuDan.getValueAt(selectedRow, 4);
                    String phone = (String) DanhSachCuDan.getValueAt(selectedRow, 5);
                    String apartmentID = (String) DanhSachCuDan.getValueAt(selectedRow, 6);
                    String birthPlace = (String) DanhSachCuDan.getValueAt(selectedRow, 7);
                    String familyID = (String) DanhSachCuDan.getValueAt(selectedRow, 8);

                    isUserSelectingCombo = false; // Tạm tắt event khi set giá trị combo

                    // Set comboHoVaTen
                    boolean foundName = false;
                    for (int i = 0; i < comboHoVaTen.getItemCount(); i++) {
                        if (comboHoVaTen.getItemAt(i).equals(name)) {
                            comboHoVaTen.setSelectedIndex(i);
                            foundName = true;
                            break;
                        }
                    }
                    if (!foundName) {
                        comboHoVaTen.addItem(name);
                        comboHoVaTen.setSelectedItem(name);
                    }

                    // Set comboMaCanHo
                    boolean foundApartment = false;
                    for (int i = 0; i < comboMaCanHo.getItemCount(); i++) {
                        if (comboMaCanHo.getItemAt(i).equals(apartmentID)) {
                            comboMaCanHo.setSelectedIndex(i);
                            foundApartment = true;
                            break;
                        }
                    }
                    if (!foundApartment) {
                        comboMaCanHo.addItem(apartmentID);
                        comboMaCanHo.setSelectedItem(apartmentID);
                    }

                    isUserSelectingCombo = true; // Bật lại event cho phép user thao tác

                    // Các trường text và date khác
                    txtCCCD.setText(cccd);
                    txtSĐT.setText(phone);
                    txtQueQuan.setText(birthPlace);
                    txtSoHoKhau.setText(familyID);
                    try {
                        if (birthDateStr != null && !birthDateStr.equalsIgnoreCase("Không rõ")) {
                            java.util.Date date = sdf.parse(birthDateStr);
                            dateNgaySinh.setDate(date);
                        } else {
                            dateNgaySinh.setDate(null);
                        }
                    } catch (Exception ex) {
                        dateNgaySinh.setDate(null);
                    }
                    if ("Nam".equalsIgnoreCase(sex)) {
                        checkNam.setSelected(true);
                        checkNu.setSelected(false);
                    } else if ("Nữ".equalsIgnoreCase(sex)) {
                        checkNam.setSelected(false);
                        checkNu.setSelected(true);
                    } else {
                        checkNam.setSelected(false);
                        checkNu.setSelected(false);
                    }
                }
            }
        });
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        // Căn trái cho tất cả các cột trong bảng (trừ cột kiểu số nếu bạn muốn căn phải)
        for (int i = 0; i < DanhSachCuDan.getColumnCount(); i++) {
            DanhSachCuDan.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }
    }
    private void loadCanHo() {
        comboMaCanHo.removeAllItems();
        comboHoVaTen.removeAllItems();

        File file = new File("data/apartments.xml");
        System.out.println("Đang đọc file tại: " + file.getAbsolutePath());

        try {
            ApartmentXML wrapper = XMLUtil.readFromXML(file, ApartmentXML.class);
            List<Apartment> apartmentList = (wrapper != null) ? wrapper.getApartment() : null;

            if (apartmentList == null || apartmentList.isEmpty()) {
                throw new NullPointerException("Danh sách apartment null - có thể file rỗng hoặc sai format.");
            }

            for (Apartment a : apartmentList) {
                comboMaCanHo.addItem(a.getApartmentID());
                comboHoVaTen.addItem(a.getOwnerName());
            }

            // Ràng buộc chỉ cho chọn 1 combo
            comboHoVaTen.addActionListener(e -> {
                if (!isUserSelectingCombo) return; // Bỏ qua khi set từ code
                if (comboHoVaTen.getSelectedIndex() != -1) {
                    comboMaCanHo.setSelectedIndex(-1);
                }
            });

            comboMaCanHo.addActionListener(e -> {
                if (!isUserSelectingCombo) return; // Bỏ qua khi set từ code
                if (comboMaCanHo.getSelectedIndex() != -1) {
                    comboHoVaTen.setSelectedIndex(-1);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi đọc dữ liệu căn hộ từ file XML:\n" + ex.getMessage(),
                    "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadTable() {
        tableModel.setRowCount(0); // Xóa tất cả dữ liệu cũ trong bảng
        List<Resident> residents = controller.getAllResidents();
        int stt = 1;

        for (Resident r : residents) {
            // Xử lý ngày sinh: nếu định dạng sai sẽ hiển thị "Không rõ"
            String birthDateStr;
            try {
                birthDateStr = sdf.format(sdf.parse(r.getBirthday()));
            } catch (Exception e) {
                birthDateStr = "Không rõ";
            }

            // Thêm dòng vào bảng
            tableModel.addRow(new Object[]{
                stt++,
                r.getName(),
                birthDateStr,
                r.getSex(),
                r.getCccd(),
                r.getPhoneNumber(),
                r.getApartmentID(),
                r.getBirthPlace(),
                r.getIDFamily()
            });
        }
    }

    
    private void clearForm() {
        comboHoVaTen.setSelectedIndex(0);
        txtCCCD.setText("");
        txtSoHoKhau.setText("");
        txtSĐT.setText("");
        txtQueQuan.setText("");
        dateNgaySinh.setDate(null);
        comboMaCanHo.setSelectedIndex(0);
        checkNam.setSelected(true);
        checkNu.setSelected(false);
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        lbHoVaTen = new javax.swing.JLabel();
        txtQueQuan = new javax.swing.JTextField();
        lbQueQuan = new javax.swing.JLabel();
        lbCCCD = new javax.swing.JLabel();
        lbGioiTinh = new javax.swing.JLabel();
        lbNgaySinh = new javax.swing.JLabel();
        checkNam = new javax.swing.JCheckBox();
        checkNu = new javax.swing.JCheckBox();
        lbMaCanHo = new javax.swing.JLabel();
        lbSoHoKhau = new javax.swing.JLabel();
        txtSĐT = new javax.swing.JTextField();
        lbSĐT = new javax.swing.JLabel();
        dateNgaySinh = new com.toedter.calendar.JDateChooser();
        butClear = new javax.swing.JButton();
        comboMaCanHo = new javax.swing.JComboBox<>();
        txtCCCD = new javax.swing.JTextField();
        txtSoHoKhau = new javax.swing.JTextField();
        comboHoVaTen = new javax.swing.JComboBox<>();
        butThem = new javax.swing.JButton();
        butCapNhat = new javax.swing.JButton();
        butXoa = new javax.swing.JButton();
        butSapXep = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        DanhSachCuDan = new javax.swing.JTable();
        butTimKiem = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        butLamMoi = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1265, 720));

        jPanel3.setBackground(new java.awt.Color(0, 51, 102));

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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(60, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(71, 71, 71))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(butQuanLyCanHo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(butQuanLyHoaDon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(butDanhSachCuDan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(butQuanLyDichVu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(butThongTinToaNha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(butHinhThucThanhToan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(butThongKe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(69, 69, 69)
                .addComponent(butDangXuat)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 112, Short.MAX_VALUE)
                .addComponent(butDangXuat)
                .addGap(49, 49, 49))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setPreferredSize(new java.awt.Dimension(920, 650));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Thông Tin Cư Dân", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        lbHoVaTen.setText("Họ và tên:");

        txtQueQuan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQueQuanActionPerformed(evt);
            }
        });

        lbQueQuan.setText("Quê quán:");

        lbCCCD.setText("CCCD:");

        lbGioiTinh.setText("Giới tính:");

        lbNgaySinh.setText("Ngày sinh:");

        checkNam.setText("Nam");
        checkNam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkNamActionPerformed(evt);
            }
        });

        checkNu.setText("Nữ");
        checkNu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkNuActionPerformed(evt);
            }
        });

        lbMaCanHo.setText("Mã căn hộ:");

        lbSoHoKhau.setText("Số hộ khẩu:");

        txtSĐT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSĐTActionPerformed(evt);
            }
        });

        lbSĐT.setText("Số điện thoại:");

        butClear.setText("Clear");
        butClear.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        butClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butClearActionPerformed(evt);
            }
        });

        comboMaCanHo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        txtCCCD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCCCDActionPerformed(evt);
            }
        });

        txtSoHoKhau.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSoHoKhauActionPerformed(evt);
            }
        });

        comboHoVaTen.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(lbGioiTinh)
                                .addGap(7, 7, 7))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lbHoVaTen)))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(checkNam, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkNu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(comboHoVaTen, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(lbNgaySinh)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dateNgaySinh, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(lbMaCanHo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboMaCanHo, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbQueQuan)
                            .addComponent(lbCCCD))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(txtQueQuan, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lbSĐT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSĐT, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(txtCCCD)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lbSoHoKhau)
                                .addGap(18, 18, 18)
                                .addComponent(txtSoHoKhau, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(438, 438, 438)
                        .addComponent(butClear, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lbMaCanHo)
                            .addComponent(lbQueQuan)
                            .addComponent(lbHoVaTen)
                            .addComponent(txtQueQuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbSĐT)
                            .addComponent(txtSĐT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboMaCanHo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboHoVaTen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dateNgaySinh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSoHoKhau, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lbNgaySinh, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(checkNu)
                                .addComponent(checkNam)
                                .addComponent(lbGioiTinh))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCCCD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbCCCD)
                            .addComponent(lbSoHoKhau))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(butClear)
                .addContainerGap())
        );

        butThem.setText("Thêm");
        butThem.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        butThem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butThemActionPerformed(evt);
            }
        });

        butCapNhat.setText("Cập Nhật");
        butCapNhat.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        butCapNhat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCapNhatActionPerformed(evt);
            }
        });

        butXoa.setText("Xóa");
        butXoa.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        butXoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butXoaActionPerformed(evt);
            }
        });

        butSapXep.setText("Sắp Xếp");
        butSapXep.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        butSapXep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSapXepActionPerformed(evt);
            }
        });

        DanhSachCuDan.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        DanhSachCuDan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "STT", "Họ Và Tên", "Ngày Sinh", "Giới Tính", "CCCD", "Số Điện Thoại", "Mã Căn Hộ", "Quê Quán", "Số Hộ Khẩu"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        DanhSachCuDan.setPreferredSize(new java.awt.Dimension(675, 500));
        jScrollPane1.setViewportView(DanhSachCuDan);

        butTimKiem.setText("Tìm Kiếm");
        butTimKiem.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        butTimKiem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butTimKiemActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel17.setText("DANH SÁCH CƯ DÂN");

        butLamMoi.setText("Làm Mới");
        butLamMoi.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        butLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLamMoiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 31, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(270, 270, 270)
                                .addComponent(butThem, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(butCapNhat, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(butXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(butSapXep, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(butTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(butLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(411, 411, 411)
                                .addComponent(jLabel17)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(59, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(butThem)
                    .addComponent(butCapNhat)
                    .addComponent(butXoa)
                    .addComponent(butSapXep)
                    .addComponent(butTimKiem)
                    .addComponent(butLamMoi))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 1016, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtQueQuanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQueQuanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtQueQuanActionPerformed

    private void checkNamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkNamActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkNamActionPerformed

    private void checkNuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkNuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkNuActionPerformed

    private void txtSĐTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSĐTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSĐTActionPerformed

    private void butThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butThemActionPerformed
        String cccd = txtCCCD.getText().trim();
        String name = "";
        String apartmentID = "";
        Object selectedName = comboHoVaTen.getSelectedItem();
        Object selectedApartment = comboMaCanHo.getSelectedItem();
        boolean nameSelected = selectedName != null && !selectedName.toString().equals("");
        boolean apartmentSelected = selectedApartment != null && !selectedApartment.toString().equals("");
        if (nameSelected && apartmentSelected) {
            JOptionPane.showMessageDialog(this, "Vui lòng chỉ chọn MỘT trong hai: Họ tên HOẶC Mã căn hộ.");
            return;
        } else if (!nameSelected && !apartmentSelected) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Họ tên hoặc Mã căn hộ.");
            return;
        }
        File file = new File("data/apartments.xml");
        ApartmentXML wrapper = XMLUtil.readFromXML(file, ApartmentXML.class);
        List<Apartment> apartmentList = (wrapper != null) ? wrapper.getApartment() : null;
        if (apartmentList == null || apartmentList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu căn hộ.");
            return;
        }
        if (apartmentSelected) {
            apartmentID = selectedApartment.toString();
            for (Apartment a : apartmentList) {
                if (a.getApartmentID().equals(apartmentID)) {
                    name = a.getOwnerName();
                    break;
                }
            }
        } else if (nameSelected) {
            name = selectedName.toString();
            for (Apartment a : apartmentList) {
                if (a.getOwnerName().equals(name)) {
                    apartmentID = a.getApartmentID();
                    break;
                }
            }
        }
        java.util.Date birthdayUtil = dateNgaySinh.getDate();
        String phone = txtSĐT.getText().trim();
        String birthPlace = txtQueQuan.getText().trim();
        String idFamily = txtSoHoKhau.getText().trim();
        String sex = "";
        if (checkNam.isSelected() && !checkNu.isSelected()) {
            sex = "Nam";
        } else if (!checkNam.isSelected() && checkNu.isSelected()) {
            sex = "Nữ";
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đúng giới tính (chỉ một).");
            return;
        }
        if (cccd.isEmpty() || name.isEmpty() || birthdayUtil == null || sex.isEmpty()
                || phone.isEmpty() || apartmentID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin.");
            return;
        }
        if (!Validator.isValidName(name)) {
            JOptionPane.showMessageDialog(this, "Họ tên không hợp lệ.");
            return;
        }
        if (!Validator.isValidCCCD(cccd)) {
            JOptionPane.showMessageDialog(this, "CCCD không hợp lệ.");
            return;
        }
        if (!Validator.isValidPhoneNumber(phone)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ.");
            return;
        }
        if (!Validator.isValidName(birthPlace)) {
            JOptionPane.showMessageDialog(this, "Quê quán không hợp lệ.");
            return;
        }
        if (!Validator.isSafeText(idFamily)) {
            JOptionPane.showMessageDialog(this, "Sổ hộ khẩu không hợp lệ.");
            return;
        }
        // Thêm phần kiểm tra trùng CCCD và trùng mã căn hộ trong danh sách cư dân
        List<Resident> residentList = controller.getAllResidents(); // giả sử có phương thức này
        if (residentList != null) {
            for (Resident r : residentList) {
                if (r.getCccd().equals(cccd)) {
                    JOptionPane.showMessageDialog(this, "CCCD đã tồn tại, không thể thêm trùng.");
                    return;
                }
                if (r.getApartmentID().equals(apartmentID)) {
                    JOptionPane.showMessageDialog(this, "Mã căn hộ đã có cư dân, không thể thêm trùng.");
                    return;
                }
                if (r.getIDFamily().equals(idFamily)) {  
                    JOptionPane.showMessageDialog(this, "Số hộ khẩu đã tồn tại, không thể thêm trùng.");
                    return;
                }
            }
        }
        String birthday = sdf.format(birthdayUtil);
        boolean success = controller.addResident(cccd, name, birthday, "", idFamily, sex, "", birthPlace, phone, apartmentID);
        if (success) {
            loadTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Thêm cư dân thành công.");
        } else {
            JOptionPane.showMessageDialog(this, "Thêm cư dân thất bại. Kiểm tra lại thông tin.");
        }
    }//GEN-LAST:event_butThemActionPerformed

    private void butCapNhatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCapNhatActionPerformed
        // Lấy danh sách CCCD cư dân
        List<String> cccdList = controller.getAllCCCD();
        if (cccdList == null || cccdList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có cư dân nào để cập nhật.");
            return;
        }
        String[] cccdArray = cccdList.toArray(new String[0]);
        // Hộp thoại chọn CCCD
        String targetCCCD = (String) JOptionPane.showInputDialog(
            this,
            "Chọn CCCD của cư dân cần cập nhật:",
            "Chọn CCCD",
            JOptionPane.QUESTION_MESSAGE,
            null,
            cccdArray,
            cccdArray[0]
        );
        if (targetCCCD == null) {
            return;
        }

        Resident existing = controller.findByCCCD(targetCCCD);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy cư dân có CCCD: " + targetCCCD);
            return;
        }

        // Tên và Mã căn hộ (chỉ được chọn 1 trong 2)
        String name = "";
        String apartmentID = "";
        Object selectedName = comboHoVaTen.getSelectedItem();
        Object selectedApartment = comboMaCanHo.getSelectedItem();
        boolean nameSelected = selectedName != null && !selectedName.toString().equals("");
        boolean apartmentSelected = selectedApartment != null && !selectedApartment.toString().equals("");
        if (nameSelected && apartmentSelected) {
            JOptionPane.showMessageDialog(this, "Vui lòng chỉ chọn MỘT trong hai: Họ tên HOẶC Mã căn hộ.");
            return;
        } else if (!nameSelected && !apartmentSelected) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Họ tên hoặc Mã căn hộ.");
            return;
        }

        // Tải file apartment.xml
        File file = new File("data/apartments.xml");
        ApartmentXML wrapper = XMLUtil.readFromXML(file, ApartmentXML.class);
        List<Apartment> apartmentList = (wrapper != null) ? wrapper.getApartment() : null;
        if (apartmentList == null || apartmentList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu căn hộ.");
            return;
        }

        // Ánh xạ name ↔ apartmentID
        if (apartmentSelected) {
            apartmentID = selectedApartment.toString();
            for (Apartment a : apartmentList) {
                if (a.getApartmentID().equals(apartmentID)) {
                    name = a.getOwnerName();
                    break;
                }
            }
        } else {
            name = selectedName.toString();
            for (Apartment a : apartmentList) {
                if (a.getOwnerName().equals(name)) {
                    apartmentID = a.getApartmentID();
                    break;
                }
            }
        }

        // Các thông tin còn lại
        String cccd = txtCCCD.getText().trim();
        java.util.Date birthdayUtil = dateNgaySinh.getDate();
        String phone = txtSĐT.getText().trim();
        String birthPlace = txtQueQuan.getText().trim();
        String idFamily = txtSoHoKhau.getText().trim();
        String sex = "";
        if (checkNam.isSelected() && !checkNu.isSelected()) {
            sex = "Nam";
        } else if (!checkNam.isSelected() && checkNu.isSelected()) {
            sex = "Nữ";
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đúng giới tính (chỉ một).");
            return;
        }

        // Kiểm tra bắt buộc
        if (cccd.isEmpty() || name.isEmpty() || birthdayUtil == null || phone.isEmpty() || apartmentID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin.");
            return;
        }

        // Kiểm tra hợp lệ
        if (!Validator.isValidCCCD(cccd)) {
            JOptionPane.showMessageDialog(this, "CCCD không hợp lệ.");
            return;
        }
        if (!Validator.isValidName(name)) {
            JOptionPane.showMessageDialog(this, "Họ tên không hợp lệ.");
            return;
        }
        if (!Validator.isValidPhoneNumber(phone)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ.");
            return;
        }
        if (!Validator.isValidName(birthPlace)) {
            JOptionPane.showMessageDialog(this, "Quê quán không hợp lệ.");
            return;
        }
        if (!Validator.isSafeText(idFamily)) {
            JOptionPane.showMessageDialog(this, "Sổ hộ khẩu không hợp lệ.");
            return;
        }

        // ** Kiểm tra trùng CCCD và mã căn hộ với cư dân khác **
        List<Resident> residentList = controller.getAllResidents();
        if (residentList != null) {
            for (Resident r : residentList) {
                // Bỏ qua chính cư dân đang cập nhật
                if (r.getCccd().equals(targetCCCD)) continue;

                if (r.getCccd().equals(cccd)) {
                    JOptionPane.showMessageDialog(this, "CCCD đã tồn tại với cư dân khác.");
                    return;
                }
                if (r.getApartmentID().equals(apartmentID)) {
                    JOptionPane.showMessageDialog(this, "Mã căn hộ đã có cư dân khác đang sử dụng.");
                    return;
                }
                if (r.getIDFamily().equals(idFamily)) {  
                    JOptionPane.showMessageDialog(this, "Số hộ khẩu đã tồn tại với cư dân khác.");
                    return;
                }
            }
        }

        String birthday = sdf.format(birthdayUtil);
        controller.updateResident(targetCCCD, name, birthday, "", idFamily, sex, "", birthPlace, phone, apartmentID);
        loadTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Cập nhật cư dân thành công.");

    }//GEN-LAST:event_butCapNhatActionPerformed

    private void butXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butXoaActionPerformed
        // Lấy danh sách CCCD cư dân hiện có
        List<String> cccdList = controller.getAllCCCD();
        if (cccdList == null || cccdList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có cư dân nào để xóa.");
            return;
        }

        // Chuyển List thành array để show dialog
        String[] cccdArray = cccdList.toArray(new String[0]);

        // Hiện hộp thoại chọn CCCD
        String selectedCCCD = (String) JOptionPane.showInputDialog(
                this,
                "Chọn CCCD của cư dân cần xóa:",
                "Chọn CCCD",
                JOptionPane.QUESTION_MESSAGE,
                null,
                cccdArray,
                cccdArray[0]
        );

        if (selectedCCCD == null) {
            // Người dùng bấm Cancel
            return;
        }

        // Kiểm tra cư dân có tồn tại không (phòng trường hợp danh sách có lỗi)
        Resident resident = controller.findByCCCD(selectedCCCD);
        if (resident == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy cư dân với CCCD đã chọn.");
            return;
        }

        // Xác nhận xóa
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa cư dân này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Thực hiện xóa
        controller.deleteResident(selectedCCCD);
        loadTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Xóa cư dân thành công.");
    }//GEN-LAST:event_butXoaActionPerformed

    private void butSapXepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSapXepActionPerformed
        String[] criteriaOptions = {"Họ tên", "Quê quán", "Mã căn hộ", "Sổ hộ khẩu"};
        String selectedCriteria = (String) JOptionPane.showInputDialog(
            this,
            "Chọn tiêu chí sắp xếp:",
            "Sắp xếp cư dân",
            JOptionPane.QUESTION_MESSAGE,
            null,
            criteriaOptions,
            criteriaOptions[0]
        );
        if (selectedCriteria == null) return;

        String[] orderOptions = {"Tăng dần", "Giảm dần"};
        String selectedOrder = (String) JOptionPane.showInputDialog(
            this,
            "Chọn thứ tự sắp xếp:",
            "Thứ tự sắp xếp",
            JOptionPane.QUESTION_MESSAGE,
            null,
            orderOptions,
            orderOptions[0]
        );
        if (selectedOrder == null) return;

        boolean ascending = selectedOrder.equals("Tăng dần");

        List<Resident> sortedList = controller.sort(selectedCriteria, ascending);

        tableModel.setRowCount(0); // Xóa bảng cũ
        int i = 1;
        for (Resident r : sortedList) {
            String birthDateStr;
            try {
                birthDateStr = (r.getBirthday() != null) ? sdf.format(sdf.parse(r.getBirthday())) : "Không rõ";
            } catch (Exception e) {
                birthDateStr = "Không rõ";
            }

            tableModel.addRow(new Object[]{
                i++,
                r.getName(),
                birthDateStr,
                r.getSex(),
                r.getCccd(),
                r.getPhoneNumber(),
                r.getApartmentID(),
                r.getBirthPlace(),
                r.getIDFamily()
            });
        }
    }//GEN-LAST:event_butSapXepActionPerformed

    private void butTimKiemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butTimKiemActionPerformed
        String[] options = {"Mã căn hộ", "Sổ hộ khẩu", "Họ tên", "CCCD"};
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Chọn tiêu chí tìm kiếm:",
                "Tìm kiếm cư dân",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (selected == null) return;

        String keyword = JOptionPane.showInputDialog(this, "Nhập từ khóa:");
        if (keyword == null || keyword.trim().isEmpty()) return;

        keyword = keyword.trim().toLowerCase();
        List<Resident> list = new ArrayList<>();

        switch (selected) {
            case "Mã căn hộ":
                list = controller.searchByApartmentID(keyword);
                break;
            case "Sổ hộ khẩu":
                list = controller.searchByIDFamily(keyword);
                break;
            case "Họ tên":
                for (Resident r : controller.getAllResidents()) {
                    if (r.getName().toLowerCase().contains(keyword)) {
                        list.add(r);
                    }
                }
                break;
            case "CCCD":
                Resident found = controller.findByCCCD(keyword);
                if (found != null) list.add(found);
                break;
        }

        // Hiển thị kết quả lên bảng
        tableModel.setRowCount(0);
        int i = 1;
        for (Resident r : list) {
            tableModel.addRow(new Object[]{
                i++, r.getName(), r.getBirthday(), r.getSex(), r.getCccd(),
                r.getPhoneNumber(), r.getApartmentID(), r.getBirthPlace(), r.getIDFamily()
            });
        }

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy cư dân nào phù hợp.");
        }
    }//GEN-LAST:event_butTimKiemActionPerformed

    private void butClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butClearActionPerformed
        clearForm();
    }//GEN-LAST:event_butClearActionPerformed

    private void butLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butLamMoiActionPerformed
        loadTable();
        clearForm();        
        JOptionPane.showMessageDialog(this, "Dữ liệu đã được làm mới.");
    }//GEN-LAST:event_butLamMoiActionPerformed

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

    private void txtCCCDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCCCDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCCCDActionPerformed

    private void txtSoHoKhauActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSoHoKhauActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSoHoKhauActionPerformed

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
            java.util.logging.Logger.getLogger(CuDanForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CuDanForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CuDanForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CuDanForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CuDanForm("Admin").setVisible(true);
            }
        });
    }
    private javax.swing.JTextField nameField, cccdField, addressField, phoneField, apartmentField, familyIDField, birthPlaceField;
    private javax.swing.JCheckBox maleCheck, femaleCheck;
    private com.toedter.calendar.JDateChooser birthDateChooser;
    private javax.swing.JTable BangCuDan;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable DanhSachCuDan;
    private javax.swing.JButton butCapNhat;
    private javax.swing.JButton butClear;
    private javax.swing.JButton butDangXuat;
    private javax.swing.JButton butDanhSachCuDan;
    private javax.swing.JButton butHinhThucThanhToan;
    private javax.swing.JButton butLamMoi;
    private javax.swing.JButton butQuanLyCanHo;
    private javax.swing.JButton butQuanLyDichVu;
    private javax.swing.JButton butQuanLyHoaDon;
    private javax.swing.JButton butSapXep;
    private javax.swing.JButton butThem;
    private javax.swing.JButton butThongKe;
    private javax.swing.JButton butThongTinToaNha;
    private javax.swing.JButton butTimKiem;
    private javax.swing.JButton butXoa;
    private javax.swing.JCheckBox checkNam;
    private javax.swing.JCheckBox checkNu;
    private javax.swing.JComboBox<String> comboHoVaTen;
    private javax.swing.JComboBox<String> comboMaCanHo;
    private com.toedter.calendar.JDateChooser dateNgaySinh;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbCCCD;
    private javax.swing.JLabel lbGioiTinh;
    private javax.swing.JLabel lbHoVaTen;
    private javax.swing.JLabel lbMaCanHo;
    private javax.swing.JLabel lbNgaySinh;
    private javax.swing.JLabel lbQueQuan;
    private javax.swing.JLabel lbSoHoKhau;
    private javax.swing.JLabel lbSĐT;
    private javax.swing.JTextField txtCCCD;
    private javax.swing.JTextField txtQueQuan;
    private javax.swing.JTextField txtSoHoKhau;
    private javax.swing.JTextField txtSĐT;
    // End of variables declaration//GEN-END:variables
}
