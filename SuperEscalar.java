import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;
import javax.swing.SwingWorker;

public class SuperEscalar {

    private final int blockSize;
    ArrayList<ArrayList<Instruction>> instructionsCerto = new ArrayList<>();
    private final Contexto[] contextos;

    public SuperEscalar(int blockSize, int contextosLeng) {
        this.contextos = new Contexto[contextosLeng];

        String instruction;
        ArrayList<Instruction> instructions = new ArrayList<>();
        this.blockSize = blockSize;
        try {
            for (int i = 0; i < contextosLeng; i++) {
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
                }
                contextos[i] = new Contexto(i, instructions);
                instructions.clear();
                randomAccessFile.close();
            }
            instructionsCerto = calculateThreadSequenceNotSMT(contextos);
        } catch (IOException e) {
            System.out.println("Exception ao ler arquivos de thread.");
            e.printStackTrace();
        }
    }

    /**
     * Executa o pipeline ciclo a ciclo atualizando o visualizador.
     * 
     * @param visualizer O visualizador para atualizar o estado do pipeline
     * @param worker     O SwingWorker que está executando a simulação
     */
    public void runPipeline(SimplePipelineVisualizer visualizer, SwingWorker<?, ?> worker) {
        for (int i = 0; i < instructionsCerto.size(); i++) {
            visualizer.updateUfCerto(instructionsCerto.get(i), i + 1);
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
    }

    public ArrayList<ArrayList<Instruction>> calculateThreadSequenceNotSMT(Contexto[] threads) {
        int numberOfThreads = threads.length;
        // Instruções finais por ciclo
        ArrayList<ArrayList<Instruction>> instructions = new ArrayList<>();
        @SuppressWarnings("unchecked")
        HashMap<String, Instruction[]>[] allQueues = new HashMap[numberOfThreads];
        // Lendo as threads
        for (int threadNumber = 0; threadNumber < numberOfThreads; threadNumber++) {
            // Queue de cada UF
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
                if (instruction.needsToBeAfterId == -1) { // Não tem dependência verdadeira
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
        while (!allFinished(finished)) {
            for (int j = 0; j < numberOfThreads; j++) {
                if (finished[j]) {
                    continue;
                }
                // Mais de um ciclo?
                boolean needRepeatMEM = false;
                boolean needRepeatJMP = false;
                
                for (int k = 0; k < blockSize || needRepeatMEM || needRepeatJMP; k++) {
                    ArrayList<Instruction> currentInstructions = new ArrayList<>();
                    Instruction[] currentQueue = allQueues[j].get("ALU1");
                    if(currentQueue.length<=ponteiros[j]){
                        break;
                    }
                    if (currentQueue[ponteiros[j]] != null) {
                        currentInstructions.add(currentQueue[ponteiros[j]]);
                    } else {
                        currentInstructions.add(new Instruction("BUB", "0", "0", "0", j));
                    }
                    currentQueue = allQueues[j].get("ALU2");
                    if (currentQueue[ponteiros[j]] != null) {
                        currentInstructions.add(currentQueue[ponteiros[j]]);
                    } else {
                        currentInstructions.add(new Instruction("BUB", "0", "0", "0", j));
                    }
                    currentQueue = allQueues[j].get("MEM");
                    if (currentQueue[ponteiros[j]] != null) {
                        needRepeatMEM = !needRepeatMEM;
                        currentInstructions.add(currentQueue[ponteiros[j]]);
                    } else {
                        currentInstructions.add(new Instruction("BUB", "0", "0", "0", j));
                    }
                    currentQueue = allQueues[j].get("JMP");
                    if (currentQueue[ponteiros[j]] != null) {
                        needRepeatJMP = !needRepeatJMP;
                        currentInstructions.add(currentQueue[ponteiros[j]]);
                    } else {
                        currentInstructions.add(new Instruction("BUB", "0", "0", "0", j));
                    }
                    if (currentInstructions.get(0).inst.equals("BUB") && currentInstructions.get(1).inst.equals("BUB")
                            && currentInstructions.get(2).inst.equals("BUB")
                            && currentInstructions.get(3).inst.equals("BUB")) {
                        finished[j] = true;
                    } else {
                        instructions.add(currentInstructions);
                    }
                    System.out.println("Thread " + j + ": " + currentInstructions.toString());
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
}