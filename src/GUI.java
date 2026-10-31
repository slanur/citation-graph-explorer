import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;


// kullanıcı arayüzü
public class GUI extends JFrame {
    private Graph graph;
    private GraphVisualizer visualizer;
    private JTextArea statsArea;
    private JTextField articleIdField;
    private JTextField kValueField;
    private JLabel hIndexLabel;
    private JLabel hMedianLabel;
    private JTextArea betweennessArea;
    private JButton loadGraphButton;
    private JButton calculateHIndexButton;
    private JButton calculateMetricsButton;
    private boolean graphLoaded = false;
    private Set<String> displayedNodes;
    
    public GUI() {
        displayedNodes = new HashSet<>();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Makale Graf Analiz Uygulaması - ProLab 3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Sol panel - Kontroller ve istatistikler
        JPanel leftPanel = createLeftPanel();
        
        // Sağ panel - Graf görselleştirme
        JPanel rightPanel = createRightPanel();
        
        // Ana pencereye ekle
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        
        pack();
        setSize(1400, 800);
        setLocationRelativeTo(null);
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(400, 0));
        
        // Başlık
        JLabel titleLabel = new JLabel("Graf Analiz Sistemi");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Graf yükleme bölümü
        JPanel loadPanel = createLoadPanel();
        panel.add(loadPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // İstatistikler bölümü
        JPanel statsPanel = createStatsPanel();
        panel.add(statsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // H-Index hesaplama bölümü
        JPanel hIndexPanel = createHIndexPanel();
        panel.add(hIndexPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Graf analiz metrikleri bölümü
        JPanel metricsPanel = createMetricsPanel();
        panel.add(metricsPanel);
        
        return panel;
    }
    
    private JPanel createLoadPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("1. Graf Yükleme"));
        
        loadGraphButton = new JButton("JSON Dosyasını Yükle");
        loadGraphButton.addActionListener(e -> loadGraph());
        panel.add(loadGraphButton, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Graf İstatistikleri"));
        
        statsArea = new JTextArea(10, 30);
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(statsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createHIndexPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("2. H-Index Hesaplama"));
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Makale ID:"));
        articleIdField = new JTextField(20);
        inputPanel.add(articleIdField);
        panel.add(inputPanel);
        
        calculateHIndexButton = new JButton("H-Index Hesapla ve Göster");
        calculateHIndexButton.setEnabled(false);
        calculateHIndexButton.addActionListener(e -> calculateHIndex());
        calculateHIndexButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(calculateHIndexButton);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel resultsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        resultsPanel.add(new JLabel("H-Index:"));
        hIndexLabel = new JLabel("-");
        hIndexLabel.setFont(new Font("Arial", Font.BOLD, 14));
        resultsPanel.add(hIndexLabel);
        
        resultsPanel.add(new JLabel("H-Median:"));
        hMedianLabel = new JLabel("-");
        hMedianLabel.setFont(new Font("Arial", Font.BOLD, 14));
        resultsPanel.add(hMedianLabel);
        
        panel.add(resultsPanel);
        
        return panel;
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("3. Graf Analiz Metrikleri"));
        
        // Betweenness Centrality
        JPanel bcPanel = new JPanel(new BorderLayout());
        bcPanel.setBorder(BorderFactory.createTitledBorder("Betweenness Centrality"));
        betweennessArea = new JTextArea(8, 30);
        betweennessArea.setEditable(false);
        betweennessArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane bcScroll = new JScrollPane(betweennessArea);
        bcPanel.add(bcScroll, BorderLayout.CENTER);
        panel.add(bcPanel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // K-Core Ayrışımı
        JPanel kcorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        kcorePanel.add(new JLabel("K Değeri:"));
        kValueField = new JTextField(5);
        kcorePanel.add(kValueField);
        
        calculateMetricsButton = new JButton("Metrikleri Hesapla");
        calculateMetricsButton.setEnabled(false);
        calculateMetricsButton.addActionListener(e -> calculateMetrics());
        kcorePanel.add(calculateMetricsButton);
        
        panel.add(kcorePanel);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Graf Görselleştirme"));
        
        visualizer = new GraphVisualizer(null);
        visualizer.setNodeClickListener(nodeId -> onNodeClick(nodeId));
        
        panel.add(visualizer, BorderLayout.CENTER);
        
        // Bilgi etiketi
        JLabel infoLabel = new JLabel("Düğümlere tıklayarak H-Core'larını genişletebilirsiniz");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadGraph() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("JSON Dosyasını Seçin");
        fileChooser.setCurrentDirectory(new java.io.File("."));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            
            try {
                // JSON'ı yükle
                graph = JSONLoader.loadGraph(filePath);
                visualizer = new GraphVisualizer(graph);
                visualizer.setNodeClickListener(nodeId -> onNodeClick(nodeId));
                
                // Sağ paneli güncelle
                Container parent = getContentPane();
                Component[] components = parent.getComponents();
                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        JPanel p = (JPanel) comp;
                        if (p.getBorder() instanceof TitledBorder) {
                            TitledBorder border = (TitledBorder) p.getBorder();
                            if ("Graf Görselleştirme".equals(border.getTitle())) {
                                p.removeAll();
                                p.add(visualizer, BorderLayout.CENTER);
                                JLabel infoLabel = new JLabel("Düğümlere tıklayarak H-Core'larını genişletebilirsiniz");
                                infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
                                p.add(infoLabel, BorderLayout.SOUTH);
                                p.revalidate();
                                p.repaint();
                                break;
                            }
                        }
                    }
                }
                
                graphLoaded = true;
                calculateHIndexButton.setEnabled(true);
                calculateMetricsButton.setEnabled(true);
                
                updateStatistics();
                
                // En çok atıf alan makaleyi ID alanına otomatik yazar
                Article mostCited = graph.getMostCitedArticle();
                if (mostCited != null) {
                    articleIdField.setText(mostCited.getId());
                }
                
                JOptionPane.showMessageDialog(this,
                    "Graf başarıyla yüklendi!\n" +
                    "Toplam " + graph.getTotalArticles() + " makale yüklendi.\n\n" +
                    "En çok atıf alan makale ID alanına yazıldı.\n" +
                    "'H-Index Hesapla' butonuna tıklayarak başlayabilirsiniz.",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Dosya yüklenirken hata oluştu:\n" + ex.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void updateStatistics() {
        if (graph == null) return;
        
        Article mostCited = graph.getMostCitedArticle();
        Article mostReferencing = graph.getMostReferencingArticle();
        
        StringBuilder stats = new StringBuilder();
        stats.append("Toplam Makale Sayısı: ").append(graph.getTotalArticles()).append("\n");
        stats.append("Toplam Referans Sayısı: ").append(graph.getTotalEdges()).append("\n");
        stats.append("Toplam Verilen Referans: ").append(graph.getTotalGivenReferences()).append("\n");
        stats.append("Toplam Alınan Referans: ").append(graph.getTotalReceivedReferences()).append("\n\n");
        
        if (mostCited != null) {
            stats.append("En Çok Referans Alan Makale:\n");
            stats.append("  ID: ").append(mostCited.getId()).append("\n");
            stats.append("  Atıf Sayısı: ").append(mostCited.getCitationCount()).append("\n\n");
        }
        
        if (mostReferencing != null) {
            stats.append("En Çok Referans Veren Makale:\n");
            stats.append("  ID: ").append(mostReferencing.getId()).append("\n");
            stats.append("  Referans Sayısı: ").append(graph.getReferencedArticles(mostReferencing.getId()).size()).append("\n");
        }
        
        statsArea.setText(stats.toString());
    }
    
    private void calculateHIndex() {
        if (!graphLoaded || graph == null) {
            JOptionPane.showMessageDialog(this, "Lütfen önce graf yükleyin!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String articleId = articleIdField.getText().trim();
        if (articleId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir makale ID'si girin!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Article article = graph.getArticle(articleId);
        if (article == null) {
            JOptionPane.showMessageDialog(this, "Makale bulunamadı: " + articleId, "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // H-Index hesapla
        int hIndex = graph.calculateHIndex(articleId);
        double hMedian = graph.calculateHMedian(articleId);
        List<Article> hCore = graph.getHCore(articleId);
        
        // Sonuçları göster
        hIndexLabel.setText(String.valueOf(hIndex));
        hMedianLabel.setText(String.format("%.1f", hMedian));
        
        // Tüm komşuları topla (hem atıf yapanlar hem atıf aldığı)
        Set<Article> allNeighbors = new HashSet<>();
        
        // 1. H-core (ona atıf yapanlar)
        allNeighbors.addAll(hCore);
        
        // 2. Referans verdiği makaleler
        for (String referencedId : article.getReferencedWorks()) {
            Article referenced = graph.getArticle(referencedId);
            if (referenced != null) {
                allNeighbors.add(referenced);
            }
        }
        
        // Graf görselleştir,tüm komşularla
        visualizer.showInitialNetwork(articleId, hCore, new ArrayList<>(allNeighbors));
        displayedNodes.clear();
        displayedNodes.add(articleId);
        for (Article a : allNeighbors) {
            displayedNodes.add(a.getId());
        }
        
        updateStatistics();
    }
    
    private void onNodeClick(String nodeId) {
        if (!graphLoaded || graph == null) return;
        
        Article article = graph.getArticle(nodeId);
        if (article == null) return;
        
        // H-Index hesaplar
        int hIndex = graph.calculateHIndex(nodeId);
        double hMedian = graph.calculateHMedian(nodeId);
        List<Article> hCore = graph.getHCore(nodeId);
        
        // Sonuçları günceller
        hIndexLabel.setText(String.valueOf(hIndex));
        hMedianLabel.setText(String.format("%.1f", hMedian));
        articleIdField.setText(nodeId);
        
        // Tıklanan düğümün komşularını bul (hem atıf yaptığı hem atıf alanlar)
        Set<Article> neighbors = new HashSet<>();
        
        // 1. Bu makaleye atıf yapan makaleler (H-core)
        neighbors.addAll(hCore);
        
        // 2. Bu makalenin atıf yaptığı makaleler
        for (String referencedId : article.getReferencedWorks()) {
            Article referenced = graph.getArticle(referencedId);
            if (referenced != null) {
                neighbors.add(referenced);
            }
        }
        
        // Grafa yeni düğümleri ekler (sadece henüz gösterilmeyen düğümler)
        List<Article> newNodes = new ArrayList<>();
        for (Article neighbor : neighbors) {
            if (!displayedNodes.contains(neighbor.getId())) {
                newNodes.add(neighbor);
                displayedNodes.add(neighbor.getId());
            }
        }
        
        // Görselleştiriciyi günceller
        visualizer.expandNetwork(nodeId, newNodes);
        
        updateStatistics();
    }
    
    private void calculateMetrics() {
        if (!graphLoaded || graph == null) {
            JOptionPane.showMessageDialog(this, "Lütfen önce graf yükleyin!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (displayedNodes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen önce bir makale için H-Index hesaplayın!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Betweenness Centrality hesaplar
        betweennessArea.setText("Hesaplanıyor...\n");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            Map<String, Double> betweenness;
            Set<String> kCoreNodes = null;
            
            @Override
            protected Void doInBackground() throws Exception {
                // Betweenness hesaplar,sadece goruntulenler için
                betweenness = GraphAnalyzer.calculateBetweennessCentralityForSubgraph(graph, displayedNodes);
                
                // K-Core hesapla
                String kText = kValueField.getText().trim();
                if (!kText.isEmpty()) {
                    try {
                        int k = Integer.parseInt(kText);
                        kCoreNodes = GraphAnalyzer.kCoreDecomposition(graph, k);
                    } catch (NumberFormatException e) {
                        // Hata durumunda null kalacak
                    }
                }
                
                return null;
            }
            
            @Override
            protected void done() {
                // Betweenness sonuçlarını gösterir
                StringBuilder sb = new StringBuilder();
                sb.append("Betweenness Centrality Değerleri:\n");
                sb.append("=====================================\n");
                
                // Sadece görüntülenen düğümler için gösterir
                List<Map.Entry<String, Double>> entries = new ArrayList<>();
                for (String nodeId : displayedNodes) {
                    if (betweenness.containsKey(nodeId)) {
                        entries.add(new AbstractMap.SimpleEntry<>(nodeId, betweenness.get(nodeId)));
                    }
                }
                
                entries.sort((a, b) -> {
                    int cmp = Double.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    return a.getKey().compareTo(b.getKey()); // ID sıralaması
                });
                
                for (Map.Entry<String, Double> entry : entries) {
                    sb.append(String.format("%-20s: %.4f\n", 
                        entry.getKey().substring(Math.max(0, entry.getKey().length() - 12)),
                        entry.getValue()));
                }
                
                betweennessArea.setText(sb.toString());
                
                // K-Core sonuçlarını gösterir
                if (kCoreNodes != null) {
                    visualizer.highlightKCore(kCoreNodes);
                    
                    int kCoreSize = kCoreNodes.size();
                    Set<String> kCoreEdges = GraphAnalyzer.getKCoreEdges(graph, kCoreNodes);
                    
                    JOptionPane.showMessageDialog(GUI.this,
                        String.format("K-Core Sonuçları:\n\nDüğüm Sayısı: %d\nKenar Sayısı: %d",
                            kCoreSize, kCoreEdges.size()),
                        "K-Core Decomposition",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}