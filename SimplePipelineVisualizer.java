import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class SimplePipelineVisualizer extends JFrame {
    private JLabel[] decodedBoxes = new JLabel[4];
    private JLabel[] ufBoxes = new JLabel[4];
    SuperEscalar superEscalar;

    public SimplePipelineVisualizer(SuperEscalar superEscalar) {
        this.superEscalar = new SuperEscalar();
        setTitle("Pipeline Visualizer");
        setSize(800, 600); // Tamanho ajustado para dois painéis
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2, 10, 10)); // Layout com 1 linha e 2 colunas
    
        // Painel para Decodificado
        JPanel decodedPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        decodedPanel.setBorder(BorderFactory.createTitledBorder("Decodificado"));
        for (int i = 0; i < 4; i++) {
            decodedBoxes[i] = new JLabel("Vazio", SwingConstants.CENTER);
            decodedBoxes[i].setOpaque(true);
            decodedBoxes[i].setBackground(Color.LIGHT_GRAY);
            decodedBoxes[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            decodedPanel.add(decodedBoxes[i]);
        }
    
        // Painel para Unidades Funcionais
        JPanel ufPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        ufPanel.setBorder(BorderFactory.createTitledBorder("Unidades Funcionais"));
        for (int i = 0; i < 4; i++) {
            ufBoxes[i] = new JLabel("Vazio", SwingConstants.CENTER);
            ufBoxes[i].setOpaque(true);
            ufBoxes[i].setBackground(Color.LIGHT_GRAY);
            ufBoxes[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            ufPanel.add(ufBoxes[i]);
        }
    
        // Adicionar os painéis ao JFrame
        add(decodedPanel); // Adiciona o painel de Decodificado na primeira coluna
        add(ufPanel);      // Adiciona o painel de Unidades Funcionais na segunda coluna
    
        setVisible(true);
    }

    public void updateDecoded(ArrayList<Instruction> decoded) {
        for (int i = 0; i < decodedBoxes.length; i++) {
            if (i < decoded.size()) {
                decodedBoxes[i].setText(decoded.get(i).codigo + " Contexto: " + decoded.get(i).contexto);
                decodedBoxes[i].setBackground(Color.CYAN);
            } else {
                decodedBoxes[i].setText("Vazio");
                decodedBoxes[i].setBackground(Color.LIGHT_GRAY);
            }
        }
    }

    public void updateUF(HashMap<String, Instruction> ufStatus) {
        String[] ufNames = { "ALU1", "ALU2", "MEM", "JMP" };
        for (int i = 0; i < ufBoxes.length; i++) {
            String status = ufStatus.getOrDefault(ufNames[i], new Instruction()).codigo;
            int contexto = ufStatus.getOrDefault(ufNames[i], new Instruction()).contexto;
            ufBoxes[i].setText(ufNames[i] + ": " + status + " Contexto: " + contexto);
            ufBoxes[i].setBackground(status.equals("VAZIO")  || status.equals("NOP") ? Color.LIGHT_GRAY : contexto == 1 ? Color.ORANGE : Color.GREEN);
        }
    }

}
