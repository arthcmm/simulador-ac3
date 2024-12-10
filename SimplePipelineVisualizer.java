import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class SimplePipelineVisualizer extends JFrame {
    public int limBoxes = 5;
    private JLabel[] ciclosBoxes = new JLabel[limBoxes];
    private JLabel[][] ufBoxes = new JLabel[4][limBoxes];
    SuperEscalar superEscalar;

    // Novos botões
    private JButton runButtonSuper;
    private JButton pauseButtonSuper;
    private JButton stopButtonSuper;

    // Referência para a interface principal
    private EscalarPipelineViewer mainViewer;

    public SimplePipelineVisualizer(SuperEscalar superEscalar, EscalarPipelineViewer mainViewer) {
        this.superEscalar = superEscalar;
        this.mainViewer = mainViewer;
        setTitle("Pipeline Visualizer (Superescalar)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // Usando BorderLayout para organizar componentes

        // Painel principal para os ciclos e unidades funcionais
        JPanel mainPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        add(mainPanel, BorderLayout.CENTER);

        // Painel para ciclos
        JPanel ciclos = new JPanel(new GridLayout(limBoxes, 1, 10, 10));
        ciclos.setBorder(BorderFactory.createTitledBorder("Ciclo"));
        for (int i = 0; i < limBoxes; i++) {
            ciclosBoxes[i] = new JLabel("Vazio", SwingConstants.CENTER);
            ciclosBoxes[i].setOpaque(true);
            ciclosBoxes[i].setBackground(Color.LIGHT_GRAY);
            ciclosBoxes[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            ciclos.add(ciclosBoxes[i]);
        }
        mainPanel.add(ciclos);

        // Painéis para as Unidades Funcionais
        String[] ufs = { "ALU1", "ALU2", "MEM", "JMP" };
        for (int j = 0; j < ufs.length; j++) {
            JPanel ufPanel = new JPanel(new GridLayout(limBoxes, 1, 10, 10));
            ufPanel.setBorder(BorderFactory.createTitledBorder(ufs[j]));
            for (int i = 0; i < limBoxes; i++) {
                ufBoxes[j][i] = new JLabel("Vazio", SwingConstants.CENTER);
                ufBoxes[j][i].setOpaque(true);
                ufBoxes[j][i].setBackground(Color.LIGHT_GRAY);
                ufBoxes[j][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                ufPanel.add(ufBoxes[j][i]);
            }
            mainPanel.add(ufPanel);
        }

        // Painel para os botões de controle
        JPanel controlPanelSuper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanelSuper.setBorder(BorderFactory.createTitledBorder("Controles da Simulação SUPERESCALAR"));

        runButtonSuper = new JButton("Run");
        pauseButtonSuper = new JButton("Pause");
        stopButtonSuper = new JButton("Stop");

        // Inicialmente, Pause e Stop estão desabilitados até que a simulação seja iniciada
        runButtonSuper.setEnabled(true);
        pauseButtonSuper.setEnabled(false);
        stopButtonSuper.setEnabled(false);

        controlPanelSuper.add(runButtonSuper);
        controlPanelSuper.add(pauseButtonSuper);
        controlPanelSuper.add(stopButtonSuper);

        add(controlPanelSuper, BorderLayout.SOUTH);

        // Implementar os ActionListeners para os botões
        runButtonSuper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Iniciar a simulação
                runButtonSuper.setEnabled(false);
                pauseButtonSuper.setEnabled(true);
                stopButtonSuper.setEnabled(true);

                // Garantir que a flag de pausa esteja desativada
                Simulador.isPaused = false;
                synchronized (Simulador.pauseLock) {
                    Simulador.pauseLock.notifyAll();
                }

                // Se a simulação já estiver rodando, não iniciar novamente
                if (Simulador.currentWorker != null && !Simulador.currentWorker.isDone()) {
                    return;
                }

                // Criar e iniciar o SwingWorker para a simulação superescalar
                Simulador.currentWorker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        superEscalar.runPipeline(SimplePipelineVisualizer.this, this);
                        return null;
                    }

                    @Override
                    protected void done() {
                        // Redefinir a flag de pausa
                        Simulador.isPaused = false;
                        synchronized (Simulador.pauseLock) {
                            Simulador.pauseLock.notifyAll();
                        }

                        runButtonSuper.setEnabled(true);
                        pauseButtonSuper.setEnabled(false);
                        stopButtonSuper.setEnabled(false);
                        pauseButtonSuper.setText("Pause");
                        try {
                            get(); // Verifica se houve exceções
                            if (!isCancelled()) {
                                JOptionPane.showMessageDialog(SimplePipelineVisualizer.this,
                                        "Simulação (Superescalar) concluída!", "Fim",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception ex) {
                            if (isCancelled()) {
                                JOptionPane.showMessageDialog(SimplePipelineVisualizer.this,
                                        "Simulação interrompida!", "Interrompida",
                                        JOptionPane.WARNING_MESSAGE);
                            } else {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(SimplePipelineVisualizer.this,
                                        "Erro na simulação!", "Erro",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                };
                Simulador.currentWorker.execute();
            }
        });

        pauseButtonSuper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Simulador.currentWorker == null) {
                    return;
                }

                if (!Simulador.isPaused) {
                    // Pausar a simulação
                    Simulador.isPaused = true;
                    pauseButtonSuper.setText("Resume");
                } else {
                    // Retomar a simulação
                    synchronized (Simulador.pauseLock) {
                        Simulador.isPaused = false;
                        Simulador.pauseLock.notifyAll();
                    }
                    pauseButtonSuper.setText("Pause");
                }
            }
        });

        stopButtonSuper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Simulador.currentWorker != null && !Simulador.currentWorker.isDone()) {
                    Simulador.currentWorker.cancel(true);
                }

                // Redefinir a flag de pausa e notificar todas as threads aguardando
                Simulador.isPaused = false;
                synchronized (Simulador.pauseLock) {
                    Simulador.pauseLock.notifyAll();
                }

                // Resetar os botões
                runButtonSuper.setEnabled(true);
                pauseButtonSuper.setEnabled(false);
                pauseButtonSuper.setText("Pause");
                stopButtonSuper.setEnabled(false);
            }
        });

        // Adicionar um WindowListener para detectar quando a janela é fechada
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Se uma simulação estiver em execução, cancelar
                if (Simulador.currentWorker != null && !Simulador.currentWorker.isDone()) {
                    Simulador.currentWorker.cancel(true);
                }

                // Redefinir a flag de pausa e notificar todas as threads aguardando
                Simulador.isPaused = false;
                synchronized (Simulador.pauseLock) {
                    Simulador.pauseLock.notifyAll();
                }

                // Reabilitar os botões na interface principal
                mainViewer.getRunButton().setEnabled(true);
                mainViewer.getPauseButton().setEnabled(false);
                mainViewer.getStopButton().setEnabled(false);
                mainViewer.getPauseButton().setText("Pause");
            }
        });

        setVisible(true); // Torna a janela visível imediatamente
    }

    /**
     * Atualiza o pipeline SUPERESCALAR no visualizador.
     * 
     * @param instructions Instruções atuais do pipeline
     * @param ciclo        Ciclo atual da simulação
     */
    public void updateUfCerto(ArrayList<Instruction> instructions, int ciclo) {
        puxaPraBaixo();
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instr = instructions.get(i);
            ciclosBoxes[0].setText("Ciclo : " + ciclo);
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