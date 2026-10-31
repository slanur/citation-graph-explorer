public class Main {
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  Makale Graf Analiz Uygulaması");
        System.out.println("  ProLab 3 - Bilgisayar Mühendisliği");
        System.out.println("===========================================");
        System.out.println();
        
        // GUI'yi başlatır
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // Sistem görünümünü kullanır
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                System.err.println("Look and Feel ayarlanamadı: " + e.getMessage());
            }
            
            // Ana pencereyi oluşturur ve gösterir
            GUI gui = new GUI();
            gui.setVisible(true);
            
            System.out.println("Uygulama başlatıldı.");
            System.out.println("Lütfen 'JSON Dosyasını Yükle' butonuna tıklayarak başlayın.");
        });
    }
}