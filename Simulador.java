
class Simulador {

    public static void main(String[] args) {
        Escalar escalar = new Escalar();
        escalar.createBMTPipeline();
        escalar.printPipeline(1);

        escalar.createIMTPipeline();
        // escalar.printPipeline(0);

        // escalar.createBMTPipeline(); // Cria o pipeline BMT

        // Inicializando a interface gráfica
        EscalarPipelineViewer viewer = new EscalarPipelineViewer();

        // Simulando a execução do pipeline
        for (int i = 0; i < escalar.totalInstructions; i++) {
            viewer.updatePipeline(escalar.imtPipeline, i);

            // Simula o avanço dos ciclos
            try {
                Thread.sleep(500); // 1 segundo por ciclo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}