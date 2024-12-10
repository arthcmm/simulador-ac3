import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

public class SimplePipelineVisualizer extends JFrame {
    private JLabel[] decodedBoxes = new JLabel[4];
    private JLabel[][] ufBoxes = new JLabel[4][5];
    SuperEscalar superEscalar;

    public SimplePipelineVisualizer(SuperEscalar superEscalar) {
        this.superEscalar = superEscalar;
        setTitle("Pipeline Visualizer (Superescalar)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(1, 2, 10, 10));

        /*
         * / Painel para Decodificado
         * JPanel decodedPanel = new JPanel(new GridLayout(4, 1, 10, 10));
         * decodedPanel.setBorder(BorderFactory.createTitledBorder("Decodificado"));
         * for (int i = 0; i < 4; i++) {
         * decodedBoxes[i] = new JLabel("Vazio", SwingConstants.CENTER);
         * decodedBoxes[i].setOpaque(true);
         * decodedBoxes[i].setBackground(Color.LIGHT_GRAY);
         * decodedBoxes[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
         * decodedPanel.add(decodedBoxes[i]);
         * }
         */
        // Painel para Unidades Funcionais
        JPanel alu1Panel = new JPanel(new GridLayout(5, 1, 10, 10));
        alu1Panel.setBorder(BorderFactory.createTitledBorder("ALU1"));
        for (int i = 0; i < 5; i++) {
            ufBoxes[0][i] = new JLabel("Vazio", SwingConstants.CENTER);
            ufBoxes[0][i].setOpaque(true);
            ufBoxes[0][i].setBackground(Color.LIGHT_GRAY);
            ufBoxes[0][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            alu1Panel.add(ufBoxes[0][i]);
        }
        JPanel alu2Panel = new JPanel(new GridLayout(5, 1, 10, 10));
        alu2Panel.setBorder(BorderFactory.createTitledBorder("ALU2"));
        for (int i = 0; i < 5; i++) {
            ufBoxes[1][i] = new JLabel("Vazio", SwingConstants.CENTER);
            ufBoxes[1][i].setOpaque(true);
            ufBoxes[1][i].setBackground(Color.LIGHT_GRAY);
            ufBoxes[1][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            alu2Panel.add(ufBoxes[1][i]);
        }
        JPanel memPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        memPanel.setBorder(BorderFactory.createTitledBorder("MEM"));
        for (int i = 0; i < 5; i++) {
            ufBoxes[2][i] = new JLabel("Vazio", SwingConstants.CENTER);
            ufBoxes[2][i].setOpaque(true);
            ufBoxes[2][i].setBackground(Color.LIGHT_GRAY);
            ufBoxes[2][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            memPanel.add(ufBoxes[2][i]);
        }
        JPanel jmpPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        jmpPanel.setBorder(BorderFactory.createTitledBorder("JMP"));
        for (int i = 0; i < 5; i++) {
            ufBoxes[3][i] = new JLabel("Vazio", SwingConstants.CENTER);
            ufBoxes[3][i].setOpaque(true);
            ufBoxes[3][i].setBackground(Color.LIGHT_GRAY);
            ufBoxes[3][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            jmpPanel.add(ufBoxes[3][i]);
        }

        // add(decodedPanel);
        add(alu1Panel);
        add(alu2Panel);
        add(memPanel);
        add(jmpPanel);

        setVisible(false); // Será visível quando a simulação for iniciada
    }

    /**
     * Atualiza as instruções decodificadas no visualizador.
     * 
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
     * 
     * @param ufStatus Mapeamento do nome da unidade funcional para sua instrução
     *                 atual
     */
    public void updateUF(HashMap<String, List<Instruction>> ufStatus) {
        String[] ufNames = { "ALU1", "ALU2", "MEM", "JMP" };

        for (int j = 0; j < ufNames.length; j++) {
            String ufName = ufNames[j];
            
            // Garante que a lista de instruções tenha exatamente 5 elementos
            List<Instruction> instrs = IntStream.range(0, 5)
                    .mapToObj(i -> {
                        List<Instruction> list = ufStatus.getOrDefault(ufName, new ArrayList<>());
                        return (i < list.size()) ? list.get(i) : new Instruction(); // Use valores padrões
                    })
                    .toList();
        
            for (int i = 0; i < 5; i++) {
                Instruction instr = instrs.get(i);
                
                if ("VAZIO".equals(instr.codigo)) {
                    ufBoxes[j][i].setText(ufNames[j] + ": VAZIO");
                    ufBoxes[j][i].setBackground(Color.LIGHT_GRAY);
                } else {
                    ufBoxes[j][i].setText(ufNames[j] + ": " + instr.codigo + " (Ctx " + instr.contexto + ")");
                    // Colorir de acordo com o contexto
                    if (instr.contexto == 0) {
                        ufBoxes[j][i].setBackground(Color.ORANGE);
                    } else {
                        ufBoxes[j][i].setBackground(Color.GREEN);
                    }
                }
            }
        }
        
    }
}