import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class GraphPlot extends JPanel {
    private static int STEP = 20;
    private double xMin;
    private double xMax;

    private Function<Double, Double> function;

    public GraphPlot(Function<Double, Double> function, double xMin, double xMax) {
        this.function = function;
        this.xMax = xMax;
        this.xMin = xMin;
    }

    public void setFunction(Function<Double, Double> function) {
        this.function = function;
        repaint();
    }

    public int getStep() {
        return STEP;
    }

    public void setStep(int step) {
        this.STEP = step;
        repaint();
    }

    public double getXMin(){
        return xMin;
    }

    public double getXMax(){
        return xMax;
    }

    public void setRange(double xMin, double xMax){
        this.xMin = xMin; this.xMax = xMax;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Сетка
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));

        for(int x = centerX + STEP; x < width; x += STEP)
            g2d.drawLine(x, 0, x, height);
        for(int x = centerX - STEP; x > 0; x -= STEP)
            g2d.drawLine(x, 0, x, height);

        for(int y = centerY + STEP; y < height; y += STEP)
            g2d.drawLine(0, y, width, y);
        for(int y = centerY - STEP; y > 0; y -= STEP)
            g2d.drawLine(0, y, width, y);

        // Координатные оси
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        g2d.drawLine(0, centerY, width, centerY);
        g2d.drawLine(centerX, 0, centerX, height);

        // Стрелочки
        int arrow = 10;
        g2d.drawLine(width, centerY, width - arrow, centerY - arrow / 2);
        g2d.drawLine(width, centerY, width - arrow, centerY + arrow / 2);

        g2d.drawLine(centerX, 0, centerX - arrow / 2, arrow);
        g2d.drawLine(centerX, 0, centerX + arrow / 2, arrow);

        // Названия осей
        g2d.drawString("X", width - 20, centerY + 20);
        g2d.drawString("Y", centerX - 20, 20);
        g2d.drawString("0", centerX - 12, centerY + 15);

        // График ф-и
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));

        int screenXMin = (int) Math.round(centerX + xMin * STEP);
        int screenXMax = (int) Math.round(centerX + xMax * STEP);

        int startX = Math.max(0, screenXMin);
        int endX = Math.min(width - 1, screenXMax);

        for (int screenX = startX; screenX < endX; screenX++) {
            // Вычисляем математический x из экранного screenX
            double x = (double) (screenX - centerX) / STEP;
            double y = function.apply(x);

            double nextX = (double) (screenX + 1 - centerX) / STEP;
            double nextY = function.apply(nextX);

            if (!Double.isNaN(y) && !Double.isInfinite(y) && !Double.isNaN(nextY) && !Double.isInfinite(nextY)) {
                int screenY = (int) Math.round(centerY - y * STEP);
                int nextScreenY = (int) Math.round(centerY - nextY * STEP);
                g2d.drawLine(screenX, screenY, screenX + 1, nextScreenY);
            }
        }
    }



    public static void main(String[] args) {
        JFrame frame = new JFrame("График функции");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphPlot panel = new GraphPlot(x -> x + 5, -10, 10);
        panel.setPreferredSize(new Dimension(600, 600));

        String[] functionNames = {
                "x + 5",
                "x²",
                "x³",
                "|x|",
                "sin(x)",
                "cos(x)"
        };
        JComboBox<String> functionBox = new JComboBox<>(functionNames);

        functionBox.addActionListener(e -> {
            String selected = (String) functionBox.getSelectedItem();
            switch (selected) {
                case "x + 5" -> panel.setFunction(x -> x + 5);
                case "x²" -> panel.setFunction(x -> x * x);
                case "x³" -> panel.setFunction(x -> x * x * x);
                case "|x|" -> panel.setFunction(x -> Math.abs(x));
                case "sin(x)" -> panel.setFunction(Math::sin);
                case "cos(x)" -> panel.setFunction(Math::cos);
            }
        });

        JButton zoomIn = new JButton("+");
        JButton zoomOut = new JButton("-");

        zoomIn.addActionListener(e -> {
            panel.setStep(panel.getStep() + 10);
        });
        zoomOut.addActionListener(e -> {
            if (panel.getStep() > 10)
                panel.setStep(panel.getStep() - 10);
        });

        JTextField xMinField = new JTextField(String.valueOf(panel.getXMin()), 4);
        JTextField xMaxField = new JTextField(String.valueOf(panel.getXMax()), 4);
        Action updateRangeAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    double min = Double.parseDouble(xMinField.getText());
                    double max = Double.parseDouble(xMaxField.getText());
                    panel.setRange(min, max);
                } catch (NumberFormatException ex) {
                    xMinField.setText(String.valueOf(panel.getXMin()));
                    xMaxField.setText(String.valueOf(panel.getXMax()));
                }
            }
        };
        xMinField.addActionListener(updateRangeAction);
        xMaxField.addActionListener(updateRangeAction);

        frame.setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Функция:"));
        controlPanel.add(functionBox);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(new JLabel("Масштаб:"));
        controlPanel.add(zoomIn);
        controlPanel.add(zoomOut);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(new JLabel("xMin:"));
        controlPanel.add(xMinField);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(new JLabel("xMax:"));
        controlPanel.add(xMaxField);

        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}