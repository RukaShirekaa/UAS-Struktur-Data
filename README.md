# Sistem Akademik Kampus

Sistem Akademik Kampus adalah aplikasi desktop berbasis Java Swing yang digunakan untuk mengelola data mahasiswa secara sederhana. Aplikasi ini dibuat untuk mempelajari penerapan struktur data seperti Linked List, Queue, Stack, Sorting, dan Searching dalam sebuah sistem akademik.

## Fitur Utama

- Menambah data mahasiswa
- Mengedit data mahasiswa
- Menghapus data mahasiswa
- Melihat detail mahasiswa
- Mencari mahasiswa berdasarkan NIM atau nama
- Mengurutkan data mahasiswa berdasarkan IPK (ranking)
- Mengelola antrian KRS menggunakan Queue (FIFO)
- Mengelola riwayat nilai menggunakan Stack (LIFO)
- Menyimpan dan memuat data ke file teks

## Struktur Data yang Digunakan

- Linked List: untuk menyimpan data mahasiswa secara dinamis
- Queue: untuk simulasi antrian KRS
- Stack: untuk riwayat nilai mahasiswa
- Bubble Sort: untuk sorting berdasarkan IPK
- Sequential Search dan Binary Search: untuk pencarian data

## Teknologi

- Java
- Swing GUI
- File I/O untuk penyimpanan data

## Cara Menjalankan

1. Pastikan Java sudah terinstal di komputer Anda.
2. Buka terminal atau command prompt di folder project.
3. Jalankan perintah berikut:

```bash
javac SistemAkademikKampus.java
java SistemAkademikKampus
```

## File Data

Aplikasi akan menyimpan data mahasiswa ke file:

- data_mahasiswa.txt

Jika file tersebut belum ada, aplikasi akan membuat data awal contoh secara otomatis.

## Deskripsi Singkat Menu

- Tambah: menambahkan mahasiswa baru
- Edit: mengubah data mahasiswa yang dipilih
- Hapus: menghapus mahasiswa yang dipilih
- Detail: menampilkan informasi lengkap mahasiswa
- Cari: mencari data mahasiswa berdasarkan mode pencarian
- Sorting / Ranking IPK: mengurutkan mahasiswa berdasarkan IPK tertinggi
- Masuk Antrian: menambahkan mahasiswa ke antrian KRS
- Proses KRS: memproses antrian KRS secara FIFO
- Input Nilai / Undo: menambah atau menghapus riwayat nilai mahasiswa menggunakan stack

## Penulis

Project ini dibuat sebagai bagian dari tugas UAS mata kuliah Pemrograman/Algoritma dan Struktur Data.
