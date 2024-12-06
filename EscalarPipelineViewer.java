import java.util.ArrayList;

import javax.swing.*;
import java.awt.*;


public class EscalarPipelineViewer extends JFrame {

    private JPanel pipelinePanel;
    private ArrayList<JLabel> stageLabels;
    private JButton runButton; 
    private JComboBox<String> architectureComboBox; // ComboBox para seleção da arquitetura

    public EscalarPipelineViewer() {
        setTitle("Visualização do Pipeline");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Painel para a linha de estágios
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridLayout(1, 5)); // 5 estágios: IF, ID, EX, MEM, WB
        String[] stages = { "IF", "ID", "EX", "MEM", "WB" };
        for (String stage : stages) {
            JLabel headerLabel = new JLabel(stage, SwingConstants.CENTER);
            headerLabel.setOpaque(true);
            headerLabel.setBackground(Color.GRAY);
            headerLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            headerPanel.add(headerLabel);
        }

        // Painel principal para os estágios do pipeline
        pipelinePanel = new JPanel();
        pipelinePanel.setLayout(new GridLayout(1, 5, 10, 10)); // 5 estágios: IF, ID, EX, MEM, WB
        stageLabels = new ArrayList<>();

        // Adicionando labels para os estágios
        for (String stage : stages) {
            JLabel label = new JLabel("NOP", SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(Color.LIGHT_GRAY);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            pipelinePanel.add(label);
            stageLabels.add(label);
        }

        // Painel para os controles
        JPanel controlPanel = new JPanel();
        runButton = new JButton("Run"); 
        controlPanel.add(runButton);

        // Adicionando ComboBox para selecionar a arquitetura
        architectureComboBox = new JComboBox<>(new String[]{"IMT", "BMT"});
        controlPanel.add(new JLabel("Arquitetura:"));
        controlPanel.add(architectureComboBox);

        // Adiciona o painel de cabeçalho, o painel principal e o painel de controle ao JFrame
        add(headerPanel, BorderLayout.NORTH); // Linha dos estágios fica no topo
        add(pipelinePanel, BorderLayout.CENTER); // Estágios do pipeline ficam no centro
        add(controlPanel, BorderLayout.SOUTH); // Botão Run e combo ficam na parte inferior

        setVisible(true);
    }

    private Color generateColor(int index) {
        int r = (index * 50) % 256; // Vermelho (ajusta o valor)
        int g = (index * 100) % 256; // Verde
        int b = (index * 150) % 256; // Azul
        return new Color(r, g, b);
    }

    public void updatePipeline(ArrayList<Instruction> pipeline, int cycle) {
        // Cada ciclo representa um avanço no pipeline
        // A instrução no ciclo N está no estágio IF
        // A instrução no ciclo N-1 está no estágio ID, e assim por diante

        // Atualiza cada estágio da pipeline
        for (int i = 0; i < stageLabels.size(); i++) {
            int currentIndex = cycle - i;
            if (currentIndex >= 0 && currentIndex < pipeline.size()) {
                Instruction instr = pipeline.get(currentIndex);
                stageLabels.get(i).setText(instr.inst + " (Ctx " + instr.contexto + ")");
                stageLabels.get(i).setBackground(generateColor(instr.contexto + 1));
                if (instr.inst.equals("BUB")) {
                    stageLabels.get(i).setBackground(Color.GRAY);
                }
            } else {
                // Para ciclos fora do alcance, exibe "NOP"
                stageLabels.get(i).setText("NOP");
                stageLabels.get(i).setBackground(Color.LIGHT_GRAY);
            }
        }

        // Atualiza o título da janela com o ciclo atual
        setTitle("Pipeline - Ciclo " + (cycle + 1));
    }

    public JButton getRunButton() {
        return runButton;
    }

    public String getSelectedArchitecture() {
        return (String) architectureComboBox.getSelectedItem();
    }
}