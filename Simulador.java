import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Simulador {

    public static void main(String[] args) {
        // Inicializar o objeto Escalar
        Escalar escalar = new Escalar();        
        escalar.createBMTPipeline(); // Cria BMTPipeline
        escalar.printPipeline(1);
        escalar.createIMTPipeline(); // Cria IMTPipeline

        // Inicializando a interface gráfica
        EscalarPipelineViewer viewer = new EscalarPipelineViewer();

        // Adicionando ActionListener ao botão Run
        viewer.getRunButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Desabilita o botão Run para evitar múltiplos cliques
                viewer.getRunButton().setEnabled(false);

                // Seleciona qual pipeline utilizar com base na seleção do combo
                String selectedArch = viewer.getSelectedArchitecture();
                ArrayList<Instruction> selectedPipeline;

                if (selectedArch.equals("BMT")) {
                    selectedPipeline = escalar.bmtPipeline;
                    System.out.println("Executando BMTPipeline...");
                } else {
                    selectedPipeline = escalar.imtPipeline;
                    System.out.println("Executando IMTPipeline...");
                }

                // Calcula o número total de ciclos necessários
                int totalCiclos = selectedPipeline.size() + 5; // 5 estágios do pipeline

                // Utiliza SwingWorker para executar a simulação em uma thread separada
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // Simulando a execução do pipeline
                        for (int i = 0; i < totalCiclos; i++) {
                            final int cycle = i;

                            // Atualiza a pipeline no Event Dispatch Thread
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    viewer.updatePipeline(selectedPipeline, cycle);
                                }
                            });

                            // Simula o avanço dos ciclos
                            try {
                                Thread.sleep(500); // 0.5 segundos por ciclo
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        // Reabilita o botão Run após a simulação
                        viewer.getRunButton().setEnabled(true);
                        JOptionPane.showMessageDialog(viewer, "Simulação concluída!", "Fim", JOptionPane.INFORMATION_MESSAGE);
                    }
                };

                worker.execute();
            }
        });
    }

}