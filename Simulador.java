import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class Simulador {

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
                viewer.getRunButton().setEnabled(false);

                String selectedArch = viewer.getSelectedArchitecture();
                String selectedMode = viewer.getSelectedMode();

                if (selectedMode.equals("SUPERESCALAR")) {
                    // Modo SUPERESCALAR
                    SuperEscalar superEscalar = new SuperEscalar();
                    superEscalar.createIMTPipeline();

                    SimplePipelineVisualizer spv = new SimplePipelineVisualizer(superEscalar);
                    spv.setVisible(true);

                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            // Executa a lógica superescalar
                            superEscalar.runPipeline(spv);
                            return null;
                        }

                        @Override
                        protected void done() {
                            viewer.getRunButton().setEnabled(true);
                            JOptionPane.showMessageDialog(viewer, "Simulação (Superescalar) concluída!", "Fim", JOptionPane.INFORMATION_MESSAGE);
                        }
                    };
                    worker.execute();
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

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        for (int i = 0; i < totalCiclos; i++) {
                            final int cycle = i;
                            SwingUtilities.invokeLater(() -> viewer.updatePipeline(selectedPipeline, cycle));
                            try {
                                Thread.sleep(500); 
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        viewer.getRunButton().setEnabled(true);
                        JOptionPane.showMessageDialog(viewer, "Simulação concluída!", "Fim", JOptionPane.INFORMATION_MESSAGE);
                    }
                };

                worker.execute();
            }
        });
    }

}