import java.awt.*;
import javax.swing.*;

import java.util.ArrayList;
import java.awt.event.*;

public class EscalarPipelineViewer extends JFrame {

    private JPanel pipelinePanel;
    private ArrayList<JLabel> stageLabels;
    private JButton runButton; 
    private JComboBox<String> architectureTypeComboBox; // ComboBox para selecionar tipo de arquitetura
    private JComboBox<String> architectureComboBox; // ComboBox para seleção da arquitetura dentro do tipo

    public EscalarPipelineViewer() {
        setTitle("Visualização do Pipeline");
        setSize(900, 500); // Aumentar o tamanho para acomodar mais componentes
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
        controlPanel.setLayout(new FlowLayout());

        runButton = new JButton("Run"); 
        controlPanel.add(runButton);

        // Adiciona ComboBox para selecionar o tipo de arquitetura
        architectureTypeComboBox = new JComboBox<>(new String[]{"Escalar", "Superescalar"});
        controlPanel.add(new JLabel("Tipo de Arquitetura:"));
        controlPanel.add(architectureTypeComboBox);

        // Adiciona ComboBox para selecionar a arquitetura específica, inicialmente configurado para "Escalar"
        architectureComboBox = new JComboBox<>(new String[]{"IMT", "BMT", "REF"});
        controlPanel.add(new JLabel("Arquitetura:"));
        controlPanel.add(architectureComboBox);

        // Listener para alternar entre tipos de arquitetura
        architectureTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedType = (String) architectureTypeComboBox.getSelectedItem();
                if (selectedType.equals("Escalar")) {
                    architectureComboBox.setEnabled(true);
                    architectureComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"IMT", "BMT", "REF"}));
                } else { // Superescalar
                    architectureComboBox.setEnabled(false);
                    architectureComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"N/A"}));
                }
            }
        });

        // Painel de cabeçalho, pipeline e controles
        add(headerPanel, BorderLayout.NORTH); // Linha dos estágios fica no topo
        add(pipelinePanel, BorderLayout.CENTER); // Estágios do pipeline ficam no centro
        add(controlPanel, BorderLayout.SOUTH); // Botão Run e combos ficam na parte inferior

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

    public String getSelectedArchitectureType() {
        return (String) architectureTypeComboBox.getSelectedItem();
    }

    public String getSelectedArchitecture() {
        return (String) architectureComboBox.getSelectedItem();
    }
}
