import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class EscalarPipelineViewer extends JFrame {

    private JPanel pipelinePanel;
    private ArrayList<JLabel> stageLabels;
    private JButton runButton; 
    private JButton pauseButton; // NOVO: Botão Pause
    private JButton stopButton;  // NOVO: Botão Stop
    private JComboBox<String> architectureComboBox; // ComboBox para seleção da arquitetura (IMT, BMT, REF)
    private JComboBox<String> modeComboBox;         // ComboBox para seleção entre ESCALAR e SUPERESCALAR

    public EscalarPipelineViewer() {
        setTitle("Visualização do Pipeline");
        setSize(1000, 400); // Ajustado para acomodar mais botões
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

        // NOVO: Botão Pause
        pauseButton = new JButton("Pause");
        pauseButton.setEnabled(false); // Inicialmente desabilitado
        controlPanel.add(pauseButton);

        // NOVO: Botão Stop
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false); // Inicialmente desabilitado
        controlPanel.add(stopButton);

        // Adiciona ComboBox para selecionar a arquitetura específica
        architectureComboBox = new JComboBox<>(new String[]{"IMT", "BMT", "REF"});
        controlPanel.add(new JLabel("Arquitetura:"));
        controlPanel.add(architectureComboBox);

        // Adiciona ComboBox para selecionar modo (ESCALAR ou SUPERESCALAR)
        modeComboBox = new JComboBox<>(new String[]{"ESCALAR", "SUPERESCALAR"});
        controlPanel.add(new JLabel("Modo:"));
        controlPanel.add(modeComboBox);

        // Painel de cabeçalho, pipeline e controles
        add(headerPanel, BorderLayout.NORTH);
        add(pipelinePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private Color generateColor(int index) {
        int r = (index * 50) % 256; // Vermelho (ajusta o valor)
        int g = (index * 100) % 256; // Verde
        int b = (index * 150) % 256; // Azul
        return new Color(r, g, b);
    }

    public void updatePipeline(ArrayList<Instruction> pipeline, int cycle) {
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
                stageLabels.get(i).setText("NOP");
                stageLabels.get(i).setBackground(Color.LIGHT_GRAY);
            }
        }
        setTitle("Pipeline - Ciclo " + (cycle + 1));
    }

    public JButton getRunButton() {
        return runButton;
    }

    public JButton getPauseButton() {
        return pauseButton;
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public String getSelectedArchitecture() {
        return (String) architectureComboBox.getSelectedItem();
    }

    public String getSelectedMode() {
        return (String) modeComboBox.getSelectedItem();
    }
}