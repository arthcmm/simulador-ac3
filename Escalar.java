import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Escalar {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    Contexto[] contextos = new Contexto[3];
    ArrayList<Instruction> imtPipeline = new ArrayList<>();
    ArrayList<Instruction> bmtPipeline = new ArrayList<>();
    int totalInstructions = 0;
    int blockSize = 3;

    Escalar() {
        String instruction;
        ArrayList<Instruction> instructions = new ArrayList<>();
        try {
            for (int i = 0; i < contextos.length; i++) {
                RandomAccessFile randomAccessFile = new RandomAccessFile("thread" + (i) + ".txt", "r");
                while ((instruction = randomAccessFile.readLine()) != null) {
                    String[] operands = instruction.split(" ");
                    instructions.add(
                            new Instruction(operands[0], operands[1], operands[2], operands[3], i));
                }

                contextos[i] = new Contexto(i, instructions);
                instructions.clear();

                randomAccessFile.close();
            }

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
        for (int i = 0; i < totalInstructions; i++) {
            int contextoAtual = i % contextos.length;
            if (ponteiros[contextoAtual] < contextos[contextoAtual].qtdInstrucoes) {
                imtPipeline.add(contextos[contextoAtual].instructions.get(ponteiros[contextoAtual]));
                ponteiros[contextoAtual]++;

            }
        }
        for (var i : imtPipeline) {
            System.out.print(i.inst + " ");

        }
        System.out.println();

    }

    public void createBMTPipeline() {
        for (Contexto contexto : contextos) {
            totalInstructions += contexto.qtdInstrucoes;
        }
        int[] ponteiros = new int[contextos.length];
        for (int i = 0; i < ponteiros.length; i++) {
            ponteiros[i] = 0;
        }
        for (int i = 0; i < totalInstructions; i++) {
            int contextoAtual = (i) % contextos.length;
            for (int k = 0; k < blockSize; k++) {
                if (ponteiros[contextoAtual] < contextos[contextoAtual].qtdInstrucoes) {
                    bmtPipeline.add(contextos[contextoAtual].instructions.get(ponteiros[contextoAtual]));
                    ponteiros[contextoAtual]++;
                }

            }
        }
        encontraBolha();
    }

    public void encontraBolha() {

        for (int i = 0; i < bmtPipeline.size(); i++) {
            if (bmtPipeline.get(i).inst.compareTo("LDW") == 0) {
                if (i + 1 < totalInstructions) {
                    if (ehBolha(i)) {
                        Instruction bubble = new Instruction("BUB", "0", "0", "0", bmtPipeline.get(i).contexto);
                        bmtPipeline.add(i + 1, bubble);
                        i += 1;
                    }
                    // bmtPipeline.remove(i+1);
                }
            }
        }
        for (var instruction : bmtPipeline) {
            System.out.print(instruction.inst + " ");
        }
        System.out.println();
    }

    public boolean ehBolha(int i) {
        if (bmtPipeline.get(i + 1).contexto == bmtPipeline.get(i).contexto
                && (bmtPipeline.get(i + 1).op1.compareTo(bmtPipeline.get(i).dest) == 0
                        || bmtPipeline.get(i + 1).op2.compareTo(bmtPipeline.get(i).dest) == 0)) {
            return true;
        }
        return false;
    }

    public void printPipeline(int multithread) {
        ArrayList<Instruction> pipeline;
        if (multithread == 0) {
            pipeline = imtPipeline;
        } else {
            pipeline = bmtPipeline;
        }
        int pointer = -1;
        if (pipeline.size() == 0) {
            System.out.println("PIPELINE VAZIO");
            return;
        }
        for (int i = 0; i < pipeline.size() + 5; i++) {
            pointer = i;
            for (int j = 0; j < 5; j++) {
                int currentIndex = pointer - j;
                if (currentIndex >= 0 && currentIndex < pipeline.size()) {
                    Instruction instr = pipeline.get(currentIndex);
                    String color = (instr.contexto == 0) ? ANSI_RED : ANSI_GREEN;
                    if (instr.inst.equals("BUB")) {
                        color = ANSI_BLACK;
                        System.out.print(color + "| " + instr.inst + "          |" + ANSI_RESET);
                    } else {
                        System.out.print(color + "| " + instr.inst + " " + instr.dest + " " + instr.op1 + " "
                                + instr.op2 + " |" + ANSI_RESET);
                    }
                } else {
                    System.out.print("| NOP          |");

                }

            }
            System.out.println();
        }

    }

}