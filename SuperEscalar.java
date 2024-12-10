import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;
import javax.swing.SwingWorker;

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
                RandomAccessFile randomAccessFile = new RandomAccessFile("thread" + i + ".txt", "r");
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
                }
                contextos[i] = new Contexto(i, instructions);
                instructions.clear();
                randomAccessFile.close();
            }

            // Inicializar unidades funcionais como vazias
            unidadesFuncionais.put("ALU1", new Instruction());
            unidadesFuncionais.put("ALU2", new Instruction());
            unidadesFuncionais.put("MEM", new Instruction());
            unidadesFuncionais.put("JMP", new Instruction());

            //VERDADEIRO PIPELINE ESTÁ AQUI :)
            calculateThreadSequenceNotSMT(contextos, 1);

        } catch (IOException e) {
            System.out.println("Exception ao ler arquivos de thread.");
            e.printStackTrace();
        }
    }

    public void createIMTPipeline() {
        // Implementação específica para o pipeline IMT superescalar
        // Aqui, vamos intercalar a decodificação de instruções de ambos os contextos
        // Para garantir balanceamento, decodificamos 2 instruções de cada contexto por ciclo

        // Nenhuma implementação necessária aqui, pois a lógica de decodificação está no runPipeline
    }

    /**
     * Executa o pipeline ciclo a ciclo atualizando o visualizador.
     * 
     * @param visualizer O visualizador para atualizar o estado do pipeline
     * @param worker O SwingWorker que está executando a simulação
     */
    public void runPipeline(SimplePipelineVisualizer visualizer, SwingWorker<?, ?> worker) {
        while (!canStopPipeline()) {
            if (worker.isCancelled()) {
                break;
            }

            ArrayList<Instruction> decoded = new ArrayList<>();
            boolean continuePipeline = stepPipeline(decoded, worker);

            // Atualiza o visualizador
            visualizer.updateDecoded(decoded);
            visualizer.updateUF(unidadesFuncionais);
            limparUF();

            // Pausa a simulação se necessário
            synchronized (Simulador.pauseLock) {
                while (Simulador.isPaused) {
                    try {
                        Simulador.pauseLock.wait();
                    } catch (InterruptedException e) {
                        if (worker.isCancelled()) {
                            break;
                        }
                    }
                }
            }

            try {
                Thread.sleep(500); // 0.5 segundos por ciclo
            } catch (InterruptedException e) {
                if (worker.isCancelled()) {
                    break;
                }
            }
        }

        // Última atualização pós-fim
        visualizer.updateDecoded(new ArrayList<>());
        visualizer.updateUF(unidadesFuncionais);
    }

    /**
     * Decodifica e processa instruções de múltiplos contextos de forma balanceada.
     * 
     * @param decodedForThisCycle Lista de instruções decodificadas neste ciclo
     * @param worker O SwingWorker que está executando a simulação
     * @return true se o pipeline deve continuar, false caso contrário
     */
    public boolean stepPipeline(ArrayList<Instruction> decodedForThisCycle, SwingWorker<?, ?> worker) {
        if (canStopPipeline() || worker.isCancelled()) {
            return false;
        }

        // Decodificar 2 instruções de cada contexto para balanceamento
        for (int c = 0; c < contextos.length; c++) {
            int contextoAtual = (roundRobinContext + c) % contextos.length;
            for (int j = 0; j < 2; j++) { // Decodificar 2 instruções por contexto
                if (!contextos[contextoAtual].instructions.isEmpty()) {
                    Instruction instruction = contextos[contextoAtual].instructions.get(0);
                    decodedForThisCycle.add(instruction);
                    if (!processInstruction(instruction)) {
                        // Instrução não processada, pode ser adicionada a uma fila de espera se necessário
                        // Implementar lógica de fila de espera se necessário
                    }
                    contextos[contextoAtual].instructions.remove(0);
                }
            }
        }

        return true;
    }

    private int selectContextToDecode() {
        for (int i = 0; i < contextos.length; i++) {
            int cIndex = (roundRobinContext + i) % contextos.length;
            if (!contextos[cIndex].instructions.isEmpty()) {
                roundRobinContext = (cIndex + 1) % contextos.length;
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
        int contexto = instruction.contexto;

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

        return false; // Instrução não foi processada, deve ir para a fila de espera
    }

    public ArrayList<ArrayList<Instruction>> calculateThreadSequenceNotSMT(Contexto[] threads,
            int blockSize) {
        int numberOfThreads = threads.length;
        // Instruções finais por ciclo
        ArrayList<ArrayList<Instruction>> instructions = new ArrayList<>();
        @SuppressWarnings("unchecked")
        HashMap<String, Instruction[]>[] allQueues = new HashMap[numberOfThreads];
        // Lendo as threads
        for (int threadNumber = 0; threadNumber < numberOfThreads; threadNumber++) {
            //Queue de cada UF
            HashMap<String, Instruction[]> queues = new HashMap<>();
            ArrayList<Instruction> threadInstructions = threads[threadNumber].instructions;
            // Registrador renomeado
            int currentNewRegister = 65;
            queues.put("ALU1", new Instruction[threadInstructions.size() * 2]);
            queues.put("ALU2", new Instruction[threadInstructions.size() * 2]);
            queues.put("MEM", new Instruction[threadInstructions.size() * 2]);
            queues.put("JMP", new Instruction[threadInstructions.size() * 2]);
            for (int i = 0; i < threadInstructions.size(); i++) {
                Instruction instruction = threadInstructions.get(i);
                instruction.id = i;
                String dest = instruction.dest;
                String op1 = instruction.op1;
                String op2 = instruction.op2;
                for (int j = 0; j < i; j++) {
                    Instruction prevInstruction = threadInstructions.get(j);
                    switch (instruction.inst) {
                        case "ADD", "SUB", "MUL", "DIV", "CPY", "DEL":
                            // Dependencia verdadeira -> Leitura após escrita
                            if (instructionWrites(prevInstruction.inst)
                                    && (prevInstruction.dest.equals(op2) || prevInstruction.dest.equals(op1))) {
                                instruction.needsToBeAfterId = Math.max(prevInstruction.id,
                                        instruction.needsToBeAfterId);
                            }
                            // Dependencia de saída -> Escrita após escrita
                            if (instructionWrites(prevInstruction.inst) && (prevInstruction.dest.equals(dest))) {
                                instruction.dest = "R" + (char) currentNewRegister;
                                currentNewRegister++;
                            }
                            // Anti-dependência -> Escrita após leitura
                            if (dest.equals(prevInstruction.op1) || dest.equals(prevInstruction.op2)
                                    || (instructionReadsInDest(prevInstruction.inst)
                                            && (prevInstruction.dest.equals(dest)))) {
                                instruction.dest = "R" + (char) currentNewRegister;
                                currentNewRegister++;
                            }
                            break;
                        case "LDW":
                            // Dependencia verdadeira -> Leitura após escrita
                            if (instructionWrites(prevInstruction.inst) && (prevInstruction.dest.equals(op2))) {
                                instruction.needsToBeAfterId = Math.max(prevInstruction.id,
                                        instruction.needsToBeAfterId);
                            }
                            // Dependencia de saída -> Escrita após escrita
                            if (instructionWrites(prevInstruction.inst) && (prevInstruction.dest.equals(dest))) {
                                instruction.dest = "R" + (char) currentNewRegister;
                                currentNewRegister++;
                            }
                            // Anti-dependência -> Escrita após leitura
                            if (dest.equals(prevInstruction.op1) || dest.equals(prevInstruction.op2)
                                    || (instructionReadsInDest(prevInstruction.inst)
                                            && (prevInstruction.dest.equals(dest)))) {
                                instruction.dest = "R" + (char) currentNewRegister;
                                currentNewRegister++;
                            }
                            break;
                        case "JMP":
                            // Dependencia verdadeira -> Leitura após escrita
                            if (instructionWrites(prevInstruction.inst) && (prevInstruction.dest.equals(op2)
                                    || prevInstruction.dest.equals(op1) || prevInstruction.dest.equals(dest))) {
                                instruction.needsToBeAfterId = Math.max(prevInstruction.id,
                                        instruction.needsToBeAfterId);
                            }
                            break;
                        case "STW":
                            // Dependencia verdadeira -> Leitura após escrita
                            if (instructionWrites(prevInstruction.inst)
                                    && (prevInstruction.dest.equals(op2) || prevInstruction.dest.equals(dest))) {
                                instruction.needsToBeAfterId = Math.max(prevInstruction.id,
                                        instruction.needsToBeAfterId);
                            }
                            break;
                        case "NOP":
                            break;
                    }
                }
            }
            // Organizando as instruções nas filas, de acordo com as dependências
            for (int i = 0; i < threadInstructions.size(); i++) {
                Instruction instruction = threadInstructions.get(i);
                String uf = getUF(instruction.inst);
                // Tem dependencia verdadeira 
                if (instruction.needsToBeAfterId == -1) { //Não tem dependência verdadeira
                    if (uf.equals("ALU")) {
                        int p = 0;
                        while (queues.get("ALU1")[p] != null && queues.get("ALU2")[p] != null) {
                            p++;
                        }
                        if (queues.get("ALU1")[p] == null) {
                            for (int c = 0; c < instruction.ciclo; c++) {
                                queues.get("ALU1")[p + c] = instruction;
                            }
                        } else {
                            for (int c = 0; c < instruction.ciclo; c++) {
                                queues.get("ALU2")[p + c] = instruction;
                            }
                        }
                    } else {
                        int p = 0;
                        while (queues.get(uf)[p] != null) {
                            p++;
                        }
                        for (int c = 0; c < instruction.ciclo; c++) {
                            queues.get(uf)[p + c] = instruction;
                        }
                    }
                } else { // Tem dependência verdadeira
                    if (uf.equals("ALU")) {
                        int p = 0;
                        int foundAt = -1;
                        while (p < threadInstructions.size()) {
                            if (queues.get("ALU1")[p] != null
                                    && queues.get("ALU1")[p].id != instruction.needsToBeAfterId) {
                                foundAt = p;
                            }
                            p++;
                        }
                        p = 0;
                        while (p < threadInstructions.size()) {
                            if (queues.get("ALU2")[p] != null
                                    && queues.get("ALU2")[p].id != instruction.needsToBeAfterId) {
                                foundAt = p;
                            }
                            p++;
                        }
                        if (foundAt != -1) {
                            p = foundAt + 1;
                            while (queues.get("ALU1")[p] != null && queues.get("ALU2")[p] != null) {
                                p++;
                            }
                            if (queues.get("ALU1")[p] == null) {
                                for (int c = 0; c < instruction.ciclo; c++) {
                                    queues.get("ALU1")[p + c] = instruction;
                                }
                            } else {
                                for (int c = 0; c < instruction.ciclo; c++) {
                                    queues.get("ALU2")[p + c] = instruction;
                                }
                            }
                        } else {
                            p = 0;
                            while (queues.get("ALU1")[p] != null && queues.get("ALU2")[p] != null) {
                                p++;
                            }
                            if (queues.get("ALU1")[p] == null) {
                                for (int c = 0; c < instruction.ciclo; c++) {
                                    queues.get("ALU1")[p + c] = instruction;
                                }
                            } else {
                                for (int c = 0; c < instruction.ciclo; c++) {
                                    queues.get("ALU2")[p + c] = instruction;
                                }
                            }
                        }
                    } else {
                        int p = 0;
                        int foundAt = -1;
                        while (p < threadInstructions.size()) {
                            if (queues.get(uf)[p] != null && queues.get(uf)[p].id != instruction.needsToBeAfterId) {
                                foundAt = p;
                            }
                            p++;
                        }
                        if (foundAt != -1) {
                            for (int c = 0; c < instruction.ciclo; c++) {
                                queues.get(uf)[foundAt + 1 + c] = instruction;
                            }
                        } else {
                            p = 0;
                            while (queues.get(uf)[p] != null) {
                                p++;
                            }
                            for (int c = 0; c < instruction.ciclo; c++) {
                                queues.get(uf)[p + c] = instruction;
                            }
                        }
                    }
                }
            }
            allQueues[threadNumber] = queues;
        }
        int[] ponteiros = new int[numberOfThreads];
        boolean[] finished = new boolean[numberOfThreads];
        for (int i = 0; i < ponteiros.length; i++) {
            ponteiros[i] = 0;
            finished[i] = false;
        }
        while(!allFinished(finished)){
            for(int j = 0; j < numberOfThreads; j++){
                if(finished[j]){
                    continue;
                }
                // Mais de um ciclo?
                boolean needRepeatMEM = false;
                boolean needRepeatJMP = false;
                for(int k = 0; k < blockSize || needRepeatMEM || needRepeatJMP; k++){
                    ArrayList<Instruction> currentInstructions = new ArrayList<>();
                    Instruction[] currentQueue = allQueues[j].get("ALU1");
                    if(currentQueue[ponteiros[j]] != null){
                        currentInstructions.add(currentQueue[ponteiros[j]]);
                    } else {
                        currentInstructions.add(new Instruction("BUB", "0", "0", "0", j));
                    }
                    currentQueue = allQueues[j].get("ALU2");
                    if(currentQueue[ponteiros[j]] != null){
                        currentInstructions.add(currentQueue[ponteiros[j]]);
                    } else {
                        currentInstructions.add(new Instruction("BUB", "0", "0", "0", j));
                    }
                    currentQueue = allQueues[j].get("MEM");
                    if(currentQueue[ponteiros[j]] != null){
                        needRepeatMEM = !needRepeatMEM;
                        currentInstructions.add(currentQueue[ponteiros[j]]);
                    } else {
                        currentInstructions.add(new Instruction("BUB", "0", "0", "0", j));
                    }
                    currentQueue = allQueues[j].get("JMP");
                    if(currentQueue[ponteiros[j]] != null){
                        needRepeatJMP = !needRepeatJMP;
                        currentInstructions.add(currentQueue[ponteiros[j]]);
                    } else {
                        currentInstructions.add(new Instruction("BUB", "0", "0", "0", j));
                    }
                    if(currentInstructions.get(0).inst.equals("BUB") && currentInstructions.get(1).inst.equals("BUB") && currentInstructions.get(2).inst.equals("BUB") && currentInstructions.get(3).inst.equals("BUB")){
                        finished[j] = true;
                    }else {
                        instructions.add(currentInstructions);
                    }
                    System.out.println("Thread " + j + ": "+ currentInstructions.toString());
                    ponteiros[j]++;
                }
            }
        }
        
        return instructions;
    }

    public boolean allFinished(boolean[] finished) {
        for (boolean f : finished) {
            if (!f) {
                return false;
            }
        }
        return true;
    }

    public boolean instructionWrites(String inst) {
        return inst.equals("ADD") || inst.equals("SUB") || inst.equals("MUL") || inst.equals("DIV")
                || inst.equals("CPY") || inst.equals("LDW");
    }

    public boolean instructionReadsInDest(String inst) {
        return inst.equals("JMP") || inst.equals("STW");
    }

    public String getUF(String inst) {
        String uf = "";
        switch (inst) {
            case "ADD", "SUB", "MUL", "DIV", "CPY", "DEL":
                uf = "ALU";
                break;
            case "LDW", "STW":
                uf = "MEM";
                break;
            case "JMP":
                uf = "JMP";
                break;
            case "NOP":
                break;
        }
        return uf;
    }

    public void limparUF() {
        int finalizadas = 0;
        finalizadas += checkAndCleanUF("ALU1");
        finalizadas += checkAndCleanUF("ALU2");
        finalizadas += checkAndCleanUF("MEM");
        finalizadas += checkAndCleanUF("JMP");
        // Opcional: pode ser usado para estatísticas ou logs
        System.out.println("Instruções finalizadas neste ciclo: " + finalizadas);
    }

    private int checkAndCleanUF(String uf) {
        Instruction inst = unidadesFuncionais.get(uf);
        if (!inst.codigo.equals("VAZIO")) {
            inst.ciclo = inst.ciclo - 1;
            if (inst.ciclo <= 0) {
                unidadesFuncionais.put(uf, new Instruction());
                return 1;
            }
            unidadesFuncionais.put(uf, inst);
        }
        return 0;
    }

    // Método para rodar a simulação do pipeline passo a passo e atualizar o
    // visualizador
    // public void runPipeline(SimplePipelineVisualizer visualizer, SwingWorker<?,
    // ?> worker) {
    // while (!canStopPipeline()) {
    // if (worker.isCancelled()) {
    // break;
    // }

    // ArrayList<Instruction> decoded = new ArrayList<>();
    // boolean continuePipeline = stepPipeline(decoded, worker);

    // // Atualiza o visualizador
    // visualizer.updateDecoded(decoded);
    // visualizer.updateUF(unidadesFuncionais);
    // limparUF();

    // // Pausa a simulação se necessário
    // synchronized (Simulador.pauseLock) {
    // while (Simulador.isPaused) {
    // try {
    // Simulador.pauseLock.wait();
    // } catch (InterruptedException e) {
    // if (worker.isCancelled()) {
    // break;
    // }
    // }
    // }
    // }

    // try {
    // Thread.sleep(500); // 0.5 segundos por ciclo
    // } catch (InterruptedException e) {
    // if (worker.isCancelled()) {
    // break;
    // }
    // }
    // }

    // // Última atualização pós-fim
    // visualizer.updateDecoded(new ArrayList<>());
    // visualizer.updateUF(unidadesFuncionais);
    // }
}