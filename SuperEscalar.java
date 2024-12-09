import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class SuperEscalar {

    Contexto[] contextos = new Contexto[2];
    int totalInstructions = 0;
    int blockSize = 3;
    int totalCiclos = 0;
    public HashMap<String, Instruction> unidadesFuncionais = new HashMap<>();
    private int roundRobinContext = 0;

    public SuperEscalar() {
        String instruction;
        ArrayList<Instruction> instructions = new ArrayList<>();

        try {
            for (int i = 0; i < contextos.length; i++) {
                RandomAccessFile randomAccessFile = new RandomAccessFile("thread" + (i) + ".txt", "r");
                int count = 0;
                while ((instruction = randomAccessFile.readLine()) != null) {
                    String[] operands = instruction.split(" ");
                    Instruction inst = new Instruction(operands[0], operands[1], operands[2], operands[3], i);
                    // Define ciclos necessários dependendo da instrução
                    if (operands[0].equals("LDW") || operands[0].equals("JMP")) {
                        inst.ciclo = 2;
                    } else {
                        inst.ciclo = 1;
                    }
                    instructions.add(inst);
                    totalCiclos += inst.ciclo;
                    count++;
                }
                contextos[i] = new Contexto(i, instructions);
                instructions.clear();
                randomAccessFile.close();
            }

            unidadesFuncionais.put("ALU1", new Instruction());
            unidadesFuncionais.put("ALU2", new Instruction());
            unidadesFuncionais.put("JMP", new Instruction());
            unidadesFuncionais.put("MEM", new Instruction());

        } catch (IOException e) {
            System.out.println("exception");
            e.printStackTrace();
        }
    }

    public void createIMTPipeline() {
        for (Contexto contexto : contextos) {
            totalInstructions += contexto.qtdInstrucoes;
        }
        int[] ponteiros = new int[contextos.length];
        for (int i = 0; i < ponteiros.length; i++) {
            ponteiros[i] = 0;
        }
        // Aqui poderia haver lógica específica para pipeline IMT superescalar.
    }

    // Retorna verdadeiro se ainda há instruções para processar
    public boolean stepPipeline(ArrayList<Instruction> decodedForThisCycle) {
        if (canStopPipeline()) {
            return false;
        }

        int contextoAtual = selectContextToDecode();
        if (contextoAtual == -1) return false;

        Stack<Instruction> restantes = new Stack<>();

        // Decodifica até 4 instruções
        for (int j = 0; j < 4; j++) {
            if (!contextos[contextoAtual].instructions.isEmpty()) {
                Instruction instruction = contextos[contextoAtual].instructions.get(0);
                decodedForThisCycle.add(instruction);
                if (!processInstruction(instruction)) {
                    restantes.add(instruction);
                }
                contextos[contextoAtual].instructions.remove(0);
            }
        }

        // Recoloca as instruções não processadas
        while (!restantes.isEmpty()) {
            Instruction inst = restantes.pop();
            contextos[contextoAtual].instructions.add(0, inst);
        }

        return true;
    }

    private int selectContextToDecode() {
        for (int i = 0; i < contextos.length; i++) {
            int cIndex = (roundRobinContext + i) % contextos.length;
            if (!contextos[cIndex].instructions.isEmpty()) {
                roundRobinContext = (cIndex+1)%contextos.length;
                return cIndex;
            }
        }
        return -1;
    }

    public boolean canStopPipeline() {
        for (Contexto contexto : contextos) {
            if (!contexto.instructions.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean processInstruction(Instruction instruction) {
        String codigo = instruction.codigo;

        if (codigo.equals("ADD") || codigo.equals("SUB") || codigo.equals("MUL") || codigo.equals("DIV") || codigo.equals("CPY") || codigo.equals("DEL")) {
            if (unidadesFuncionais.get("ALU1").codigo.equals("VAZIO")) {
                unidadesFuncionais.put("ALU1", instruction);
                return true;
            } else if (unidadesFuncionais.get("ALU2").codigo.equals("VAZIO")) {
                unidadesFuncionais.put("ALU2", instruction);
                return true;
            }
        }

        if (codigo.equals("LDW")) {
            if (unidadesFuncionais.get("MEM").codigo.equals("VAZIO")) {
                unidadesFuncionais.put("MEM", instruction);
                return true;
            }
        }

        if (codigo.equals("JMP")) {
            if (unidadesFuncionais.get("JMP").codigo.equals("VAZIO")) {
                unidadesFuncionais.put("JMP", instruction);
                return true;
            }
        }

        if (codigo.equals("NOP")) {
            return true;
        }

        return false;
    }

    public int limparUF() {
        int finalizadas = 0;
        finalizadas += checkAndCleanUF("ALU1");
        finalizadas += checkAndCleanUF("ALU2");
        finalizadas += checkAndCleanUF("MEM");
        finalizadas += checkAndCleanUF("JMP");
        return finalizadas;
    }

    private int checkAndCleanUF(String uf) {
        Instruction inst = unidadesFuncionais.get(uf);
        inst.ciclo = inst.ciclo - 1;
        if (inst.ciclo <= 0) {
            if (!inst.codigo.equals("VAZIO")) {
                unidadesFuncionais.put(uf, new Instruction());
                return 1;
            }
            unidadesFuncionais.put(uf, new Instruction());
        } else {
            unidadesFuncionais.put(uf, inst);
        }
        return 0;
    }

    // Executa o pipeline ciclo a ciclo atualizando o visualizador
    public void runPipeline(SimplePipelineVisualizer visualizer) {
        while(!canStopPipeline()) {
            ArrayList<Instruction> decoded = new ArrayList<>();
            stepPipeline(decoded);
            // Atualiza o visualizador
            visualizer.updateDecoded(decoded);
            visualizer.updateUF(unidadesFuncionais);
            limparUF();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Última atualização pós-fim
        visualizer.updateDecoded(new ArrayList<>());
        visualizer.updateUF(unidadesFuncionais);
    }
}