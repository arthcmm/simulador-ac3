import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SimplePipelineVisualizer extends JFrame {
    private JLabel[] decodedBoxes = new JLabel[4];
    private JLabel[] ufBoxes = new JLabel[4];
    SuperEscalar superEscalar;

    public SimplePipelineVisualizer(SuperEscalar superEscalar) {
        this.superEscalar = superEscalar;
        setTitle("Pipeline Visualizer (Superescalar)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(1, 2, 10, 10));

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

        add(decodedPanel);
        add(ufPanel);

        setVisible(false); // Será visível quando a simulação for iniciada
    }

    /**
     * Atualiza as instruções decodificadas no visualizador.
     * @param decoded Lista de instruções decodificadas neste ciclo
     */
    public void updateDecoded(ArrayList<Instruction> decoded) {
        for (int i = 0; i < decodedBoxes.length; i++) {
            if (i < decoded.size()) {
                Instruction instr = decoded.get(i);
                decodedBoxes[i].setText(instr.codigo + " (Ctx " + instr.contexto + ")");
                decodedBoxes[i].setBackground(Color.CYAN);
            } else {
                decodedBoxes[i].setText("Vazio");
                decodedBoxes[i].setBackground(Color.LIGHT_GRAY);
            }
        }
    }

    /**
     * Atualiza o estado das unidades funcionais no visualizador.
     * @param ufStatus Mapeamento do nome da unidade funcional para sua instrução atual
     */
    public void updateUF(HashMap<String, Instruction> ufStatus) {
        String[] ufNames = { "ALU1", "ALU2", "MEM", "JMP" };
        for (int i = 0; i < ufBoxes.length; i++) {
            Instruction instr = ufStatus.getOrDefault(ufNames[i], new Instruction());
            if (instr.codigo.equals("VAZIO")) {
                ufBoxes[i].setText(ufNames[i] + ": VAZIO");
                ufBoxes[i].setBackground(Color.LIGHT_GRAY);
            } else {
                ufBoxes[i].setText(ufNames[i] + ": " + instr.codigo + " (Ctx " + instr.contexto + ")");
                // Colorir de acordo com o contexto
                if (instr.contexto == 0) {
                    ufBoxes[i].setBackground(Color.ORANGE);
                } else {
                    ufBoxes[i].setBackground(Color.GREEN);
                }
            }
        }
    }
}