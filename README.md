# 💰 Harcama Takip Uygulaması (Expense Tracker)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Swing-blue?style=for-the-badge)
![Status](https://img.shields.io/badge/Durum-Tamamlandı-success?style=for-the-badge)

Java Swing kullanılarak geliştirilmiş, verileri yerel bir CSV dosyasında saklayan, filtreleme, raporlama ve grafiksel analiz yeteneklerine sahip kapsamlı bir kişisel finans takip uygulamasıdır.

## 🚀 Öne Çıkan Özellikler

Bu proje, temel veri girişinin ötesinde kullanıcı deneyimini artıran özellikler sunar:

* **📊 Dinamik Grafik Paneli:** Harcamalarınızı kategorilere göre (Gıda, Ulaşım, Fatura vb.) **sabit renk kodlarıyla** ayrılmış sütun grafikleriyle görselleştirin.
* **💾 Kalıcı Depolama (CSV):** Veriler `expenses.csv` dosyasında tutulur. Uygulama kapansa bile verileriniz güvendedir.
* **🔍 Gelişmiş Filtreleme:**
    * **Ay ve Yıl Bazlı:** Belirli bir dönemi incelemek için filtreleri kullanın.
    * **Varsayılan Görünüm:** Filtre uygulanmadığında tüm geçmişi görüntüleyin.
    * **Otomatik Odak:** Yeni veri eklendiğinde filtreler otomatik temizlenir, böylece eklenen veri anında görünür.
* **📈 Akıllı Aylık Özet:**
    * İstediğiniz ayın toplam harcamasını tek tıkla görün.
    * İçinde bulunduğumuz ayda iseniz, **"Bugüne kadarki (Current Day)"** harcama tutarını görerek bütçenizi kontrol edin.
* **🔔 Otomatik Uyarılar:**
    * **Ay Sonu Bildirimi:** Uygulama, ayın son gününde açıldığında o ayın toplam harcamasını otomatik bir pop-up ile bildirir.
* **🗑️ Veri Yönetimi:**
    * **Çoklu Silme:** Tablo üzerinden seçim kutucukları (Checkbox) ile birden fazla harcamayı seçip silebilirsiniz.
    * **Tümünü Sil:** Güvenlik onaylı "Tümünü Sil" butonu ile veritabanını sıfırlayabilirsiniz.

## 📂 Proje Yapısı

Proje, MVC (Model-View-Controller) prensiplerine uygun olarak paketlere ayrılmıştır:

```text
src/
├── app/
│   └── ExpenseTrackerApp.java  // 🏁 Ana uygulama, Arayüz (GUI) ve Kontrolcü mantığı.
├── model/
│   └── Transaction.java        // 📦 Veri modeli (Tarih, Kategori, Tutar, Not).
├── ui/
│   └── ChartPanel.java         // 🎨 Özel grafik çizim bileşeni (JPanel extend eder).
└── io/
    └── CsvStorage.java         // 💾 Dosya okuma/yazma işlemleri (File I/O).

🛠️ Kurulum ve Çalıştırma
Proje standart Java kütüphaneleri (JDK 8 ve üzeri) kullanır, harici bir bağımlılık gerektirmez.

1.Projeyi Klonlayın:
Bash
git clone [https://github.com/kullaniciadi/harcama-takip.git](https://github.com/kullaniciadi/harcama-takip.git)

2.IDE ile Açın: IntelliJ IDEA, Eclipse veya VS Code ile projeyi açın.

3.Çalıştırın: src/app/ExpenseTrackerApp.java sınıfını çalıştırın.

🎨 Renk Paleti
Grafikler, göz yormayan modern "Flat UI" renkleri kullanılarak tasarlanmıştır:

🍎 Gıda: Mat Kırmızı (#E74C3C)

🚌 Ulaşım: Mat Mavi (#3498DB)

🎬 Eğlence: Mat Mor (#9B59B6)

📄 Fatura: Mat Yeşil (#2ECC71)

👕 Giyim: Mat Sarı (#F1C40F)

⚙️ Diğer: Mat Gri (#95A5A6)
```

Bu proje eğitim amaçlı geliştirilmiştir.
