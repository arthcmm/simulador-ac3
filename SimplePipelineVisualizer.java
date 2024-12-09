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

        JPanel decodedPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        decodedPanel.setBorder(BorderFactory.createTitledBorder("Decodificado"));
        for (int i = 0; i < 4; i++) {
            decodedBoxes[i] = new JLabel("Vazio", SwingConstants.CENTER);
            decodedBoxes[i].setOpaque(true);
            decodedBoxes[i].setBackground(Color.LIGHT_GRAY);
            decodedBoxes[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            decodedPanel.add(decodedBoxes[i]);
        }

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

    public void updateDecoded(ArrayList<Instruction> decoded) {
        for (int i = 0; i < decodedBoxes.length; i++) {
            if (i < decoded.size()) {
                decodedBoxes[i].setText(decoded.get(i).codigo + " Ctx: " + decoded.get(i).contexto);
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
            Instruction instr = ufStatus.getOrDefault(ufNames[i], new Instruction());
            ufBoxes[i].setText(ufNames[i] + ": " + instr.codigo + " Ctx: " + instr.contexto);
            ufBoxes[i].setBackground(instr.codigo.equals("VAZIO") || instr.codigo.equals("NOP") ? Color.LIGHT_GRAY :
                                     instr.contexto == 1 ? Color.ORANGE : Color.GREEN);
        }
    }
}