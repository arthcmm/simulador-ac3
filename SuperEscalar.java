import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;

public class SuperEscalar {

    Contexto[] contextos = new Contexto[2];
    int totalInstructions = 0;
    int blockSize = 3;
    int totalCiclos = 0;
    public HashMap<String, Instruction> unidadesFuncionais = new HashMap<>();
    ArrayList<Instruction> imtPipeline = new ArrayList<>();

    SuperEscalar() {
        String instruction;
        ArrayList<Instruction> instructions = new ArrayList<>();

        try {
            for (int i = 0; i < contextos.length; i++) {
                RandomAccessFile randomAccessFile = new RandomAccessFile("thread" + (i) + ".txt", "r");
                int count = 0;
                while ((instruction = randomAccessFile.readLine()) != null) {
                    String[] operands = instruction.split(" ");
                    instructions.add(
                            new Instruction(operands[0], operands[1], operands[2], operands[3], i));
                    if (operands[0].compareTo("LDW") == 0 || operands[0].compareTo("JMP") == 0) {
                        instructions.get(count).ciclo = 2;
                    } else {
                        instructions.get(count).ciclo = 1;
                    }

                    totalCiclos += instructions.get(count).ciclo;
                    count++;
                }
                // for (var in : instructions) {
                // System.out.print(in.ciclo + " ");
                // }
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
        System.out.println(totalInstructions);
        for (int i = 0; i < totalInstructions; i++) {
            int contextoAtual = i % contextos.length;
            for (int j = 0; j < 4; j++) {
                if (ponteiros[contextoAtual] < contextos[contextoAtual].qtdInstrucoes) {
                    imtPipeline.add(contextos[contextoAtual].instructions.get(ponteiros[contextoAtual]));
                    ponteiros[contextoAtual]++;
                }
            }
        }

        for (var i : imtPipeline) {
            System.out.print(i.codigo + " ");

        }
        System.out.println();
    }

    public void printPipeline() {
        Stack<Instruction> restantes = new Stack<>();
        ArrayList<Instruction> estacaoReserva = new ArrayList<>();

        for (int i = 0; !canStopPipeline(); i++) {

            int contextoAtual = i % contextos.length;
            System.out.print("DECODIFICADO: ");
            for (int j = 0; j < 4; j++) {
                if (!contextos[contextoAtual].instructions.isEmpty()) {
                    Instruction instruction = contextos[contextoAtual].instructions.get(0);
                    System.out.print(instruction.codigo + " ");
                    if (!processInstruction(instruction)) {
                        restantes.add(instruction); // Adiciona à fila de espera
                        estacaoReserva.add(instruction);
                    }
                    contextos[contextoAtual].instructions.remove(0); // Remove do estágio IF
                }

            }
            System.out.println();


            while (!restantes.isEmpty()) {
                Instruction inst = restantes.peek();
                contextos[contextoAtual].instructions.addFirst(inst);
                restantes.pop();
            }

            printUF();

            limparUF();

        }
        printUF();

        limparUF();
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

        if (codigo.equals("ADD")) {
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

    public void limparUF() {
        int finalizadas = 0;
        unidadesFuncionais.get("ALU1").ciclo = unidadesFuncionais.get("ALU1").ciclo - 1;
        unidadesFuncionais.get("ALU2").ciclo = unidadesFuncionais.get("ALU2").ciclo - 1;
        unidadesFuncionais.get("MEM").ciclo = unidadesFuncionais.get("MEM").ciclo - 1;
        unidadesFuncionais.get("JMP").ciclo = unidadesFuncionais.get("JMP").ciclo - 1;
        if (unidadesFuncionais.get("ALU1").ciclo == 0) {
            if (unidadesFuncionais.get("ALU1").codigo.compareTo("VAZIO") != 0) {
                finalizadas++;
            }
            unidadesFuncionais.put("ALU1", new Instruction());
        }
        if (unidadesFuncionais.get("ALU2").ciclo == 0) {
            if (unidadesFuncionais.get("ALU2").codigo.compareTo("VAZIO") != 0) {
                finalizadas++;
            }
            unidadesFuncionais.put("ALU2", new Instruction());
        }
        if (unidadesFuncionais.get("MEM").ciclo == 0) {
            if (unidadesFuncionais.get("MEM").codigo.compareTo("VAZIO") != 0) {
                finalizadas++;
            }
            unidadesFuncionais.put("MEM", new Instruction());

        }
        if (unidadesFuncionais.get("JMP").ciclo == 0) {
            if (unidadesFuncionais.get("JMP").codigo.compareTo("VAZIO") != 0) {
                finalizadas++;
            }
            unidadesFuncionais.put("JMP", new Instruction());

        }
        System.out.println("Instrucoes por ciclo: " + finalizadas);
    }

    public void printUF() {
        System.out.println("PRINT UF ==================");
        System.out.println("ALU1: " +
                unidadesFuncionais.get("ALU1").codigo + " contexto: " + unidadesFuncionais.get("ALU1").contexto);
        System.out.println("ALU2: " +
                unidadesFuncionais.get("ALU2").codigo + " contexto: " + unidadesFuncionais.get("ALU2").contexto);
        System.out
                .println("MEM: " + unidadesFuncionais.get("MEM").codigo + " contexto: "
                        + unidadesFuncionais.get("MEM").contexto);
        System.out
                .println("JMP: " + unidadesFuncionais.get("JMP").codigo + " contexto: "
                        + unidadesFuncionais.get("JMP").contexto);
        System.out.println("==================");

    }


}
