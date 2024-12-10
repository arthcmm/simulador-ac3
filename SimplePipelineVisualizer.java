import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SimplePipelineVisualizer extends JFrame {
    public int limBoxes = 5;
    private JLabel[] ciclosBoxes = new JLabel[limBoxes];
    private JLabel[][] ufBoxes = new JLabel[4][limBoxes];
    SuperEscalar superEscalar;

    public SimplePipelineVisualizer(SuperEscalar superEscalar) {
        this.superEscalar = superEscalar;
        setTitle("Pipeline Visualizer (Superescalar)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(1, 2, 10, 10));
        JPanel ciclos = new JPanel(new GridLayout(5, 1, 10, 10));
        ciclos.setBorder(BorderFactory.createTitledBorder("Ciclo"));
        for (int i = 0; i < 5; i++) {
            ciclosBoxes[i] = new JLabel("Vazio", SwingConstants.CENTER);
            ciclosBoxes[i].setOpaque(true);
            ciclosBoxes[i].setBackground(Color.LIGHT_GRAY);
            ciclosBoxes[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            ciclos.add(ciclosBoxes[i]);
        }
        add(ciclos);
        JPanel ALU1 = new JPanel(new GridLayout(5, 1, 10, 10));
        ALU1.setBorder(BorderFactory.createTitledBorder("ALU1"));
        for (int i = 0; i < 5; i++) {
            ufBoxes[0][i] = new JLabel("Vazio", SwingConstants.CENTER);
            ufBoxes[0][i].setOpaque(true);
            ufBoxes[0][i].setBackground(Color.LIGHT_GRAY);
            ufBoxes[0][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            ALU1.add(ufBoxes[0][i]);
        }
        JPanel ALU2 = new JPanel(new GridLayout(5, 1, 10, 10));
        ALU2.setBorder(BorderFactory.createTitledBorder("ALU2"));
        for (int i = 0; i < 5; i++) {
            ufBoxes[1][i] = new JLabel("Vazio", SwingConstants.CENTER);
            ufBoxes[1][i].setOpaque(true);
            ufBoxes[1][i].setBackground(Color.LIGHT_GRAY);
            ufBoxes[1][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            ALU2.add(ufBoxes[1][i]);
        }
        JPanel MEM = new JPanel(new GridLayout(5, 1, 10, 10));
        MEM.setBorder(BorderFactory.createTitledBorder("MEM"));
        for (int i = 0; i < 5; i++) {
            ufBoxes[2][i] = new JLabel("Vazio", SwingConstants.CENTER);
            ufBoxes[2][i].setOpaque(true);
            ufBoxes[2][i].setBackground(Color.LIGHT_GRAY);
            ufBoxes[2][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            MEM.add(ufBoxes[2][i]);
        }
        JPanel JMP = new JPanel(new GridLayout(5, 1, 10, 10));
        JMP.setBorder(BorderFactory.createTitledBorder("JMP"));
        for (int i = 0; i < 5; i++) {
            ufBoxes[3][i] = new JLabel("Vazio", SwingConstants.CENTER);
            ufBoxes[3][i].setOpaque(true);
            ufBoxes[3][i].setBackground(Color.LIGHT_GRAY);
            ufBoxes[3][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            JMP.add(ufBoxes[3][i]);
        }

        add(ALU1);
        add(ALU2);
        add(MEM);
        add(JMP);

        setVisible(false); // Será visível quando a simulação for iniciada
    }

    public void updateUfCerto(ArrayList<Instruction> instructions, int ciclo) {
        puxaPraBaixo();
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instr = instructions.get(i);
            ciclosBoxes[0].setText("Ciclo : " + ciclo);
            ;
            if (instr.codigo.equals("BUB") || instr.codigo.equals("VAZIO")) {
                ufBoxes[i][0].setText(" BUB");
                ufBoxes[i][0].setBackground(Color.RED);
            } else {
                ufBoxes[i][0].setText(instr.codigo + " " + instr.dest + ", " + instr.op1 + ", " + instr.op2
                        + " (Thread " + instr.contexto + ")");
                // Colorir de acordo com o contexto
                if (instr.contexto == 0) {
                    ufBoxes[i][0].setBackground(Color.ORANGE);
                } else {
                    ufBoxes[i][0].setBackground(Color.GREEN);
                }
            }
        }
    }

    private void puxaPraBaixo() {
        for (int i = limBoxes - 1; i > 0; i--) {
            ciclosBoxes[i].setText(ciclosBoxes[i - 1].getText());
            
            for (int j = 0; j < 4; j++) {
                ufBoxes[j][i].setText(ufBoxes[j][i - 1].getText());
                ufBoxes[j][i].setBackground(ufBoxes[j][i - 1].getBackground());
            }
        }
    }
    
}