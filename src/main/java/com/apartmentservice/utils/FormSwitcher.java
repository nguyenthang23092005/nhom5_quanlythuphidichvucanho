package com.apartmentservice.utils;
/**
 *
 * @author Nguyen Van Thang, Pham Van Su, Dang Anh Tuyen
 */
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class FormSwitcher {
    // Chuyển form nếu khác form hiện tại
    public static void switchForm(JFrame current, JFrame next) {
        if (current.getClass().equals(next.getClass())) {
            JOptionPane.showMessageDialog(current,
                    "Bạn đang ở giao diện này rồi.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        current.dispose(); // Đóng form hiện tại
        next.setLocationRelativeTo(null); // Căn giữa màn hình
        next.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Đóng khi bấm nút X
        next.setVisible(true); // Hiển thị form mới
    }
}
