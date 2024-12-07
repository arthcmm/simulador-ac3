import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

class Simulador {

    public static void main(String[] args) {

        SuperEscalar superEscalar = new SuperEscalar();
        superEscalar.createIMTPipeline();
        superEscalar.printPipeline();
        rodarSuperEscalar();
        // superEscalar.simulatePipelineStages();

    }

    public static void rodarEscalar() {

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

    public static void rodarSuperEscalar() {
        SuperEscalar superEscalar = new SuperEscalar();
        superEscalar.createIMTPipeline();
        SimplePipelineVisualizer visualizer = new SimplePipelineVisualizer(superEscalar);

        HashMap<String, Instruction> ufStatus = new HashMap<>();
        Stack<Instruction> restantes = new Stack<>();
        ArrayList<Instruction> estacaoReserva = new ArrayList<>();
        for (int i = 0; i < superEscalar.totalCiclos; i++) {

            int contextoAtual = i % superEscalar.contextos.length;
            ArrayList<Instruction> decoded = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                if (!superEscalar.contextos[contextoAtual].instructions.isEmpty()) {
                    Instruction instruction = superEscalar.contextos[contextoAtual].instructions.get(0);
                    // System.out.print(instruction.codigo + " ");
                    decoded.add(instruction);
                    if (!superEscalar.processInstruction(instruction)) {
                        restantes.add(instruction); // Adiciona à fila de espera
                        estacaoReserva.add(instruction);
                    }
                    superEscalar.contextos[contextoAtual].instructions.remove(0); // Remove do estágio IF
                }

            }

            ufStatus.put("ALU1", superEscalar.unidadesFuncionais.get("ALU1"));
            ufStatus.put("ALU2", superEscalar.unidadesFuncionais.get("ALU2"));
            ufStatus.put("MEM", superEscalar.unidadesFuncionais.get("MEM"));
            ufStatus.put("JMP", superEscalar.unidadesFuncionais.get("JMP"));

            while (!restantes.isEmpty()) {
                Instruction inst = restantes.peek();
                superEscalar.contextos[contextoAtual].instructions.addFirst(inst);
                restantes.pop();
            }

            superEscalar.limparUF();

            visualizer.updateDecoded(decoded);
            visualizer.updateUF(ufStatus);
            try {
                Thread.sleep(1000); // 1 segundo por ciclo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}