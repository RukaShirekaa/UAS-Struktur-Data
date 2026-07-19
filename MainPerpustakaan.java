import javax.swing.*;
import java.awt.*;

// ==========================================
// SISTEM MANAJEMEN PERPUSTAKAAN TERPADU (GUI)
// Terintegrasi dengan Syarat UAS Struktur Data
// ==========================================
public class MainPerpustakaan extends JFrame {

    static JTextArea areaOutput;
    static LinkedListKatalog perpustakaan = new LinkedListKatalog();
    
    static String[] kategoriBuku = {"Sastra Kiri & Pemikiran", "Manga & Komik", "Novel Fiksi", "Sastra Klasik", "Light Novel"};

    static void cetak(String teks) {
        areaOutput.append(teks + "\n");
    }

    // ==========================================
    // 1. STRUKTUR NODE UNTUK DOUBLE LINKED LIST (BUKU)
    // ==========================================
    static class Buku {
        String idBuku, judul, pengarang, kategori;
        int tahun;
        boolean isTersedia;
        Buku next;
        Buku prev;

        public Buku(String idBuku, String judul, String pengarang, int tahun, String kategori) {
            this.idBuku = idBuku;
            this.judul = judul;
            this.pengarang = pengarang;
            this.tahun = tahun;
            this.kategori = kategori;
            this.isTersedia = true;
            this.next = null;
            this.prev = null;
        }
    }

    // ==========================================
    // 2. QUEUE (ANTREAN RESERVASI BUKU)
    // ==========================================
    static class NodeReservasi {
        String namaPeminjam;
        NodeReservasi next;

        public NodeReservasi(String namaPeminjam) {
            this.namaPeminjam = namaPeminjam;
            this.next = null;
        }
    }

    static class QueueReservasi {
        NodeReservasi front, rear;

        public void enqueue(String nama) {
            NodeReservasi newNode = new NodeReservasi(nama);
            if (rear == null) {
                front = rear = newNode;
                return;
            }
            rear.next = newNode;
            rear = newNode;
        }

        public String dequeue() {
            if (front == null) return null;
            String nama = front.namaPeminjam;
            front = front.next;
            if (front == null) rear = null;
            return nama;
        }

        public boolean isEmpty() {
            return front == null;
        }
    }

    // ==========================================
    // 3. STACK (RIWAYAT TRANSAKSI & UNDO DELETE)
    // ==========================================
    
    static class NodeUndo {
        Buku bukuDihapus;
        NodeUndo next;
        
        public NodeUndo(Buku buku) {
            this.bukuDihapus = buku;
            this.next = null;
        }
    }

    static class StackUndo {
        NodeUndo top;
        
        public void push(Buku buku) {
            NodeUndo newNode = new NodeUndo(buku);
            newNode.next = top;
            top = newNode;
        }
        
        public Buku pop() {
            if (top == null) return null;
            Buku dikembalikan = top.bukuDihapus;
            top = top.next;
            return dikembalikan;
        }
    }

    static class NodeRiwayat {
        String catatan;
        NodeRiwayat next;

        public NodeRiwayat(String catatan) {
            this.catatan = catatan;
            this.next = null;
        }
    }

    static class StackRiwayat {
        NodeRiwayat top;
        public void push(String catatan) {
            NodeRiwayat newNode = new NodeRiwayat(catatan);
            newNode.next = top;
            top = newNode;
        }
        public void tampilkanRiwayat() {
            if (top == null) {
                cetak("Belum ada riwayat transaksi.");
                return;
            }
            NodeRiwayat current = top;
            cetak("\n--- Riwayat Transaksi Terakhir ---");
            while (current != null) {
                cetak("- " + current.catatan);
                current = current.next;
            }
        }
    }

    // ==========================================
    // 4. DOUBLE LINKED LIST (KATALOG BUKU UTAMA)
    // ==========================================
    static class LinkedListKatalog {
        Buku head;
        QueueReservasi antrean = new QueueReservasi();
        StackRiwayat riwayat = new StackRiwayat();
        StackUndo stackUndo = new StackUndo(); 

        public void injectData(String id, String judul, String pengarang, int tahun, String kategori) {
            Buku bukuBaru = new Buku(id, judul, pengarang, tahun, kategori);
            if (head == null) {
                head = bukuBaru;
            } else {
                Buku current = head;
                while (current.next != null) {
                    current = current.next;
                }
                current.next = bukuBaru;
                bukuBaru.prev = current; 
            }
        }

        public void tambahBuku(String id, String judul, String pengarang, int tahun, String kategori) {
            injectData(id, judul, pengarang, tahun, kategori);
            cetak("Sukses: Buku '" + judul + "' (" + kategori + ") berhasil ditambahkan!");
        }

        public void hapusBuku(String id) {
            if (head == null) {
                cetak("Katalog kosong, tidak ada yang bisa dihapus.");
                return;
            }
            Buku current = head;
            while (current != null) {
                if (current.idBuku.equals(id)) {
                    stackUndo.push(new Buku(current.idBuku, current.judul, current.pengarang, current.tahun, current.kategori));

                    if (current == head) {
                        head = current.next;
                        if (head != null) head.prev = null;
                    } else {
                        current.prev.next = current.next;
                        if (current.next != null) {
                            current.next.prev = current.prev;
                        }
                    }
                    cetak("Buku dengan ID '" + id + "' (" + current.judul + ") berhasil dihapus!");
                    return;
                }
                current = current.next;
            }
            cetak("Buku dengan ID '" + id + "' tidak ditemukan.");
        }

        public void undoDelete() {
            Buku bukuDikembalikan = stackUndo.pop(); 
            if (bukuDikembalikan == null) {
                cetak("Tidak ada data buku yang bisa di-undo.");
            } else {
                injectData(bukuDikembalikan.idBuku, bukuDikembalikan.judul, bukuDikembalikan.pengarang, bukuDikembalikan.tahun, bukuDikembalikan.kategori);
                cetak("Undo Berhasil! Buku '" + bukuDikembalikan.judul + "' (ID: " + bukuDikembalikan.idBuku + ") telah dikembalikan ke katalog.");
            }
        }

        public void traversalMaju() {
            if (head == null) {
                cetak("Katalog buku masih kosong.");
                return;
            }
            Buku current = head;
            cetak("\n--- Katalog Buku (Traversal Maju) ---");
            cetak(String.format("%-5s | %-30s | %-20s | %-5s | %-10s", "ID", "Judul", "Pengarang", "Tahun", "Status"));
            cetak("--------------------------------------------------------------------------------------");
            while (current != null) {
                String status = current.isTersedia ? "Tersedia" : "Dipinjam";
                cetak(String.format("%-5s | %-30s | %-20s | %-5d | %-10s", current.idBuku, current.judul, current.pengarang, current.tahun, status));
                current = current.next;
            }
        }

        public void traversalMundur() {
            if (head == null) {
                cetak("Katalog buku masih kosong.");
                return;
            }
            
            Buku current = head;
            while (current.next != null) {
                current = current.next;
            }

            cetak("\n--- Katalog Buku (Traversal Mundur) ---");
            cetak(String.format("%-5s | %-30s | %-20s | %-5s | %-10s", "ID", "Judul", "Pengarang", "Tahun", "Status"));
            cetak("--------------------------------------------------------------------------------------");
            while (current != null) {
                String status = current.isTersedia ? "Tersedia" : "Dipinjam";
                cetak(String.format("%-5s | %-30s | %-20s | %-5d | %-10s", current.idBuku, current.judul, current.pengarang, current.tahun, status));
                current = current.prev; 
            }
        }

        public void cariBuku(String judulDicari) { 
            Buku current = head;
            boolean ditemukan = false;
            while (current != null) {
                if (current.judul.toLowerCase().contains(judulDicari.toLowerCase())) {
                    cetak("\nHasil Pencarian:");
                    cetak("ID: " + current.idBuku + " | Judul: " + current.judul + " | Tahun: " + current.tahun + " | Status: " + (current.isTersedia ? "Tersedia" : "Dipinjam"));
                    ditemukan = true;
                }
                current = current.next;
            }
            if (!ditemukan) cetak("Buku dengan kata kunci '" + judulDicari + "' tidak ditemukan.");
        }

        public void urutkanBukuBerdasarkanTahun() { 
            if (head == null || head.next == null) return;
            boolean swapped;
            do {
                swapped = false;
                Buku current = head;
                Buku previous = null;
                while (current.next != null) {
                    if (current.tahun < current.next.tahun) {
                        Buku temp = current.next;
                        current.next = temp.next;
                        temp.next = current;
                        temp.prev = previous; 
                        current.prev = temp;
                        if (current.next != null) current.next.prev = current;
                        
                        if (previous == null) {
                            head = temp;
                        } else {
                            previous.next = temp;
                        }
                        previous = temp;
                        swapped = true;
                    } else {
                        previous = current;
                        current = current.next;
                    }
                }
            } while (swapped);
            cetak("Berhasil: Katalog telah diurutkan dari tahun terbit paling baru.");
        }

        public void pinjamBuku(String id, String nama) { 
            Buku current = head;
            while (current != null) {
                if (current.idBuku.equals(id)) {
                    if (current.isTersedia) {
                        current.isTersedia = false;
                        String catatan = nama + " meminjam buku: " + current.judul;
                        riwayat.push(catatan);
                        cetak("Berhasil! " + catatan);
                    } else {
                        cetak("Maaf, buku sedang dipinjam. " + nama + " dimasukkan ke antrean reservasi.");
                        antrean.enqueue(nama);
                        riwayat.push(nama + " masuk antrean reservasi untuk buku: " + current.judul);
                    }
                    return;
                }
                current = current.next;
            }
            cetak("Buku dengan ID " + id + " tidak ditemukan.");
        }

        public void kembalikanBuku(String id) { 
            Buku current = head;
            while (current != null) {
                if (current.idBuku.equals(id)) {
                    if (!current.isTersedia) {
                        current.isTersedia = true;
                        String catatan = "Buku '" + current.judul + "' dikembalikan.";
                        riwayat.push(catatan);
                        cetak(catatan);
                        
                        if (!antrean.isEmpty()) {
                            String nextPeminjam = antrean.dequeue();
                            cetak("-> Mengalihkan peminjaman kepada pengantre: " + nextPeminjam);
                            pinjamBuku(id, nextPeminjam);
                        }
                    } else {
                        cetak("Gagal: Buku ini masih berstatus tersedia di rak.");
                    }
                    return;
                }
                current = current.next;
            }
            cetak("Buku dengan ID " + id + " tidak ditemukan.");
        }

        public void lihatRiwayat() {
            riwayat.tampilkanRiwayat();
        }

        // Menampilkan seluruh buku beserta ringkasan jumlah
        public void lihatSemuaBuku() {
            if (head == null) {
                cetak("Katalog buku masih kosong.");
                return;
            }
            int total = 0, tersedia = 0, dipinjam = 0;
            Buku current = head;
            cetak("\n--- Daftar Semua Buku ---");
            cetak(String.format("%-5s | %-30s | %-20s | %-5s | %-10s", "ID", "Judul", "Pengarang", "Tahun", "Status"));
            cetak("--------------------------------------------------------------------------------------");
            while (current != null) {
                String status = current.isTersedia ? "Tersedia" : "Dipinjam";
                if (current.isTersedia) tersedia++; else dipinjam++;
                total++;
                cetak(String.format("%-5s | %-30s | %-20s | %-5d | %-10s", current.idBuku, current.judul, current.pengarang, current.tahun, status));
                current = current.next;
            }
            cetak("--------------------------------------------------------------------------------------");
            cetak("Total: " + total + " buku  |  Tersedia: " + tersedia + "  |  Dipinjam: " + dipinjam);
        }

        // Filter Linked List berdasarkan kategori (integrasi Array kategoriBuku)
        public void lihatBukuPerKategori(String kategori) {
            if (head == null) {
                cetak("Katalog buku masih kosong.");
                return;
            }
            int total = 0;
            Buku current = head;
            cetak("\n--- Buku Kategori: " + kategori + " ---");
            cetak(String.format("%-5s | %-30s | %-20s | %-5s | %-10s", "ID", "Judul", "Pengarang", "Tahun", "Status"));
            cetak("--------------------------------------------------------------------------------------");
            while (current != null) {
                if (current.kategori != null && current.kategori.equals(kategori)) {
                    String status = current.isTersedia ? "Tersedia" : "Dipinjam";
                    cetak(String.format("%-5s | %-30s | %-20s | %-5d | %-10s", current.idBuku, current.judul, current.pengarang, current.tahun, status));
                    total++;
                }
                current = current.next;
            }
            cetak("--------------------------------------------------------------------------------------");
            if (total == 0) {
                cetak("Tidak ada buku pada kategori '" + kategori + "'.");
            } else {
                cetak("Total: " + total + " buku pada kategori ini.");
            }
        }
    }

    // ==========================================
    // PALET WARNA & FONT (TEMA ELEGAN - DARK)
    // ==========================================
    static final Color BG_UTAMA   = new Color(30, 31, 43);
    static final Color BG_PANEL   = new Color(40, 42, 54);
    static final Color BG_TOMBOL  = new Color(52, 54, 70);
    static final Color BG_HOVER   = new Color(68, 71, 90);
    static final Color AKSEN      = new Color(139, 233, 253);
    static final Color AKSEN_HIJAU= new Color(80, 250, 123);
    static final Color AKSEN_ORANYE = new Color(255, 184, 108);
    static final Color AKSEN_MERAH  = new Color(255, 85, 85);
    static final Color AKSEN_UNGU   = new Color(189, 147, 249);
    static final Color TEKS_TERANG= new Color(248, 248, 242);
    static final Color TEKS_REDUP = new Color(150, 155, 175);

    // Tombol menu dengan bar aksen berwarna sesuai kategori + hover
    private JButton buatTombol(String teks, Color aksen) {
        JButton btn = new JButton(teks);
        btn.setFocusPainted(false);
        btn.setForeground(TEKS_TERANG);
        btn.setBackground(BG_TOMBOL);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, aksen),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(BG_HOVER);
                btn.setForeground(aksen);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(BG_TOMBOL);
                btn.setForeground(TEKS_TERANG);
            }
        });
        return btn;
    }

    // Label seksi kecil pemisah kelompok tombol
    private JLabel buatLabelSeksi(String teks) {
        JLabel lbl = new JLabel(teks);
        lbl.setForeground(TEKS_REDUP);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 4, 3, 0));
        return lbl;
    }

    // ==========================================
    // 5. SETUP ANTARMUKA GUI (SWING)
    // ==========================================
    public MainPerpustakaan() {
        setTitle("Sistem Manajemen Perpustakaan Terpadu");
        Dimension layar = Toolkit.getDefaultToolkit().getScreenSize();
        int lebar = Math.min(950, layar.width - 120);
        int tinggi = Math.min(720, layar.height - 120);
        setSize(lebar, tinggi);
        setMinimumSize(new Dimension(760, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_UTAMA);

        // --- Header Banner ---
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, AKSEN),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)));

        // Ikon buku (emoji) di kiri sebagai logo
        JLabel ikon = new JLabel("📚");
        ikon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        header.add(ikon, BorderLayout.WEST);

        JLabel lblJudul = new JLabel("SISTEM MANAJEMEN PERPUSTAKAAN");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblJudul.setForeground(AKSEN_HIJAU);

        JLabel subJudul = new JLabel("Double Linked List  •  Stack Undo Delete  •  Queue Reservasi  •  Traversal 2 Arah");
        subJudul.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subJudul.setForeground(TEKS_REDUP);

        JPanel teksHeader = new JPanel(new GridLayout(2, 1));
        teksHeader.setOpaque(false);
        teksHeader.add(lblJudul);
        teksHeader.add(subJudul);
        header.add(teksHeader, BorderLayout.CENTER);

        // Badge versi di kanan
        JLabel badge = new JLabel("Putri Canon");
        badge.setOpaque(true);
        badge.setBackground(BG_TOMBOL);
        badge.setForeground(AKSEN);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        header.add(badge, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- Area Output Teks ---
        areaOutput = new JTextArea();
        areaOutput.setEditable(false);
        areaOutput.setFont(new Font("Consolas", Font.PLAIN, 14)); 
        areaOutput.setBackground(BG_PANEL);
        areaOutput.setForeground(TEKS_TERANG);
        areaOutput.setCaretColor(TEKS_TERANG);
        areaOutput.setMargin(new Insets(12, 14, 12, 14));
        areaOutput.setLineWrap(false);

        JScrollPane scrollPane = new JScrollPane(areaOutput);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 8));
        scrollPane.getViewport().setBackground(BG_PANEL);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // Scrollbar tipis bergaya gelap
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                this.thumbColor = BG_HOVER;
                this.trackColor = BG_UTAMA;
            }
            protected JButton createDecreaseButton(int o) { return tombolNol(); }
            protected JButton createIncreaseButton(int o) { return tombolNol(); }
            private JButton tombolNol() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel Tombol Menu (dikelompokkan per kategori) ---
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setBackground(BG_UTAMA);
        panelMenu.setBorder(BorderFactory.createEmptyBorder(14, 7, 14, 16));

        // Warna aksen per kategori aksi
        JButton btnLihatSemua = buatTombol("  0.  Lihat Semua Buku",            AKSEN);
        JButton btnLihatMaju  = buatTombol("  1.  Traversal Maju",              AKSEN);
        JButton btnLihatMundur= buatTombol("  2.  Traversal Mundur",            AKSEN);
        JButton btnCari       = buatTombol("  6.  Cari Buku",                   AKSEN);
        JButton btnUrut       = buatTombol("  7.  Urutkan Buku",                AKSEN);

        JButton btnTambah     = buatTombol("  3.  Tambah Buku Baru",            AKSEN_HIJAU);
        JButton btnHapus      = buatTombol("  4.  Hapus Buku",                  AKSEN_MERAH);
        JButton btnUndo       = buatTombol("  5.  Undo Delete (Stack)",         AKSEN_UNGU);

        JButton btnPinjam     = buatTombol("  8.  Pinjam Buku",                 AKSEN_ORANYE);
        JButton btnKembali    = buatTombol("  9.  Kembalikan Buku",             AKSEN_ORANYE);
        JButton btnRiwayat    = buatTombol(" 10.  Riwayat Peminjaman",          AKSEN_UNGU);

        JButton btnKategori   = buatTombol(" 11.  Daftar Kategori (Array)",     TEKS_REDUP);
        JButton btnFilterKat  = buatTombol(" 12.  Lihat Buku per Kategori",     AKSEN_HIJAU);
        JButton btnBersihkan  = buatTombol("  ✕   Bersihkan Layar",            TEKS_REDUP);

        // Kelompok: Tampilan & Pencarian
        panelMenu.add(buatLabelSeksi("TAMPILAN & PENCARIAN"));
        panelMenu.add(btnLihatSemua);
        panelMenu.add(Box.createVerticalStrut(3));
        panelMenu.add(btnLihatMaju);
        panelMenu.add(Box.createVerticalStrut(3));
        panelMenu.add(btnLihatMundur);
        panelMenu.add(Box.createVerticalStrut(3));
        panelMenu.add(btnCari);
        panelMenu.add(Box.createVerticalStrut(3));
        panelMenu.add(btnUrut);

        // Kelompok: Manajemen Data
        panelMenu.add(buatLabelSeksi("MANAJEMEN DATA"));
        panelMenu.add(btnTambah);
        panelMenu.add(Box.createVerticalStrut(3));
        panelMenu.add(btnHapus);
        panelMenu.add(Box.createVerticalStrut(3));
        panelMenu.add(btnUndo);

        // Kelompok: Transaksi
        panelMenu.add(buatLabelSeksi("TRANSAKSI"));
        panelMenu.add(btnPinjam);
        panelMenu.add(Box.createVerticalStrut(3));
        panelMenu.add(btnKembali);
        panelMenu.add(Box.createVerticalStrut(3));
        panelMenu.add(btnRiwayat);

        // Kelompok: Lainnya
        panelMenu.add(buatLabelSeksi("LAINNYA"));
        panelMenu.add(btnKategori);
        panelMenu.add(Box.createVerticalStrut(3));
        panelMenu.add(btnFilterKat);
        panelMenu.add(Box.createVerticalStrut(3));
        panelMenu.add(btnBersihkan);
        panelMenu.add(Box.createVerticalGlue());

        // Bungkus menu dalam scroll pane agar semua tombol tetap terjangkau di layar kecil
        JScrollPane scrollMenu = new JScrollPane(panelMenu,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMenu.setBorder(null);
        scrollMenu.setPreferredSize(new Dimension(290, 0));
        scrollMenu.getViewport().setBackground(BG_UTAMA);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollMenu, BorderLayout.EAST);

        // --- Footer Status Bar ---
        JLabel footer = new JLabel("  Siap  •  Gunakan tombol menu di sebelah kanan");
        footer.setOpaque(true);
        footer.setBackground(BG_PANEL);
        footer.setForeground(new Color(150, 155, 175));
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
        add(footer, BorderLayout.SOUTH);

        // --- Event Listeners untuk Setiap Tombol ---

        btnLihatSemua.addActionListener(e -> {
            areaOutput.setText("");
            perpustakaan.lihatSemuaBuku();
        });

        btnLihatMaju.addActionListener(e -> {
            areaOutput.setText("");
            perpustakaan.traversalMaju();
        });

        btnLihatMundur.addActionListener(e -> {
            areaOutput.setText(""); 
            perpustakaan.traversalMundur();
        });

        btnTambah.addActionListener(e -> {
            try {
                String id = JOptionPane.showInputDialog(this, "Masukkan ID Buku:");
                if (id == null || id.isEmpty()) return;
                
                String judul = JOptionPane.showInputDialog(this, "Masukkan Judul Buku:");
                if (judul == null || judul.isEmpty()) return;
                
                String pengarang = JOptionPane.showInputDialog(this, "Masukkan Pengarang:");
                if (pengarang == null || pengarang.isEmpty()) return;
                
                String tahunStr = JOptionPane.showInputDialog(this, "Masukkan Tahun Terbit (Angka):");
                if (tahunStr == null || tahunStr.isEmpty()) return;
                
                int tahun = Integer.parseInt(tahunStr);

                // Pilih kategori dari Array kategoriBuku (wajib pilih, tidak bisa ketik bebas)
                String kategori = (String) JOptionPane.showInputDialog(
                        this,
                        "Pilih Kategori Buku:",
                        "Kategori",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        kategoriBuku,
                        kategoriBuku[0]);
                if (kategori == null) return;

                perpustakaan.tambahBuku(id, judul, pengarang, tahun, kategori);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tahun terbit harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnHapus.addActionListener(e -> {
            String idHapus = JOptionPane.showInputDialog(this, "Masukkan ID Buku yang akan dihapus:");
            if (idHapus != null && !idHapus.isEmpty()) {
                perpustakaan.hapusBuku(idHapus);
            }
        });

        btnUndo.addActionListener(e -> {
            perpustakaan.undoDelete();
        });

        btnCari.addActionListener(e -> {
            String cari = JOptionPane.showInputDialog(this, "Masukkan Judul Buku yang dicari:");
            if (cari != null && !cari.isEmpty()) {
                areaOutput.setText("");
                perpustakaan.cariBuku(cari);
            }
        });

        btnUrut.addActionListener(e -> {
            areaOutput.setText("");
            perpustakaan.urutkanBukuBerdasarkanTahun();
            perpustakaan.traversalMaju(); 
        });

        btnPinjam.addActionListener(e -> {
            String idPinjam = JOptionPane.showInputDialog(this, "Masukkan ID Buku yang dipinjam:");
            if (idPinjam == null || idPinjam.isEmpty()) return;
            String nama = JOptionPane.showInputDialog(this, "Masukkan Nama Peminjam:");
            if (nama == null || nama.isEmpty()) return;
            perpustakaan.pinjamBuku(idPinjam, nama);
        });

        btnKembali.addActionListener(e -> {
            String idKembali = JOptionPane.showInputDialog(this, "Masukkan ID Buku yang dikembalikan:");
            if (idKembali != null && !idKembali.isEmpty()) {
                perpustakaan.kembalikanBuku(idKembali);
            }
        });

        btnRiwayat.addActionListener(e -> {
            areaOutput.setText("");
            perpustakaan.lihatRiwayat();
        });

        btnKategori.addActionListener(e -> {
            areaOutput.setText("");
            cetak("\n--- Kategori Buku Tersedia (Statis Array) ---");
            for (int i = 0; i < kategoriBuku.length; i++) {
                cetak("[" + (i + 1) + "] " + kategoriBuku[i]);
            }
        });

        btnFilterKat.addActionListener(e -> {
            // Pilih kategori dari Array, lalu filter isi Linked List
            String kategori = (String) JOptionPane.showInputDialog(
                    this,
                    "Pilih Kategori untuk difilter:",
                    "Lihat Buku per Kategori",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    kategoriBuku,
                    kategoriBuku[0]);
            if (kategori == null) return;
            areaOutput.setText("");
            perpustakaan.lihatBukuPerKategori(kategori);
        });

        btnBersihkan.addActionListener(e -> areaOutput.setText(""));
    }

    // ==========================================
    // MAIN METHOD
    // ==========================================
    public static void main(String[] args) {
        // PERUBAHAN: Injeksi Data Dummy (Buku Kiri, Manga, dan Novel)
        perpustakaan.injectData("K01", "Madilog", "Tan Malaka", 1943, "Sastra Kiri & Pemikiran");
        perpustakaan.injectData("K02", "Das Kapital", "Karl Marx", 1867, "Sastra Kiri & Pemikiran");
        perpustakaan.injectData("M01", "One Piece Vol. 1", "Eiichiro Oda", 1997, "Manga & Komik");
        perpustakaan.injectData("M02", "Berserk Vol. 1", "Kentaro Miura", 1989, "Manga & Komik");
        perpustakaan.injectData("N01", "Bumi Manusia", "Pramoedya A. Toer", 1980, "Novel Fiksi");
        perpustakaan.injectData("N02", "Cantik Itu Luka", "Eka Kurniawan", 2002, "Novel Fiksi");

        SwingUtilities.invokeLater(() -> {
            MainPerpustakaan app = new MainPerpustakaan();
            app.setVisible(true);
            cetak("Selamat Datang di Sistem Manajemen Perpustakaan Terpadu.");
        });
    }
}