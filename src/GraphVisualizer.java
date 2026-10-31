import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;


//Graf görselleştirme paneli
public class GraphVisualizer extends JPanel {
    private Graph graph;
    private Set<String> nodesToShow; // Gösterilecek düğümler
    private Map<String, Point2D.Double> nodePositions; // Düğüm konumları
    private Map<String, Color> nodeColors; // Düğüm renkleri
    private Set<String> highlightedNodes; // Vurgulanan düğümler
    private Set<String> kCoreNodes; // K-core düğümleri
    private Set<String> newEdges; // Yeni eklenen kenarlar (kırmızı gösterilecek)
    private String hoveredNode; // Mouse üzerindeki düğüm
    private String selectedNode; // Seçili düğüm
    private NodeClickListener clickListener;
    
    private static final int NODE_RADIUS = 30;
    private static final Color DEFAULT_COLOR = new Color(255, 255, 255); // Beyaz - normal düğümler
    private static final Color HCORE_COLOR = new Color(255, 235, 180); // Açık sarı - h-core düğümleri
    private static final Color SELECTED_COLOR = new Color(255, 180, 180); // Açık kırmızı/pembe - seçili düğüm
    private static final Color NEW_NODE_COLOR = new Color(200, 255, 200); // Açık yeşil - yeni düğümler
    private static final Color KCORE_COLOR = new Color(220, 200, 255); // Açık mor/lila - k-core düğümleri
    private static final Color EDGE_COLOR = Color.BLACK; // Siyah - normal kenarlar
    private static final Color KCORE_EDGE_COLOR = new Color(100, 150, 255); // Mavi - k-core kenarları
    private static final Color NEW_EDGE_COLOR = new Color(255, 100, 100); // Kırmızı - yeni kenarlar
    
    public interface NodeClickListener {
        void onNodeClick(String nodeId);
    }
    
    public GraphVisualizer(Graph graph) {
        this.graph = graph;
        this.nodesToShow = new HashSet<>();
        this.nodePositions = new HashMap<>();
        this.nodeColors = new HashMap<>();
        this.highlightedNodes = new HashSet<>();
        this.kCoreNodes = new HashSet<>();
        this.newEdges = new HashSet<>();
        this.hoveredNode = null;
        this.selectedNode = null;
        
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        
        // Mouse listener'ları ekle
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String clickedNode = getNodeAt(e.getPoint());
                if (clickedNode != null && clickListener != null) {
                    selectedNode = clickedNode;
                    clickListener.onNodeClick(clickedNode);
                    repaint();
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                String newHoveredNode = getNodeAt(e.getPoint());
                if (!Objects.equals(hoveredNode, newHoveredNode)) {
                    hoveredNode = newHoveredNode;
                    repaint();
                }
            }
        });
    }
    
    public void setNodeClickListener(NodeClickListener listener) {
        this.clickListener = listener;
    }
    
     //H-Core düğümlerini gösterir
    public void showHCore(String articleId, List<Article> hCore) {
        nodesToShow.clear();
        nodeColors.clear();
        highlightedNodes.clear();
        kCoreNodes.clear();
        newEdges.clear();
        
        nodesToShow.add(articleId);
        nodeColors.put(articleId, SELECTED_COLOR);
        highlightedNodes.add(articleId);
        selectedNode = articleId;
        
        for (Article article : hCore) {
            nodesToShow.add(article.getId());
            nodeColors.put(article.getId(), HCORE_COLOR);
        }
        
        calculateLayout();
        repaint();
    }
    
     // İlk ağı göster - merkez düğüm, H-core ve referans verdiği düğümler
    public void showInitialNetwork(String centralArticleId, List<Article> hCore, List<Article> allNeighbors) {
        nodesToShow.clear();
        nodeColors.clear();
        highlightedNodes.clear();
        kCoreNodes.clear();
        newEdges.clear();
        
        // Merkez düğüm (seçili makale)
        nodesToShow.add(centralArticleId);
        nodeColors.put(centralArticleId, SELECTED_COLOR);
        highlightedNodes.add(centralArticleId);
        selectedNode = centralArticleId;
        
        // H-core düğümleri (ona atıf yapanlar) - sarı/turuncu
        Set<String> hCoreIds = new HashSet<>();
        for (Article article : hCore) {
            hCoreIds.add(article.getId());
        }
        
        // Tüm komşuları ekler
        for (Article article : allNeighbors) {
            String nodeId = article.getId();
            nodesToShow.add(nodeId);
            
            // H-core'daysa sarı/turuncu, değilse varsayılan beyaz
            if (hCoreIds.contains(nodeId)) {
                nodeColors.put(nodeId, HCORE_COLOR);
            } else {
                nodeColors.put(nodeId, DEFAULT_COLOR);
            }
        }
        
        calculateLayout();
        repaint();
    }
    
     // Mevcut grafa yeni H-Core düğümleri ekler
    public void addHCoreToGraph(String clickedNodeId, List<Article> newHCore) {
        // Tıklanan düğümü vurgula
        selectedNode = clickedNodeId;
        
        // Eski düğümlerin renklerini varsayılana çevir (selected hariç)
        for (String nodeId : new HashSet<>(nodeColors.keySet())) {
            if (!nodeId.equals(clickedNodeId) && !kCoreNodes.contains(nodeId)) {
                nodeColors.put(nodeId, DEFAULT_COLOR);
            }
        }
        
        nodeColors.put(clickedNodeId, SELECTED_COLOR);
        
        // Yeni düğümleri ekle
        for (Article article : newHCore) {
            String nodeId = article.getId();
            if (!nodesToShow.contains(nodeId)) {
                nodesToShow.add(nodeId);
                nodeColors.put(nodeId, NEW_NODE_COLOR);
            }
        }
        
        calculateLayout();
        repaint();
    }

     // Ağı genişlet - tıklanan düğümün tüm komşularını ekle
    public void expandNetwork(String clickedNodeId, List<Article> newNeighbors) {
        // Tıklanan düğümü vurgula
        selectedNode = clickedNodeId;
        
        // Önceki "yeni kenarlar" artık normal hale gelir
        newEdges.clear();
        
        // Eski düğümlerin renklerini varsayılana çevirir (selected hariç)
        for (String nodeId : new HashSet<>(nodeColors.keySet())) {
            if (!nodeId.equals(clickedNodeId) && !kCoreNodes.contains(nodeId)) {
                nodeColors.put(nodeId, DEFAULT_COLOR);
            }
        }
        
        nodeColors.put(clickedNodeId, SELECTED_COLOR);
        
        // Yeni komşu düğümleri ekler
        Set<String> newNodeIds = new HashSet<>();
        for (Article article : newNeighbors) {
            String nodeId = article.getId();
            if (!nodesToShow.contains(nodeId)) {
                nodesToShow.add(nodeId);
                nodeColors.put(nodeId, NEW_NODE_COLOR);
                newNodeIds.add(nodeId);
            }
        }
        
        // Yeni düğümler ve mevcut düğümler arasındaki kenarları işaretler
        for (String newNodeId : newNodeIds) {
            // Yeni düğümün mevcut düğümlere olan kenarları
            for (String existingNode : nodesToShow) {
                if (!newNodeIds.contains(existingNode)) {
                    //aralarında kenar var mı?
                    if (graph.getReferencedArticles(newNodeId).contains(existingNode)) {
                        newEdges.add(newNodeId + "->" + existingNode);
                    }
                    if (graph.getReferencedArticles(existingNode).contains(newNodeId)) {
                        newEdges.add(existingNode + "->" + newNodeId);
                    }
                }
            }
        }
        
        calculateLayout();
        repaint();
    }
    
    
    //K-Core düğümlerini vurgula
    public void highlightKCore(Set<String> kCoreNodeIds) {
        this.kCoreNodes = new HashSet<>(kCoreNodeIds);
        
        for (String nodeId : kCoreNodeIds) {
            if (nodesToShow.contains(nodeId)) {
                nodeColors.put(nodeId, KCORE_COLOR);
            }
        }
        
        repaint();
    }
    
    
     //Düğüm düzenini hesaplar
    private void calculateLayout() {
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) {
            width = 800;
            height = 600;
        }
        
        int margin = 80;
        List<String> nodes = new ArrayList<>(nodesToShow);
        Collections.sort(nodes);
        if (nodes.size() == 1) {
            // Tek düğüm varsa merkeze koy
            nodePositions.put(nodes.get(0), new Point2D.Double(width / 2.0, height / 2.0));
        } else if (nodes.size() <= 6) {
            // Az sayıda düğüm varsa dairesel yerleşim
            double centerX = width / 2.0;
            double centerY = height / 2.0;
            double radius = Math.min(width, height) / 3.0;
            
            for (int i = 0; i < nodes.size(); i++) {
                double angle = 2 * Math.PI * i / nodes.size();
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);
                nodePositions.put(nodes.get(i), new Point2D.Double(x, y));
            }
        } else {
            // Çok düğüm varsa grid layout
            int cols = (int) Math.ceil(Math.sqrt(nodes.size()));
            int rows = (int) Math.ceil((double) nodes.size() / cols);
            
            double cellWidth = (width - 2 * margin) / (double) cols;
            double cellHeight = (height - 2 * margin) / (double) rows;
            
            for (int i = 0; i < nodes.size(); i++) {
                int row = i / cols;
                int col = i % cols;
                double x = margin + cellWidth * (col + 0.5);
                double y = margin + cellHeight * (row + 0.5);
                nodePositions.put(nodes.get(i), new Point2D.Double(x, y));
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (nodePositions.isEmpty()) {
            calculateLayout();
        }
        
        // Kenarları çizer
        drawEdges(g2d);
        
        // Düğümleri çizer
        drawNodes(g2d);
        
        // Hover bilgisini gösterir
        if (hoveredNode != null) {
            drawNodeInfo(g2d, hoveredNode);
        }
    }
    
    private void drawEdges(Graphics2D g2d) {
        Set<String> drawnEdges = new HashSet<>();
        
        for (String fromId : nodesToShow) {
            Point2D.Double fromPos = nodePositions.get(fromId);
            if (fromPos == null) continue;
            
            // Referans verdiği makaleler
            List<String> references = graph.getReferencedArticles(fromId);
            for (String toId : references) {
                if (nodesToShow.contains(toId)) {
                    Point2D.Double toPos = nodePositions.get(toId);
                    if (toPos == null) continue;
                    
                    String edgeKey = fromId + "->" + toId;
                    if (!drawnEdges.contains(edgeKey)) {
                        // Kenar renk ve kalınlık belirleme (öncelik sırasına göre)
                        if (newEdges.contains(edgeKey)) {
                            // Yeni eklenen kenarlar kırmızı
                            g2d.setColor(NEW_EDGE_COLOR);
                            g2d.setStroke(new BasicStroke(3));
                        } else if (kCoreNodes.contains(fromId) && kCoreNodes.contains(toId)) {
                            // K-core kenarları mavi
                            g2d.setColor(KCORE_EDGE_COLOR);
                            g2d.setStroke(new BasicStroke(3));
                        } else {
                            // Normal kenarlar siyah
                            g2d.setColor(EDGE_COLOR);
                            g2d.setStroke(new BasicStroke(2));
                        }
                        
                        drawArrow(g2d, fromPos, toPos);
                        drawnEdges.add(edgeKey);
                    }
                }
            }
        }
    }
    
    private void drawArrow(Graphics2D g2d, Point2D.Double from, Point2D.Double to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double angle = Math.atan2(dy, dx);
        double length = Math.sqrt(dx * dx + dy * dy);
        
        // Düğüm yarıçapı kadar kısaltır
        double shortenedLength = length - NODE_RADIUS;
        double endX = from.x + shortenedLength * Math.cos(angle);
        double endY = from.y + shortenedLength * Math.sin(angle);
        
        double startX = from.x + NODE_RADIUS * Math.cos(angle);
        double startY = from.y + NODE_RADIUS * Math.sin(angle);
        
        // Çizgiyi çizer
        g2d.drawLine((int) startX, (int) startY, (int) endX, (int) endY);
        
        // Ok başını çizer
        double arrowSize = 10;
        double angle1 = angle + Math.PI * 0.85;
        double angle2 = angle - Math.PI * 0.85;
        
        int[] xPoints = {
            (int) endX,
            (int) (endX + arrowSize * Math.cos(angle1)),
            (int) (endX + arrowSize * Math.cos(angle2))
        };
        int[] yPoints = {
            (int) endY,
            (int) (endY + arrowSize * Math.sin(angle1)),
            (int) (endY + arrowSize * Math.sin(angle2))
        };
        
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
    
    private void drawNodes(Graphics2D g2d) {
        // Düğümlere harf etiketleri atamak için
        Map<String, String> nodeLabels = new HashMap<>();
        List<String> sortedNodes = new ArrayList<>(nodePositions.keySet());
        sortedNodes.sort(Comparator.naturalOrder());
        
        for (int i = 0; i < sortedNodes.size(); i++) {
            String label = String.valueOf((char) ('A' + i));
            nodeLabels.put(sortedNodes.get(i), label);
        }
        
        for (Map.Entry<String, Point2D.Double> entry : nodePositions.entrySet()) {
            String nodeId = entry.getKey();
            Point2D.Double pos = entry.getValue();
            
            // Düğüm rengini alır
            Color color = nodeColors.getOrDefault(nodeId, DEFAULT_COLOR);
            
            // Hover durumunda daha açık renk
            if (nodeId.equals(hoveredNode)) {
                color = color.brighter();
            }
            
            // Düğümü çizer
            g2d.setColor(color);
            g2d.fillOval((int) (pos.x - NODE_RADIUS), (int) (pos.y - NODE_RADIUS),
                        NODE_RADIUS * 2, NODE_RADIUS * 2);
            
            // Kenar çizer
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval((int) (pos.x - NODE_RADIUS), (int) (pos.y - NODE_RADIUS),
                        NODE_RADIUS * 2, NODE_RADIUS * 2);
            
            Article article = graph.getArticle(nodeId);
            if (article != null) {
                // Atıf sayısını üstte gösterir (büyük font)
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String citationText = String.valueOf(article.getCitationCount());
                FontMetrics fm1 = g2d.getFontMetrics();
                int textWidth1 = fm1.stringWidth(citationText);
                g2d.drawString(citationText, (int) (pos.x - textWidth1 / 2.0), (int) (pos.y - 8));
                
                // ID'nin son 10 karakterini ortada gösterir (orta font)
                g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                String shortId = nodeId.length() > 10 ? nodeId.substring(nodeId.length() - 10) : nodeId;
                FontMetrics fm2 = g2d.getFontMetrics();
                int textWidth2 = fm2.stringWidth(shortId);
                g2d.drawString(shortId, (int) (pos.x - textWidth2 / 2.0), (int) (pos.y + 5));
                
                // Harf etiketini altta gösterir (büyük font)
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String label = nodeLabels.get(nodeId);
                FontMetrics fm3 = g2d.getFontMetrics();
                int textWidth3 = fm3.stringWidth(label);
                g2d.drawString(label, (int) (pos.x - textWidth3 / 2.0), (int) (pos.y + 18));
            }
        }
    }
    
    private void drawNodeInfo(Graphics2D g2d, String nodeId) {
        Article article = graph.getArticle(nodeId);
        if (article == null) return;
        
        Point2D.Double pos = nodePositions.get(nodeId);
        if (pos == null) return;
        
        // Bilgi kartı oluşturur
        String[] lines = {
            "ID: " + article.getId(),
            "Title: " + truncate(article.getTitle(), 40),
            "Year: " + article.getYear(),
            "Authors: " + truncate(String.join(", ", article.getAuthors()), 40),
            "Citations: " + article.getCitationCount()
        };
        
        // Kart boyutunu hesaplar
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, fm.stringWidth(line));
        }
        
        int cardWidth = maxWidth + 20;
        int cardHeight = lines.length * (fm.getHeight() + 2) + 10;
        
        int cardX = (int) pos.x + NODE_RADIUS + 10;
        int cardY = (int) pos.y - cardHeight / 2;
        
        // Ekran dışına taşmayı kontrol eder
        if (cardX + cardWidth > getWidth()) {
            cardX = (int) pos.x - NODE_RADIUS - cardWidth - 10;
        }
        if (cardY < 0) cardY = 10;
        if (cardY + cardHeight > getHeight()) {
            cardY = getHeight() - cardHeight - 10;
        }
        
        // Kartı çizer
        g2d.setColor(new Color(255, 255, 230, 240));
        g2d.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 10, 10);
        
        // Metni yazar
        g2d.setColor(Color.BLACK);
        int textY = cardY + fm.getHeight();
        for (String line : lines) {
            g2d.drawString(line, cardX + 10, textY);
            textY += fm.getHeight() + 2;
        }
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    private String getNodeAt(Point point) {
        for (Map.Entry<String, Point2D.Double> entry : nodePositions.entrySet()) {
            Point2D.Double pos = entry.getValue();
            double distance = point.distance(pos);
            if (distance <= NODE_RADIUS) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public void clear() {
        nodesToShow.clear();
        nodePositions.clear();
        nodeColors.clear();
        highlightedNodes.clear();
        kCoreNodes.clear();
        hoveredNode = null;
        selectedNode = null;
        repaint();
    }
}
