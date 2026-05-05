# 🔴 RedNet v1.0.0

[TR] RedNet, Minecraft sunucuları için geliştirilmiş, fiziksel kablo kirliliğini ortadan kaldıran ve uzak mesafeler arasında kızıltaş sinyali taşımayı sağlayan profesyonel bir eklentidir.

[EN] RedNet is a professional Minecraft plugin that eliminates physical cable clutter and enables wireless redstone signal transmission over long distances.

---

## ✨ Özellikler / Features

*   **Kablosuz Bağlantı / Wireless Connection:** Bloklar arasında sınırsız mesafede sinyal iletimi. / Signal transmission across unlimited distances.
*   **Gelişmiş Devre Türleri / Advanced Circuit Types:**
    *   `Quantum`: Birebir kopya / Exact signal copy.
    *   `Toggle`: Her sinyalde durum değiştirme / Toggle state on pulse.
    *   `Impulse`: Kısa süreli tetikleme / Short pulse signal.
    *   `Random`: %50 şans faktörü / 50% chance factor.
    *   `Reverse`: Sinyal tersleyici / NOT Gate (Inverter).
*   **Gecikme Desteği / Delay Support:** Saniye bazlı gecikme atama / Second-based signal delay.
*   **Görsel Arayüz / Advanced GUI:** `/rednet edit` ile tam kontrol / Full control via edit menu.
*   **Fizik Kalkanı / Physics Protection:** Lamba ve kapılar için fizik güncelleme koruması / Protection against vanilla physics updates for lamps and doors.

---

## 🛠 Kurulum / Installation

1. `target/RedNet-1.0.0.jar` dosyasını `plugins` klasörüne atın. / Place the jar file into the `plugins` folder.
2. Sunucuyu başlatın. / Start the server.
3. `/rednet <type> [delay]` komutuyla başlayın. / Start with the command.

---

## 💻 Komutlar / Commands

* `/rednet <type> [delay]` - Yeni devre kur / Create new circuit.
* `/rednet cancel` - Kurulumu iptal et / Cancel setup.
* `/rednet info` - Bağlantıları göster / Show connections.
* `/rednet edit` - Devreyi düzenle / Edit circuit.