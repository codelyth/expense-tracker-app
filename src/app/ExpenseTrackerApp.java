package app;

import io.CsvStorage;
import model.Transaction;
import ui.ChartPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseTrackerApp extends JFrame{
	// --- Arayüz Bileşenleri ---
    private JTextField txtDate;
    private JComboBox<String> cmbCategory;
    private JTextField txtAmount;
    private JTextField txtNote;
    
    // Filtre Alanları
    private JComboBox<String> cmbFilterMonth;
    private JTextField txtFilterYear;

    private JTable table;
    private DefaultTableModel tableModel;
    private ChartPanel chartPanel;

    private List<Transaction> allTransactions;
    private List<Transaction> displayedTransactions;
    
    private final String FILE_PATH = "expenses.csv";

    public ExpenseTrackerApp() {
        allTransactions = new ArrayList<>();
        displayedTransactions = new ArrayList<>();

        setTitle("Harcama Takip Uygulaması");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. ÜST PANEL ---
        JPanel topContainerPanel = new JPanel();
        topContainerPanel.setLayout(new BoxLayout(topContainerPanel, BoxLayout.Y_AXIS));

        // A) Veri Giriş
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Yeni Harcama Ekle"));
        
        txtDate = new JTextField(LocalDate.now().toString(), 10);
        String[] categories = {"Gıda", "Ulaşım", "Eğlence", "Fatura", "Giyim", "Diğer"};
        cmbCategory = new JComboBox<>(categories);
        txtAmount = new JTextField(8);
        txtNote = new JTextField(15);

        inputPanel.add(new JLabel("Tarih:"));
        inputPanel.add(txtDate);
        inputPanel.add(new JLabel("Kategori:"));
        inputPanel.add(cmbCategory);
        inputPanel.add(new JLabel("Tutar:"));
        inputPanel.add(txtAmount);
        inputPanel.add(new JLabel("Not:"));
        inputPanel.add(txtNote);
        
        // B) Filtre Paneli
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Harcamaları Filtrele"));
        filterPanel.setBackground(new Color(240, 248, 255)); 

        String[] months = {"Tümü", "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", 
                           "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};
        cmbFilterMonth = new JComboBox<>(months);
        
        txtFilterYear = new JTextField("", 6);

        filterPanel.add(new JLabel("Ay:"));
        filterPanel.add(cmbFilterMonth);
        filterPanel.add(new JLabel("Yıl (Boş=Hepsi):"));
        filterPanel.add(txtFilterYear);
        
        JButton btnClearFilter = new JButton("Filtreyi Temizle");
        btnClearFilter.addActionListener(e -> {
            cmbFilterMonth.setSelectedIndex(0);
            txtFilterYear.setText("");
            loadAndFilterTransactions();
        });
        filterPanel.add(btnClearFilter);

        topContainerPanel.add(inputPanel);
        topContainerPanel.add(filterPanel);

        add(topContainerPanel, BorderLayout.NORTH);

        // --- 2. ORTA PANEL ---
        String[] columnNames = {"Seç", "Tarih", "Kategori", "Tutar", "Not"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setMaxWidth(40);

        JScrollPane tableScrollPane = new JScrollPane(table);

        chartPanel = new ChartPanel();
        chartPanel.setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, chartPanel);
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);

        // --- 3. ALT PANEL ---
        JPanel buttonPanel = new JPanel();
        JButton btnAdd = new JButton("Ekle");
        JButton btnSave = new JButton("Kaydet");
        JButton btnRefresh = new JButton("Listeyi Yenile / Filtrele");
        JButton btnDeleteSelected = new JButton("Seçilenleri Sil");
        JButton btnClearAll = new JButton("TÜMÜNÜ SİL");
        
        // AYLIK ÖZET
        JButton btnMonthSummary = new JButton("Aylık Özet");
        btnMonthSummary.setBackground(new Color(0, 153, 153)); // Turkuaz/Mavi tonu
        btnMonthSummary.setForeground(Color.BLACK);

        // Renk Ayarları
        btnDeleteSelected.setForeground(Color.BLACK);
        btnDeleteSelected.setBackground(Color.ORANGE.darker());
        btnClearAll.setForeground(Color.BLACK);
        btnClearAll.setBackground(Color.RED); 

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnDeleteSelected);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnMonthSummary); // Butonu panele ekle
        buttonPanel.add(btnSave);
        buttonPanel.add(btnClearAll);

        add(buttonPanel, BorderLayout.SOUTH);

        // --- İşlevler ---
        btnAdd.addActionListener(e -> addTransaction());

        btnSave.addActionListener(e -> {
            saveData();
            JOptionPane.showMessageDialog(this, "Veriler manuel olarak kaydedildi.");
        });

        btnRefresh.addActionListener(e -> {
            loadAndFilterTransactions();
            JOptionPane.showMessageDialog(this, "Liste güncellendi.");
        });
        
        btnMonthSummary.addActionListener(e -> showMonthSummary());

        btnDeleteSelected.addActionListener(e -> deleteSelectedTransactions());

        btnClearAll.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(this, 
                    "DİKKAT: Tüm kayıtlar kalıcı olarak silinecek!\nDevam etmek istiyor musunuz?", 
                    "Verileri Sil", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                deleteAllData();
            }
        });

        loadAndFilterTransactions();
        checkEndOfMonthAlert();
    }

    // --- AYLIK ÖZET PENCERESİ ---
    private void showMonthSummary() {
        // Hangi ayı baz alacağız? (Filtredeki mi, Şu anki mi?)
        int selectedMonthIndex = cmbFilterMonth.getSelectedIndex(); // 0=Tümü, 1=Ocak...
        int targetYear = -1;
        int targetMonth = -1;
        
        LocalDate today = LocalDate.now();

        // 1. Hedef Ay ve Yılı Belirle
        if (selectedMonthIndex == 0) {
            // Filtre "Tümü" ise -> Varsayılan olarak bugünün tarihini al
            targetMonth = today.getMonthValue();
            targetYear = today.getYear();
        } else {
            // Filtre seçiliyse -> Filtredeki değerleri al
            targetMonth = selectedMonthIndex;
            try {
                targetYear = Integer.parseInt(txtFilterYear.getText().trim());
            } catch (NumberFormatException e) {
                targetYear = today.getYear(); // Yıl girilmediyse mevcut yılı al
            }
        }

        // 2. Verileri Hesapla
        final int fYear = targetYear;
        final int fMonth = targetMonth;

        // O aydaki tüm harcamalar
        List<Transaction> monthTransactions = allTransactions.stream()
                .filter(t -> t.getDate().getYear() == fYear && t.getDate().getMonthValue() == fMonth)
                .collect(Collectors.toList());

        double totalMonth = monthTransactions.stream().mapToDouble(Transaction::getAmount).sum();

        // O aydaki "Bugüne Kadarki" harcamalar
        // Eğer hedef ay, şu an içinde bulunduğumuz ay ise anlamlıdır.
        // Eğer geçmiş bir ay ise (Örn: Geçen yılın Ocak ayı), "bugüne kadar" demek ayın tamamı demektir.
        double totalUpToToday = 0;
        
        boolean isCurrentActiveMonth = (fYear == today.getYear() && fMonth == today.getMonthValue());
        
        if (isCurrentActiveMonth) {
            // Sadece ayın 1'inden bugüne (dahil) olanlar
            totalUpToToday = monthTransactions.stream()
                    .filter(t -> !t.getDate().isAfter(today)) 
                    .mapToDouble(Transaction::getAmount)
                    .sum();
        } else {
            // Geçmiş veya gelecek ay ise toplam tutarı gösterir
            totalUpToToday = totalMonth;
        }

        // 3. Mesajı Hazırla
        String monthName = java.time.Month.of(fMonth).getDisplayName(TextStyle.FULL, new Locale("tr", "TR"));
        
        StringBuilder msg = new StringBuilder();
        msg.append("DÖNEM: ").append(monthName).append(" ").append(fYear).append("\n\n");
        
        if (isCurrentActiveMonth) {
            msg.append("📅 Bugüne (Ayın ").append(today.getDayOfMonth()).append("'ine) Kadar:  ");
            msg.append(String.format("%.2f TL", totalUpToToday)).append("\n");
            msg.append("--------------------------------------\n");
        }
        
        msg.append("💰 Ayın Genel Toplamı:  ");
        msg.append(String.format("%.2f TL", totalMonth));

        JOptionPane.showMessageDialog(this, msg.toString(), "Aylık Harcama Özeti", JOptionPane.INFORMATION_MESSAGE);
    }

    private void checkEndOfMonthAlert() {
        LocalDate today = LocalDate.now();
        // Test etmek isterseniz bu satırı açın: LocalDate today = LocalDate.of(2025, 1, 31); 

        if (today.getDayOfMonth() == today.lengthOfMonth()) {
            double currentMonthTotal = allTransactions.stream()
                    .filter(t -> t.getDate().getYear() == today.getYear() && 
                                 t.getDate().getMonth() == today.getMonth())
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            
            String monthName = today.getMonth().getDisplayName(TextStyle.FULL, new Locale("tr", "TR"));
            String message = String.format("Dikkat! Bugün %s ayının son günü.\n\n" +
                                           "Bu ayki toplam harcamanız:\n" +
                                           "--> %.2f TL <--\n\n" +
                                           "Bütçenizi kontrol etmeyi unutmayın!", 
                                           monthName, currentMonthTotal);

            JOptionPane.showMessageDialog(this, message, "Ay Sonu Özeti", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addTransaction() {
        try {
            LocalDate date = LocalDate.parse(txtDate.getText());
            String category = (String) cmbCategory.getSelectedItem();
            double amount = Double.parseDouble(txtAmount.getText().replace(",", "."));
            String note = txtNote.getText();

            Transaction t = new Transaction(date, category, amount, note);
            allTransactions.add(t);
            saveData();
            
            cmbFilterMonth.setSelectedIndex(0); 
            txtFilterYear.setText("");          
            
            loadAndFilterTransactions();

            txtAmount.setText("");
            txtNote.setText("");

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Hatalı tarih formatı! (yyyy-MM-dd)");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lütfen tutar kısmına geçerli bir sayı girin.");
        }
    }

    private void deleteSelectedTransactions() {
        List<Transaction> toRemove = new ArrayList<>();
        boolean anySelected = false;

        for (int i = 0; i < table.getRowCount(); i++) {
            Boolean isChecked = (Boolean) table.getValueAt(i, 0);
            if (isChecked != null && isChecked) {
                toRemove.add(displayedTransactions.get(i));
                anySelected = true;
            }
        }

        if (!anySelected) {
            JOptionPane.showMessageDialog(this, "Lütfen silmek için en az bir kayıt seçin.");
            return;
        }

        int response = JOptionPane.showConfirmDialog(this, 
                "Seçili " + toRemove.size() + " kaydı silmek istediğinize emin misiniz?", 
                "Seçilenleri Sil", 
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            allTransactions.removeAll(toRemove);
            saveData();
            loadAndFilterTransactions();
            JOptionPane.showMessageDialog(this, "Seçili kayıtlar silindi.");
        }
    }

    private void deleteAllData() {
        allTransactions.clear();
        saveData();
        loadAndFilterTransactions();
        JOptionPane.showMessageDialog(this, "Tüm veriler başarıyla silindi.");
    }

    private void saveData() {
        CsvStorage.save(allTransactions, FILE_PATH);
    }

    private void loadAndFilterTransactions() {
        allTransactions = CsvStorage.load(FILE_PATH);
        
        String selectedMonth = (String) cmbFilterMonth.getSelectedItem();
        String yearText = txtFilterYear.getText().trim();
        boolean filterByYear = !yearText.isEmpty();
        
        int year = -1;
        if (filterByYear) {
            try {
                year = Integer.parseInt(yearText);
            } catch (NumberFormatException e) {
                filterByYear = false; 
            }
        }
        
        int finalYear = year;
        boolean finalFilterByYear = filterByYear;
        int monthIndex = cmbFilterMonth.getSelectedIndex();

        displayedTransactions = allTransactions.stream()
            .filter(t -> {
                boolean yearMatch = !finalFilterByYear || (t.getDate().getYear() == finalYear);
                boolean monthMatch = (monthIndex == 0) || (t.getDate().getMonthValue() == monthIndex);
                return yearMatch && monthMatch;
            }).collect(Collectors.toList());

        displayedTransactions.sort(Comparator.comparing(Transaction::getDate));

        tableModel.setRowCount(0);
        for (Transaction t : displayedTransactions) {
            tableModel.addRow(new Object[]{false, t.getDate(), t.getCategory(), t.getAmount(), t.getNote()});
        }
        
        updateChart();
    }

    private void updateChart() {
        Map<String, Double> totals = new HashMap<>();
        for (Transaction t : displayedTransactions) {
            totals.put(t.getCategory(), totals.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
        }
        chartPanel.setData(totals);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ExpenseTrackerApp app = new ExpenseTrackerApp();
            app.setVisible(true);
        });
    }
}
