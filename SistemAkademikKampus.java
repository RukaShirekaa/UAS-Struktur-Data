import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class SistemAkademikKampus extends JFrame {

    // ---- Struktur data inti ----
    private final LinkedListMahasiswa daftar = new LinkedListMahasiswa();
    private final QueueKRS antrianKRS = new QueueKRS();

    // ---- File penyimpanan data ----
    private static final String FILE_DATA = "data_mahasiswa.txt";

    // ---- Komponen GUI ----
    private DefaultTableModel tableModel;
    private JTable tabelMahasiswa;
    private JTextField tfNim, tfNama, tfIpk, tfCari;
    private JComboBox<String> cbJurusan;
    private JComboBox<String> cbModeCari;
    private JTextArea logArea;

    // Pilihan jurusan tetap
    private static final String[] JURUSAN = {
        "Teknik Informatika", "Teknologi Rekayasa Multimedia", "Teknik Otomotif"
    };
    private DefaultListModel<String> modelAntrian;
    private JList<String> listAntrian;
    private DefaultListModel<String> modelRiwayat;
    private JList<String> listRiwayat;
    private JLabel lblStatus;

    // Font global yang lebih rapi
    private static final Font FONT_UI = new Font("Segoe UI", Font.PLAIN, 13);

    // Katalog mata kuliah untuk simulasi KRS: nama + SKS
    private static final String[] KRS_MATKUL = {
        "Struktur Data", "Basis Data", "Pemrograman Web",
        "Jaringan Komputer", "Kecerdasan Buatan", "Sistem Operasi"
    };
    private static final int[] KRS_SKS = { 3, 3, 2, 3, 3, 2 };

    public SistemAkademikKampus() {
        setTitle("Sistem Akademik Kampus - Struktur Data");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buatHeader(), BorderLayout.NORTH);
        add(buatPanelTengah(), BorderLayout.CENTER);
        add(buatPanelKanan(), BorderLayout.EAST);
        add(buatStatusBar(), BorderLayout.SOUTH);

        // Load data dari file jika ada, kalau tidak pakai seed
        if (!loadDariFile()) {
            seedData();
        }
        refreshTabel();
    }

    // ========================= HEADER / DASHBOARD =========================
    private JPanel buatHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(33, 64, 120));
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel judul = new JLabel("SISTEM AKADEMIK KAMPUS");
        judul.setFont(new Font("Segoe UI", Font.BOLD, 22));
        judul.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Linked List - Stack - Queue - Sorting - Searching");
        sub.setForeground(new Color(200, 210, 235));
        JPanel teks = new JPanel(new GridLayout(2, 1));
        teks.setOpaque(false);
        teks.add(judul);
        teks.add(sub);
        p.add(teks, BorderLayout.WEST);

        // Tombol Save / Load di kanan header
        JPanel aksiFile = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        aksiFile.setOpaque(false);
        JButton bSave = new JButton("Simpan Data");
        JButton bLoad = new JButton("Muat Data");
        bSave.addActionListener(e -> aksiSimpan());
        bLoad.addActionListener(e -> aksiMuat());
        aksiFile.add(bSave);
        aksiFile.add(bLoad);
        p.add(aksiFile, BorderLayout.EAST);
        return p;
    }

    // ========================= PANEL TENGAH (form + tabel) =========================
    private JPanel buatPanelTengah() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));

        // --- Form input mahasiswa ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Form Data Mahasiswa"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        tfNim = new JTextField(10);
        tfNama = new JTextField(14);
        cbJurusan = new JComboBox<>(JURUSAN);
        tfIpk = new JTextField(6);

        int r = 0;
        addFormRow(form, g, r++, "NIM (8 digit):", tfNim, "Nama:", tfNama);
        addFormRow(form, g, r++, "Jurusan:", cbJurusan, "IPK (max 4.00):", tfIpk);

        JPanel tombolForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton bTambah = new JButton("Tambah");
        JButton bEdit = new JButton("Edit");
        JButton bHapus = new JButton("Hapus");
        JButton bDetail = new JButton("Detail");
        JButton bClear = new JButton("Bersihkan");
        bTambah.addActionListener(e -> aksiTambah());
        bEdit.addActionListener(e -> aksiEdit());
        bHapus.addActionListener(e -> aksiHapus());
        bDetail.addActionListener(e -> tampilkanDetail(mahasiswaTerpilih()));
        bClear.addActionListener(e -> bersihkanForm());
        tombolForm.add(bTambah);
        tombolForm.add(bEdit);
        tombolForm.add(bHapus);
        tombolForm.add(bDetail);
        tombolForm.add(bClear);
        g.gridx = 0; g.gridy = r; g.gridwidth = 4;
        form.add(tombolForm, g);

        // --- Toolbar cari + sorting ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolbar.setBorder(BorderFactory.createTitledBorder("Cari & Ranking"));
        cbModeCari = new JComboBox<>(new String[]{"NIM (Sequential)", "NIM (Binary)", "Nama (Sequential)"});
        tfCari = new JTextField(14);
        JButton bCari = new JButton("Cari");
        JButton bReset = new JButton("Tampil Semua");
        JButton bRanking = new JButton("Sorting / Ranking IPK");
        bCari.addActionListener(e -> aksiCari());
        bReset.addActionListener(e -> refreshTabel());
        bRanking.addActionListener(e -> aksiRanking());
        toolbar.add(new JLabel("Mode:"));
        toolbar.add(cbModeCari);
        toolbar.add(tfCari);
        toolbar.add(bCari);
        toolbar.add(bReset);
        toolbar.add(bRanking);

        JPanel atas = new JPanel(new BorderLayout());
        atas.add(form, BorderLayout.CENTER);
        atas.add(toolbar, BorderLayout.SOUTH);

        // --- Tabel data mahasiswa ---
        tableModel = new DefaultTableModel(
                new String[]{"Rank", "NIM", "Nama", "Jurusan", "IPK", "Status KRS", "Jml Nilai"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabelMahasiswa = new JTable(tableModel);
        tabelMahasiswa.setRowHeight(26);
        tabelMahasiswa.setFont(FONT_UI);
        tabelMahasiswa.getTableHeader().setReorderingAllowed(false);
        tabelMahasiswa.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        // Renderer pewarnaan IPK (kolom 4)
        tabelMahasiswa.setDefaultRenderer(Object.class, new IpkColorRenderer());
        // Klik baris tampilkan detail
        tabelMahasiswa.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                Mahasiswa m = mahasiswaTerpilih();
                if (m == null) return;
                isiFormDari(m); // isi form untuk kemudahan edit
                if (e.getClickCount() == 2) tampilkanDetail(m); // double klik = popup detail
            }
        });
        JScrollPane sp = new JScrollPane(tabelMahasiswa);
        sp.setBorder(BorderFactory.createTitledBorder("Tabel Data Mahasiswa (klik baris = pilih, dobel klik = detail)"));

        wrap.add(atas, BorderLayout.NORTH);
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private void addFormRow(JPanel form, GridBagConstraints g, int row,
                            String l1, JComponent c1, String l2, JComponent c2) {
        g.gridwidth = 1;
        g.gridx = 0; g.gridy = row; g.weightx = 0; form.add(new JLabel(l1), g);
        g.gridx = 1; g.weightx = 1; form.add(c1, g);
        g.gridx = 2; g.weightx = 0; form.add(new JLabel(l2), g);
        g.gridx = 3; g.weightx = 1; form.add(c2, g);
    }

    // ========================= PANEL KANAN (Queue + Stack + Log) =========================
    private JPanel buatPanelKanan() {
        JPanel p = new JPanel(new GridLayout(3, 1, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));
        p.setPreferredSize(new Dimension(340, 0));

        // --- Panel Antrian KRS (Queue) ---
        JPanel panelKRS = new JPanel(new BorderLayout(4, 4));
        panelKRS.setBorder(BorderFactory.createTitledBorder("Antrian KRS (Queue - FIFO)"));
        modelAntrian = new DefaultListModel<>();
        listAntrian = new JList<>(modelAntrian);
        listAntrian.setFont(FONT_UI);
        panelKRS.add(new JScrollPane(listAntrian), BorderLayout.CENTER);
        JPanel tombolKRS = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JButton bMasuk = new JButton("Masuk Antrian");
        JButton bProses = new JButton("Proses KRS");
        bMasuk.addActionListener(e -> aksiMasukAntrian());
        bProses.addActionListener(e -> aksiProsesKRS());
        tombolKRS.add(bMasuk);
        tombolKRS.add(bProses);
        panelKRS.add(tombolKRS, BorderLayout.SOUTH);

        // --- Panel Riwayat Nilai (Stack) ---
        JPanel panelNilai = new JPanel(new BorderLayout(4, 4));
        panelNilai.setBorder(BorderFactory.createTitledBorder("Riwayat Nilai (Stack - LIFO)"));
        modelRiwayat = new DefaultListModel<>();
        listRiwayat = new JList<>(modelRiwayat);
        listRiwayat.setFont(FONT_UI);
        panelNilai.add(new JScrollPane(listRiwayat), BorderLayout.CENTER);
        JPanel tombolNilai = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JButton bInput = new JButton("Input Nilai");
        JButton bUndo = new JButton("Undo (Pop)");
        JButton bLihat = new JButton("Lihat Riwayat");
        bInput.addActionListener(e -> aksiInputNilai());
        bUndo.addActionListener(e -> aksiUndoNilai());
        bLihat.addActionListener(e -> aksiLihatRiwayat());
        tombolNilai.add(bInput);
        tombolNilai.add(bUndo);
        tombolNilai.add(bLihat);
        panelNilai.add(tombolNilai, BorderLayout.SOUTH);

        // --- Panel Log Aktivitas ---
        JPanel panelLog = new JPanel(new BorderLayout(4, 4));
        panelLog.setBorder(BorderFactory.createTitledBorder("Log Aktivitas"));
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(120, 230, 120));
        panelLog.add(new JScrollPane(logArea), BorderLayout.CENTER);
        JButton bClearLog = new JButton("Bersihkan Log");
        bClearLog.addActionListener(e -> logArea.setText(""));
        panelLog.add(bClearLog, BorderLayout.SOUTH);

        p.add(panelKRS);
        p.add(panelNilai);
        p.add(panelLog);
        return p;
    }

    private JPanel buatStatusBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createEtchedBorder());
        lblStatus = new JLabel("Siap.");
        p.add(lblStatus);
        return p;
    }

    // ========================= AKSI: CRUD =========================
    private void aksiTambah() {
        String nim = tfNim.getText().trim();
        String nama = tfNama.getText().trim();
        String jurusan = (String) cbJurusan.getSelectedItem();
        String ipkStr = tfIpk.getText().trim();

        // ---- Validasi ketat ----
        if (nim.isEmpty() || nama.isEmpty() || ipkStr.isEmpty()) {
            pesanGagal("Semua field wajib diisi.");
            return;
        }
        if (!nim.matches("\\d{8}")) {
            pesanGagal("NIM harus 8 digit angka. Contoh: 23110001");
            return;
        }
        if (nama.isEmpty()) {
            pesanGagal("Nama tidak boleh kosong.");
            return;
        }
        double ipk;
        try {
            ipk = Double.parseDouble(ipkStr);
        } catch (NumberFormatException ex) {
            pesanGagal("IPK harus berupa angka. Contoh: 3.50");
            return;
        }
        if (ipk < 0 || ipk > 4.00) {
            pesanGagal("IPK harus di rentang 0.00 - 4.00.");
            return;
        }
        if (daftar.cariByNim(nim) != null) {
            pesanGagal("NIM '" + nim + "' sudah terdaftar. NIM harus unik.");
            return;
        }
        daftar.tambah(new Mahasiswa(nim, nama, jurusan, ipk));
        refreshTabel(); // AUTO SORTING: ranking otomatis ter-update
        bersihkanForm();
        log("Tambah mahasiswa: " + nama + " (" + nim + ")");
        pesanSukses("Mahasiswa '" + nama + "' berhasil ditambahkan.");
    }

    private void aksiEdit() {
        Mahasiswa m = mahasiswaTerpilih();
        if (m == null) {
            pesanGagal("Pilih baris mahasiswa di tabel dulu.");
            return;
        }
        String nama = tfNama.getText().trim();
        String jurusan = (String) cbJurusan.getSelectedItem();
        String ipkStr = tfIpk.getText().trim();

        if (nama.isEmpty()) {
            pesanGagal("Nama tidak boleh kosong.");
            return;
        }
        double ipk;
        try {
            ipk = Double.parseDouble(ipkStr);
        } catch (NumberFormatException ex) {
            pesanGagal("IPK harus berupa angka. Contoh: 3.50");
            return;
        }
        if (ipk < 0 || ipk > 4.00) {
            pesanGagal("IPK harus di rentang 0.00 - 4.00.");
            return;
        }
        // NIM tidak diubah (identitas unik), hanya nama/jurusan/IPK
        m.setNama(nama);
        m.setJurusan(jurusan);
        m.setIpk(ipk);
        refreshTabel(); // AUTO SORTING setelah edit IPK
        log("Edit mahasiswa: " + m.getNim() + " -> IPK " + String.format("%.2f", ipk));
        pesanSukses("Data mahasiswa '" + nama + "' berhasil diperbarui.");
    }

    private void aksiHapus() {
        Mahasiswa m = mahasiswaTerpilih();
        if (m == null) {
            pesanGagal("Pilih baris mahasiswa di tabel dulu.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this,
                "Hapus mahasiswa " + m.getNama() + " (" + m.getNim() + ")?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        daftar.hapus(m.getNim());
        refreshTabel();
        log("Hapus mahasiswa: " + m.getNama() + " (" + m.getNim() + ")");
        pesanSukses("Data '" + m.getNama() + "' terhapus.");
    }

    private void bersihkanForm() {
        tfNim.setText("");
        tfNama.setText("");
        cbJurusan.setSelectedIndex(0);
        tfIpk.setText("");
        tfNim.requestFocus();
    }

    /** Isi form dari mahasiswa terpilih (memudahkan edit). */
    private void isiFormDari(Mahasiswa m) {
        tfNim.setText(m.getNim());
        tfNama.setText(m.getNama());
        cbJurusan.setSelectedItem(m.getJurusan());
        tfIpk.setText(String.format("%.2f", m.getIpk()));
    }

    // ========================= DETAIL MAHASISWA =========================
    private void tampilkanDetail(Mahasiswa m) {
        if (m == null) {
            pesanGagal("Pilih baris mahasiswa di tabel dulu.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("NIM        : ").append(m.getNim()).append("\n");
        sb.append("Nama       : ").append(m.getNama()).append("\n");
        sb.append("Jurusan    : ").append(m.getJurusan()).append("\n");
        sb.append("IPK        : ").append(String.format("%.2f", m.getIpk())).append("\n");
        sb.append("Status KRS : ").append(m.getStatusKRS()).append("\n");
        if (!m.getMatkulKRS().isEmpty()) {
            sb.append("Matkul KRS : ").append(m.getMatkulKRS().replace(";", ", ")).append("\n");
            sb.append("Total SKS  : ").append(m.getTotalSks()).append("\n");
        }
        sb.append("\nRiwayat Nilai (Stack, terbaru di atas):\n");
        double[] nilai = m.getRiwayatNilai().toArray();
        if (nilai.length == 0) {
            sb.append("  (belum ada nilai)\n");
        } else {
            for (int i = 0; i < nilai.length; i++) {
                sb.append(String.format("  %d. %.1f%s%n", i + 1, nilai[i],
                        i == 0 ? "  <- terbaru" : ""));
            }
        }

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 13));
        ta.setBackground(new Color(248, 248, 248));
        JScrollPane sc = new JScrollPane(ta);
        sc.setPreferredSize(new Dimension(420, 300));
        JOptionPane.showMessageDialog(this, sc,
                "Detail Mahasiswa - " + m.getNama(), JOptionPane.INFORMATION_MESSAGE);
    }

    // ========================= AKSI: SEARCHING =========================
    private void aksiCari() {
        String key = tfCari.getText().trim();
        if (key.isEmpty()) {
            pesanGagal("Masukkan kata kunci pencarian.");
            return;
        }
        int mode = cbModeCari.getSelectedIndex();
        if (mode == 0) { // Sequential by NIM
            Mahasiswa m = daftar.cariByNim(key);
            tampilkanHasilCari(m == null ? new Mahasiswa[0] : new Mahasiswa[]{m}, false);
            lblStatus.setText("Sequential Search NIM: " + (m == null ? "tidak ditemukan" : "ditemukan"));
        } else if (mode == 1) { // Binary by NIM
            Mahasiswa m = daftar.binarySearchByNim(key);
            tampilkanHasilCari(m == null ? new Mahasiswa[0] : new Mahasiswa[]{m}, false);
            lblStatus.setText("Binary Search NIM: " + (m == null ? "tidak ditemukan" : "ditemukan"));
        } else { // Sequential by Nama
            Mahasiswa[] hasil = daftar.cariByNama(key);
            tampilkanHasilCari(hasil, false);
            lblStatus.setText("Sequential Search Nama: " + hasil.length + " hasil");
        }
    }

    // ========================= AKSI: SORTING =========================
    private void aksiRanking() {
        Mahasiswa[] ranked = daftar.sortByIpkDesc();
        isiTabel(ranked, true);
        lblStatus.setText("Ranking berdasarkan IPK (Bubble Sort). Total " + ranked.length + " mahasiswa.");
        pesanSukses("Data diurutkan berdasarkan IPK tertinggi.");
    }

    // ========================= AKSI: QUEUE (KRS) =========================
    private void aksiMasukAntrian() {
        Mahasiswa m = mahasiswaTerpilih();
        if (m == null) {
            pesanGagal("Pilih mahasiswa di tabel untuk masuk antrian.");
            return;
        }
        antrianKRS.enqueue(m);
        refreshAntrian();
        log(m.getNama() + " masuk antrian KRS");
        pesanSukses(m.getNama() + " masuk antrian KRS.");
    }

    /** Simulasi KRS realistis: user pilih mata kuliah lewat checkbox, sistem hitung SKS. */
    private void aksiProsesKRS() {
        Mahasiswa m = antrianKRS.dequeue();
        if (m == null) {
            pesanGagal("Antrian KRS kosong.");
            return;
        }

        // Panel pemilihan mata kuliah (checkbox)
        JPanel panel = new JPanel(new GridLayout(0, 1, 2, 2));
        panel.add(new JLabel("Pilih mata kuliah untuk " + m.getNama() + ":"));
        JCheckBox[] cek = new JCheckBox[KRS_MATKUL.length];
        for (int i = 0; i < KRS_MATKUL.length; i++) {
            cek[i] = new JCheckBox(KRS_MATKUL[i] + " (" + KRS_SKS[i] + " SKS)");
            panel.add(cek[i]);
        }

        int ok = JOptionPane.showConfirmDialog(this, panel,
                "Proses KRS (FIFO) - " + m.getNim(), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            // batal: kembalikan ke depan antrian tidak didukung; enqueue ulang di belakang
            antrianKRS.enqueue(m);
            refreshAntrian();
            lblStatus.setText("Proses KRS dibatalkan. Mahasiswa dikembalikan ke antrian.");
            return;
        }

        StringBuilder matkul = new StringBuilder();
        int totalSks = 0;
        int jml = 0;
        for (int i = 0; i < cek.length; i++) {
            if (cek[i].isSelected()) {
                if (matkul.length() > 0) matkul.append(";");
                matkul.append(KRS_MATKUL[i]);
                totalSks += KRS_SKS[i];
                jml++;
            }
        }

        if (jml == 0) {
            pesanGagal("Tidak ada mata kuliah dipilih. KRS dibatalkan.");
            antrianKRS.enqueue(m);
            refreshAntrian();
            return;
        }

        // Simpan hasil KRS ke mahasiswa
        m.setMatkulKRS(matkul.toString());
        m.setTotalSks(totalSks);
        m.setStatusKRS("Sudah KRS");

        refreshAntrian();
        refreshTabel();
        log("KRS diproses: " + m.getNama() + " ambil " + jml + " matkul (" + totalSks + " SKS)");

        String isi = "KRS BERHASIL DIPROSES\n\n"
                + "Mahasiswa : " + m.getNama() + " (" + m.getNim() + ")\n"
                + "Mata kuliah:\n  - " + matkul.toString().replace(";", "\n  - ") + "\n"
                + "Total SKS : " + totalSks + "\n"
                + "Status    : Sudah KRS";
        JOptionPane.showMessageDialog(this, isi, "Proses KRS (FIFO)", JOptionPane.INFORMATION_MESSAGE);
        lblStatus.setText("KRS diproses (FIFO). Sisa antrian: " + antrianKRS.size());
    }

    // ========================= AKSI: STACK (Nilai) =========================
    private void aksiInputNilai() {
        Mahasiswa m = mahasiswaTerpilih();
        if (m == null) {
            pesanGagal("Pilih mahasiswa di tabel untuk input nilai.");
            return;
        }
        String s = JOptionPane.showInputDialog(this,
                "Input nilai untuk " + m.getNama() + " (0-100):", "Input Nilai",
                JOptionPane.QUESTION_MESSAGE);
        if (s == null) return; // batal
        double nilai;
        try {
            nilai = Double.parseDouble(s.trim());
        } catch (NumberFormatException ex) {
            pesanGagal("Nilai harus angka.");
            return;
        }
        if (nilai < 0 || nilai > 100) {
            pesanGagal("Nilai harus 0 - 100.");
            return;
        }
        m.getRiwayatNilai().push(nilai); // PUSH ke stack
        refreshTabel();
        refreshRiwayat(m);
        log("Input nilai " + nilai + " untuk " + m.getNama());
        pesanSukses("Nilai " + nilai + " ditambahkan (push).");
    }

    private void aksiUndoNilai() {
        Mahasiswa m = mahasiswaTerpilih();
        if (m == null) {
            pesanGagal("Pilih mahasiswa di tabel.");
            return;
        }
        Double dihapus = m.getRiwayatNilai().pop(); // POP dari stack
        if (dihapus == null) {
            pesanGagal("Riwayat nilai kosong, tidak ada yang di-undo.");
            return;
        }
        refreshTabel();
        refreshRiwayat(m);
        log("Undo nilai " + dihapus + " milik " + m.getNama());
        pesanSukses("Nilai terakhir (" + dihapus + ") dihapus (pop/undo).");
    }

    private void aksiLihatRiwayat() {
        Mahasiswa m = mahasiswaTerpilih();
        if (m == null) {
            pesanGagal("Pilih mahasiswa di tabel.");
            return;
        }
        refreshRiwayat(m);
        lblStatus.setText("Riwayat nilai " + m.getNama() + ": " + m.getRiwayatNilai().size() + " entri.");
    }

    // ========================= SAVE & LOAD (FILE HANDLING) =========================
    /**
     * Simpan seluruh data mahasiswa ke file teks.
     * Format 1 baris per mahasiswa (dipisah '|'):
     *   nim|nama|jurusan|ipk|statusKRS|totalSks|matkulKRS|n1,n2,n3
     * Nilai disimpan dari dasar ke atas agar urutan stack tetap benar saat load.
     */
    private void aksiSimpan() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_DATA))) {
            Mahasiswa[] arr = daftar.toArray();
            for (Mahasiswa m : arr) {
                // Ambil nilai dari atas ke bawah lalu balik jadi bawah ke atas
                double[] atasKeBawah = m.getRiwayatNilai().toArray();
                StringBuilder nilaiStr = new StringBuilder();
                for (int i = atasKeBawah.length - 1; i >= 0; i--) {
                    if (nilaiStr.length() > 0) nilaiStr.append(",");
                    nilaiStr.append(atasKeBawah[i]);
                }
                bw.write(String.join("|",
                        m.getNim(),
                        m.getNama(),
                        m.getJurusan(),
                        String.valueOf(m.getIpk()),
                        m.getStatusKRS(),
                        String.valueOf(m.getTotalSks()),
                        m.getMatkulKRS(),
                        nilaiStr.toString()));
                bw.newLine();
            }
            log("Simpan data ke file: " + FILE_DATA + " (" + arr.length + " mahasiswa)");
            pesanSukses("Data berhasil disimpan ke " + FILE_DATA);
        } catch (IOException ex) {
            pesanGagal("Gagal menyimpan file: " + ex.getMessage());
        }
    }

    private void aksiMuat() {
        if (loadDariFile()) {
            refreshTabel();
            pesanSukses("Data berhasil dimuat dari " + FILE_DATA);
        } else {
            pesanGagal("File " + FILE_DATA + " tidak ditemukan atau kosong.");
        }
    }

    /** Load data dari file. Return true jika berhasil membaca minimal 1 baris. */
    private boolean loadDariFile() {
        File f = new File(FILE_DATA);
        if (!f.exists()) return false;
        boolean adaData = false;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            daftar.kosongkan();
            String baris;
            while ((baris = br.readLine()) != null) {
                if (baris.trim().isEmpty()) continue;
                String[] p = baris.split("\\|", -1);
                if (p.length < 4) continue; // baris rusak, lewati
                String nim = p[0];
                String nama = p[1];
                String jurusan = p[2];
                double ipk;
                try { ipk = Double.parseDouble(p[3]); } catch (NumberFormatException e) { continue; }

                Mahasiswa m = new Mahasiswa(nim, nama, jurusan, ipk);
                if (p.length > 4) m.setStatusKRS(p[4]);
                if (p.length > 5) {
                    try { m.setTotalSks(Integer.parseInt(p[5])); } catch (NumberFormatException ignored) {}
                }
                if (p.length > 6) m.setMatkulKRS(p[6]);
                if (p.length > 7 && !p[7].isEmpty()) {
                    for (String n : p[7].split(",")) {
                        try { m.getRiwayatNilai().push(Double.parseDouble(n)); }
                        catch (NumberFormatException ignored) {}
                    }
                }
                daftar.tambah(m);
                adaData = true;
            }
            if (adaData) log("Muat data dari file: " + FILE_DATA);
        } catch (IOException ex) {
            return false;
        }
        return adaData;
    }

    // ========================= REFRESH / RENDER =========================
    private void refreshTabel() {
        isiTabel(daftar.sortByIpkDesc(), true); // AUTO SORTING (ranking otomatis)
    }

    private void isiTabel(Mahasiswa[] arr, boolean pakaiRank) {
        tableModel.setRowCount(0);
        for (int i = 0; i < arr.length; i++) {
            Mahasiswa m = arr[i];
            tableModel.addRow(new Object[]{
                    pakaiRank ? (i + 1) : "-",
                    m.getNim(), m.getNama(), m.getJurusan(),
                    String.format("%.2f", m.getIpk()),
                    m.getStatusKRS(),
                    m.getRiwayatNilai().size()
            });
        }
    }

    private void tampilkanHasilCari(Mahasiswa[] arr, boolean pakaiRank) {
        if (arr.length == 0) {
            pesanGagal("Data tidak ditemukan.");
        }
        isiTabel(arr, pakaiRank);
    }

    private void refreshAntrian() {
        modelAntrian.clear();
        Mahasiswa[] arr = antrianKRS.toArray();
        int i = 1;
        for (Mahasiswa m : arr) {
            modelAntrian.addElement(i++ + ". " + m.getNama() + " (" + m.getNim() + ")");
        }
        if (arr.length == 0) modelAntrian.addElement("(antrian kosong)");
    }

    private void refreshRiwayat(Mahasiswa m) {
        modelRiwayat.clear();
        double[] nilai = m.getRiwayatNilai().toArray(); // atas ke bawah
        if (nilai.length == 0) {
            modelRiwayat.addElement("(belum ada nilai)");
            return;
        }
        for (int i = 0; i < nilai.length; i++) {
            String label = (i == 0) ? "   <- TOP (terbaru)" : "";
            modelRiwayat.addElement(String.format("%.1f%s", nilai[i], label));
        }
    }

    // ========================= HELPER =========================
    private Mahasiswa mahasiswaTerpilih() {
        int row = tabelMahasiswa.getSelectedRow();
        if (row < 0) return null;
        String nim = String.valueOf(tableModel.getValueAt(row, 1));
        return daftar.cariByNim(nim);
    }

    /** Tambah 1 baris ke log aktivitas dengan format [LOG] ... */
    private void log(String pesan) {
        logArea.append("[LOG] " + pesan + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void pesanSukses(String s) {
        lblStatus.setText(s);
        JOptionPane.showMessageDialog(this, s, "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    private void pesanGagal(String s) {
        lblStatus.setText("Gagal: " + s);
        JOptionPane.showMessageDialog(this, s, "Gagal", JOptionPane.ERROR_MESSAGE);
    }

    private void seedData() {
        daftar.tambah(new Mahasiswa("23110001", "Budi Santoso", "Teknik Informatika", 3.75));
        daftar.tambah(new Mahasiswa("23110002", "Siti Aminah", "Teknologi Rekayasa Multimedia", 3.90));
        daftar.tambah(new Mahasiswa("23110003", "Andi Wijaya", "Teknik Informatika", 3.45));
        daftar.tambah(new Mahasiswa("23110004", "Dewi Lestari", "Teknik Otomotif", 3.60));
        daftar.tambah(new Mahasiswa("23110005", "Rizki Pratama", "Teknologi Rekayasa Multimedia", 2.20));
        daftar.tambah(new Mahasiswa("23110006", "Nurul Hidayah", "Teknik Otomotif", 3.85));
        Mahasiswa m = daftar.cariByNim("23110001");
        if (m != null) { m.getRiwayatNilai().push(85); m.getRiwayatNilai().push(90); }
    }

    // ========================= RENDERER: WARNA IPK =========================
    /**
     * Renderer tabel untuk mewarnai baris berdasarkan IPK:
     *  - IPK >= 3.5 : hijau
     *  - IPK <  2.5 : merah
     *  - selain itu : normal
     */
    private static class IpkColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            // Kolom 4 = IPK
            double ipk = -1;
            try {
                ipk = Double.parseDouble(String.valueOf(table.getValueAt(row, 4)));
            } catch (Exception ignored) {}

            if (!isSelected) {
                if (ipk >= 3.5) {
                    c.setBackground(new Color(210, 245, 210)); // hijau muda
                } else if (ipk >= 0 && ipk < 2.5) {
                    c.setBackground(new Color(250, 210, 210)); // merah muda
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }

    // ========================= MAIN =========================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new SistemAkademikKampus().setVisible(true));
    }
}

/* ============================================================================
 *  CLASS PENDUKUNG - digabung dalam satu file (non-public).
 *  Di Java, hanya SATU class public per file (harus sama dengan nama file).
 *  Class lain boleh non-public dalam file yang sama.
 * ============================================================================ */

/**
 * Mahasiswa - entitas data mahasiswa.
 * NIM unik, nama, jurusan, IPK, riwayat nilai (Stack), status KRS.
 */
class Mahasiswa {
    private String nim;
    private String nama;
    private String jurusan;
    private double ipk;
    private final StackNilai riwayatNilai;

    private String statusKRS = "Belum KRS";
    private String matkulKRS = "";  // dipisah ";"
    private int totalSks = 0;

    public Mahasiswa(String nim, String nama, String jurusan, double ipk) {
        this.nim = nim;
        this.nama = nama;
        this.jurusan = jurusan;
        this.ipk = ipk;
        this.riwayatNilai = new StackNilai();
    }

    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getJurusan() { return jurusan; }
    public void setJurusan(String jurusan) { this.jurusan = jurusan; }

    public double getIpk() { return ipk; }
    public void setIpk(double ipk) { this.ipk = ipk; }

    public StackNilai getRiwayatNilai() { return riwayatNilai; }

    public String getStatusKRS() { return statusKRS; }
    public void setStatusKRS(String statusKRS) { this.statusKRS = statusKRS; }

    public String getMatkulKRS() { return matkulKRS; }
    public void setMatkulKRS(String matkulKRS) { this.matkulKRS = matkulKRS; }

    public int getTotalSks() { return totalSks; }
    public void setTotalSks(int totalSks) { this.totalSks = totalSks; }

    @Override
    public String toString() {
        return String.format("%s - %s (%s) IPK: %.2f", nim, nama, jurusan, ipk);
    }
}

/**
 * Node - simpul untuk Linked List mahasiswa.
 */
class Node {
    Mahasiswa data;
    Node next;

    public Node(Mahasiswa data) {
        this.data = data;
        this.next = null;
    }
}

/**
 * LinkedListMahasiswa - Singly Linked List penyimpan data mahasiswa.
 *  - LINKED LIST : penyimpanan utama (tambah/hapus dinamis)
 *  - SORTING     : Bubble Sort berdasarkan IPK
 *  - SEARCHING   : Sequential Search (NIM/Nama) + Binary Search (NIM)
 */
class LinkedListMahasiswa {
    private Node head;
    private int size;

    /** Tambah mahasiswa ke akhir list. NIM wajib unik. */
    public boolean tambah(Mahasiswa m) {
        if (cariByNim(m.getNim()) != null) return false; // NIM duplikat ditolak
        Node baru = new Node(m);
        if (head == null) {
            head = baru;
        } else {
            Node cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = baru;
        }
        size++;
        return true;
    }

    /** Hapus mahasiswa berdasarkan NIM. */
    public boolean hapus(String nim) {
        if (head == null) return false;
        if (head.data.getNim().equals(nim)) {
            head = head.next;
            size--;
            return true;
        }
        Node cur = head;
        while (cur.next != null && !cur.next.data.getNim().equals(nim)) {
            cur = cur.next;
        }
        if (cur.next == null) return false;
        cur.next = cur.next.next;
        size--;
        return true;
    }

    public int size() { return size; }
    public boolean isEmpty() { return head == null; }

    /** Kosongkan seluruh list (dipakai saat load data dari file). */
    public void kosongkan() {
        head = null;
        size = 0;
    }

    // ============================ SEARCHING ============================

    /** SEQUENTIAL SEARCH berdasarkan NIM. O(n). */
    public Mahasiswa cariByNim(String nim) {
        Node cur = head;
        while (cur != null) {
            if (cur.data.getNim().equals(nim)) return cur.data;
            cur = cur.next;
        }
        return null;
    }

    /** SEQUENTIAL SEARCH berdasarkan Nama (case-insensitive, cocok sebagian). O(n). */
    public Mahasiswa[] cariByNama(String nama) {
        Mahasiswa[] sementara = new Mahasiswa[size];
        int jml = 0;
        String key = nama.toLowerCase();
        Node cur = head;
        while (cur != null) {
            if (cur.data.getNama().toLowerCase().contains(key)) {
                sementara[jml++] = cur.data;
            }
            cur = cur.next;
        }
        Mahasiswa[] hasil = new Mahasiswa[jml];
        System.arraycopy(sementara, 0, hasil, 0, jml);
        return hasil;
    }

    /** BINARY SEARCH berdasarkan NIM. Array diurutkan dulu (Bubble Sort). O(log n). */
    public Mahasiswa binarySearchByNim(String nim) {
        Mahasiswa[] arr = toArray();
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j].getNim().compareTo(arr[j + 1].getNim()) > 0) {
                    Mahasiswa t = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = t;
                }
            }
        }
        int low = 0, high = arr.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = arr[mid].getNim().compareTo(nim);
            if (cmp == 0) return arr[mid];
            if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }

    // ============================ SORTING ============================

    /** BUBBLE SORT berdasarkan IPK (descending) untuk ranking. O(n^2). */
    public Mahasiswa[] sortByIpkDesc() {
        Mahasiswa[] arr = toArray();
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j].getIpk() < arr[j + 1].getIpk()) {
                    Mahasiswa t = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = t;
                }
            }
        }
        return arr;
    }

    // ============================ UTIL ============================

    /** Salin seluruh data linked list ke array. */
    public Mahasiswa[] toArray() {
        Mahasiswa[] arr = new Mahasiswa[size];
        Node cur = head;
        int i = 0;
        while (cur != null) {
            arr[i++] = cur.data;
            cur = cur.next;
        }
        return arr;
    }
}

/**
 * QueueKRS - Queue (FIFO) untuk simulasi antrian pengambilan KRS.
 */
class QueueKRS {

    private static class QNode {
        Mahasiswa data;
        QNode next;
        QNode(Mahasiswa data) { this.data = data; }
    }

    private QNode head; // depan (dilayani lebih dulu)
    private QNode tail; // belakang
    private int size;

    public void enqueue(Mahasiswa m) {
        QNode baru = new QNode(m);
        if (tail == null) {
            head = tail = baru;
        } else {
            tail.next = baru;
            tail = baru;
        }
        size++;
    }

    public Mahasiswa dequeue() {
        if (isEmpty()) return null;
        Mahasiswa m = head.data;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return m;
    }

    public Mahasiswa peek() {
        return isEmpty() ? null : head.data;
    }

    public boolean isEmpty() { return head == null; }

    public int size() { return size; }

    /** Kembalikan array mahasiswa dari depan ke belakang antrian. */
    public Mahasiswa[] toArray() {
        Mahasiswa[] arr = new Mahasiswa[size];
        QNode cur = head;
        int i = 0;
        while (cur != null) {
            arr[i++] = cur.data;
            cur = cur.next;
        }
        return arr;
    }
}

/**
 * StackNilai - Stack (LIFO) untuk menyimpan riwayat nilai mahasiswa.
 */
class StackNilai {

    private static class SNode {
        double nilai;
        SNode next;
        SNode(double nilai) { this.nilai = nilai; }
    }

    private SNode top;
    private int size;

    public void push(double nilai) {
        SNode baru = new SNode(nilai);
        baru.next = top;
        top = baru;
        size++;
    }

    public Double pop() {
        if (isEmpty()) return null;
        double nilai = top.nilai;
        top = top.next;
        size--;
        return nilai;
    }

    public Double peek() {
        if (isEmpty()) return null;
        return top.nilai;
    }

    public boolean isEmpty() { return top == null; }

    public int size() { return size; }

    /** Kembalikan array nilai dari puncak (terbaru) ke dasar (terlama). */
    public double[] toArray() {
        double[] arr = new double[size];
        SNode cur = top;
        int i = 0;
        while (cur != null) {
            arr[i++] = cur.nilai;
            cur = cur.next;
        }
        return arr;
    }
}
