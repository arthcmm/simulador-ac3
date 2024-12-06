import java.util.ArrayList;

import javax.swing.*;
import java.awt.*;


public class EscalarPipelineViewer extends JFrame {

    private JPanel pipelinePanel;
    private ArrayList<JLabel> stageLabels;

    public EscalarPipelineViewer() {
        setTitle("Visualização do Pipeline");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Painel principal para os estágios do pipeline
        pipelinePanel = new JPanel();
        pipelinePanel.setLayout(new GridLayout(1, 5, 10, 10)); // 5 estágios: IF, ID, EX, MEM, WB
        stageLabels = new ArrayList<>();

        // Adicionando labels para os estágios
        String[] stages = { "IF", "ID", "EX", "MEM", "WB" };
        for (String stage : stages) {
            JLabel label = new JLabel(stage, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(Color.LIGHT_GRAY);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            pipelinePanel.add(label);
            stageLabels.add(label);
        }

        // Painel para a linha de estágios
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridLayout(1, 5)); // 5 estágios: IF, ID, EX, MEM, WB
        for (String stage : stages) {
            JLabel headerLabel = new JLabel(stage, SwingConstants.CENTER);
            headerLabel.setOpaque(true);
            headerLabel.setBackground(Color.GRAY);
            headerLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            headerPanel.add(headerLabel);
        }

        // Adiciona o painel de cabeçalho e o painel principal ao JFrame
        add(headerPanel, BorderLayout.NORTH); // Linha dos estágios fica no topo
        add(pipelinePanel, BorderLayout.CENTER); // Estágios do pipeline ficam no centro

        setVisible(true);
    }

    private Color generateColor(int index) {
        int r = (index * 50) % 256;  // Vermelho (ajusta o valor)
        int g = (index * 100) % 256; // Verde
        int b = (index * 150) % 256; // Azul
        return new Color(r, g, b);
    }

    public void updatePipeline(ArrayList<Instruction> pipeline, int cycle) {

        int pointer = cycle; // Posição atual no ciclo

        // Verifica se o pipeline está vazio
        if (pipeline.isEmpty()) {
            for (JLabel label : stageLabels) {
                label.setText("PIPELINE VAZIO");
                label.setBackground(Color.GRAY);
            }
            return;
        }

        // Atualiza cada estágio da pipeline
        for (int i = 0; i < stageLabels.size(); i++) {
            int currentIndex = pointer - i;

            if (currentIndex >= 0 && currentIndex < pipeline.size()) {
                Instruction instr = pipeline.get(currentIndex);

                // Define o texto e a cor de acordo com o contexto
                stageLabels.get(i).setText(instr.inst + " (Ctx " + instr.contexto + ")");
                stageLabels.get(i).setBackground(generateColor(instr.contexto + 1));
                // stageLabels.get(i).setBackground(instr.contexto == 0 ? Color.RED : Color.GREEN);
                if (instr.inst.compareTo("BUB") == 0) {
                    stageLabels.get(i).setBackground(Color.GRAY);
                }
            } else {
                // Para ciclos fora do alcance, exibe "NOP"
                stageLabels.get(i).setText("NOP");
                stageLabels.get(i).setBackground(Color.LIGHT_GRAY);
            }
        }

        // Atualiza o título da janela com o ciclo atual
        if (cycle < pipeline.size() + 5) {
            setTitle("Pipeline - Ciclo " + cycle);
        }
    }
}