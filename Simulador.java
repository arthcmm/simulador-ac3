import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class Simulador {

    public static volatile boolean isPaused = false;      // Flag de pausa
    public static final Object pauseLock = new Object();  // Lock para pausar
    private static SwingWorker<Void, Void> currentWorker; // Referência para o SwingWorker atual

    public static void main(String[] args) {
        // Inicializar o objeto Escalar
        Escalar escalar = new Escalar();        
        escalar.createIMTPipeline(); // Cria IMTPipeline
        escalar.printPipeline(0);
        escalar.createBMTPipeline(); // Cria BMTPipeline
        escalar.printPipeline(1);
        escalar.createREFPipeline(); // Cria REF Pipeline
        escalar.printPipeline(2);

        // Inicializando a interface gráfica
        EscalarPipelineViewer viewer = new EscalarPipelineViewer();

        // Adicionando ActionListener ao botão Run
        viewer.getRunButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Desabilita o botão Run e habilita Pause e Stop
                viewer.getRunButton().setEnabled(false);
                viewer.getPauseButton().setEnabled(true);
                viewer.getStopButton().setEnabled(true);

                String selectedArch = viewer.getSelectedArchitecture();
                String selectedMode = viewer.getSelectedMode();

                if (selectedMode.equals("SUPERESCALAR")) {
                    // Modo SUPERESCALAR
                    SuperEscalar superEscalar = new SuperEscalar();
                    superEscalar.createIMTPipeline();

                    SimplePipelineVisualizer spv = new SimplePipelineVisualizer(superEscalar);
                    spv.setVisible(true);

                    // Criar e iniciar o SwingWorker para a simulação superescalar
                    currentWorker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            superEscalar.runPipeline(spv, this);
                            return null;
                        }

                        @Override
                        protected void done() {
                            viewer.getRunButton().setEnabled(true);
                            viewer.getPauseButton().setEnabled(false);
                            viewer.getStopButton().setEnabled(false);
                            try {
                                get(); // Verifica se houve exceções
                                if (!isCancelled()) {
                                    JOptionPane.showMessageDialog(viewer, "Simulação (Superescalar) concluída!", "Fim", JOptionPane.INFORMATION_MESSAGE);
                                }
                            } catch (Exception ex) {
                                if (isCancelled()) {
                                    JOptionPane.showMessageDialog(viewer, "Simulação interrompida!", "Interrompida", JOptionPane.WARNING_MESSAGE);
                                } else {
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(viewer, "Erro na simulação!", "Erro", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    };
                    currentWorker.execute();
                    return;
                }

                // Caso contrário, modo ESCALAR
                ArrayList<Instruction> selectedPipeline;

                if (selectedArch.equals("BMT")) {
                    selectedPipeline = escalar.bmtPipeline;
                    System.out.println("Executando BMTPipeline...");
                } else if (selectedArch.equals("IMT")) {
                    selectedPipeline = escalar.imtPipeline;
                    System.out.println("Executando IMTPipeline...");
                } else { // REF
                    selectedPipeline = escalar.refPipeline;
                    System.out.println("Executando REF Pipeline...");
                }

                int totalCiclos = selectedPipeline.size() + 5; // 5 estágios

                // Inicializar contadores de métricas
                final int[] totalCycles = {0};
                final int[] bubbleCycles = {0};
                final int[] executedInstructions = {0};

                // Criar e iniciar o SwingWorker para a simulação escalar
                currentWorker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        for (int i = 0; i < totalCiclos; i++) {
                            if (isCancelled()) {
                                break;
                            }

                            // Pausa a simulação se necessário
                            synchronized (pauseLock) {
                                while (isPaused) {
                                    try {
                                        pauseLock.wait();
                                    } catch (InterruptedException ex) {
                                        if (isCancelled()) {
                                            break;
                                        }
                                    }
                                }
                            }

                            final int cycle = i;
                            SwingUtilities.invokeLater(() -> viewer.updatePipeline(selectedPipeline, cycle));

                            // Atualizar os contadores
                            totalCycles[0]++;
                            // Verificar se há bolhas (BUB) no ciclo atual
                            if (selectedPipeline.size() > cycle) {
                                Instruction instr = selectedPipeline.get(cycle);
                                if (instr.inst.equals("BUB")) {
                                    bubbleCycles[0]++;
                                } else {
                                    executedInstructions[0]++;
                                }
                            }

                            // Calcular CPI
                            double cpi = executedInstructions[0] > 0 ? (double) totalCycles[0] / executedInstructions[0] : 0.0;

                            // Atualizar as métricas na interface gráfica
                            SwingUtilities.invokeLater(() -> {
                                viewer.updateTotalCycles(totalCycles[0]);
                                viewer.updateBubbleCycles(bubbleCycles[0]);
                                viewer.updateCPI(cpi);
                            });

                            // Simula o avanço dos ciclos
                            try {
                                Thread.sleep(500); // 0.5 segundos por ciclo
                            } catch (InterruptedException ex) {
                                if (isCancelled()) {
                                    break;
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        viewer.getRunButton().setEnabled(true);
                        viewer.getPauseButton().setEnabled(false);
                        viewer.getStopButton().setEnabled(false);
                        try {
                            get(); // Verifica se houve exceções
                            if (!isCancelled()) {
                                JOptionPane.showMessageDialog(viewer, "Simulação concluída!", "Fim", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception ex) {
                            if (isCancelled()) {
                                JOptionPane.showMessageDialog(viewer, "Simulação interrompida!", "Interrompida", JOptionPane.WARNING_MESSAGE);
                            } else {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(viewer, "Erro na simulação!", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                };
                currentWorker.execute();
            }
        });

        // Adicionando ActionListener ao botão Pause
        viewer.getPauseButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentWorker == null) {
                    return;
                }

                if (!isPaused) {
                    // Pausar a simulação
                    isPaused = true;
                    viewer.getPauseButton().setText("Resume");
                } else {
                    // Retomar a simulação
                    synchronized (pauseLock) {
                        isPaused = false;
                        pauseLock.notifyAll();
                    }
                    viewer.getPauseButton().setText("Pause");
                }
            }
        });

        // Adicionando ActionListener ao botão Stop
        viewer.getStopButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentWorker != null && !currentWorker.isDone()) {
                    currentWorker.cancel(true);
                }

                // Resetar os botões
                viewer.getRunButton().setEnabled(true);
                viewer.getPauseButton().setEnabled(false);
                viewer.getPauseButton().setText("Pause");
                viewer.getStopButton().setEnabled(false);
            }
        });
    }
}