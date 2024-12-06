import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.*;
import java.awt.*;

class Instruction {
    String inst;
    String dest;
    String op1;
    String op2;
    int contexto;

    Instruction(String inst, String dest, String op1, String op2, int contexto) {
        this.inst = inst;
        this.dest = dest;
        this.op1 = op1;
        this.op2 = op2;
        this.contexto = contexto;
    }

}

class Contexto {

    int id;
    ArrayList<Instruction> instructions = new ArrayList<>();
    int qtdInstrucoes;

    @SuppressWarnings("unchecked")
    Contexto(int id, ArrayList<Instruction> instructions) {
        this.id = id;
        this.instructions = (ArrayList<Instruction>) instructions.clone();
        qtdInstrucoes = instructions.size();
    }

}

class Escalar {

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
        // System.out.println("| IF | | ID | | EX | | MEM | | WB |");
        // System.out.println("===============================");
        // for (int i = 0; i < totalInstructions + 5; i++) {
        // if (i <= totalInstructions) {
        // for (int j = pointer; j >= window; j--) {
        // if (j < totalInstructions && j >= 0) {
        // if (pipeline.get(j).contexto == 0) {
        // System.out.print(ANSI_RED + "| " + pipeline.get(j).inst + " |" + ANSI_RESET);
        // } else {
        // System.out.print(ANSI_GREEN + "| " + pipeline.get(j).inst + " |" +
        // ANSI_RESET);

        // }
        // }
        // }
        // for (int j = nop - 1; j >= 0; j--) {
        // System.out.print("| NOP |");
        // }
        // pointer++;
        // window++;
        // nop--;
        // } else {
        // for (int j = pointer; j >= totalInstructions; j--) {
        // System.out.print("| NOP |");
        // }
        // for (int j = pointer - contador++; j >= window; j--) {
        // if (j < totalInstructions) {
        // if (pipeline.get(j).contexto == 0) {

        // System.out.print(ANSI_RED + "| " + pipeline.get(j).inst + " |" + ANSI_RESET);
        // } else {
        // System.out.print(ANSI_GREEN + "| " + pipeline.get(j).inst + " |" +
        // ANSI_RESET);

        // }
        // }
        // }
        // pointer++;
        // window++;
        // }

        // System.out.println(" CICLO: " + i);
        // }
        for (int i = 0; i < pipeline.size() + 5; i++) {
            pointer = i;
            for (int j = 0; j < 5; j++) {
                int currentIndex = pointer - j;
                if (currentIndex >= 0 && currentIndex < pipeline.size()) {
                    Instruction instr = pipeline.get(currentIndex);
                    String color = (instr.contexto == 0) ? ANSI_RED : ANSI_GREEN;
                    System.out.print(color + "| " + instr.inst + " |" + ANSI_RESET);
                } else {
                    System.out.print("| NOP |");

                }

            }
            System.out.println();
        }

    }

}

class PipelineViewer extends JFrame {

    private JPanel pipelinePanel;
    private ArrayList<JLabel> stageLabels;

    public PipelineViewer() {
        setTitle("Visualização do Pipeline");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Painel principal para os estágios do pipeline
        pipelinePanel = new JPanel();
        pipelinePanel.setLayout(new GridLayout(1, 5, 10, 10)); // 5 estágios: IF, ID, EX, MEM, WB
        stageLabels = new ArrayList<>();

        // Adicionando labels para os estágios
        String[] stages = { "IF", "ID", "EX", "MEM", "WB" };
        for (String stage : stages) {
            JLabel label = new JLabel(stage, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(Color.LIGHT_GRAY);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            pipelinePanel.add(label);
            stageLabels.add(label);
        }

        // Painel para a linha de estágios
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridLayout(1, 5)); // 5 estágios: IF, ID, EX, MEM, WB
        for (String stage : stages) {
            JLabel headerLabel = new JLabel(stage, SwingConstants.CENTER);
            headerLabel.setOpaque(true);
            headerLabel.setBackground(Color.GRAY);
            headerLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            headerPanel.add(headerLabel);
        }

        // Adiciona o painel de cabeçalho e o painel principal ao JFrame
        add(headerPanel, BorderLayout.NORTH); // Linha dos estágios fica no topo
        add(pipelinePanel, BorderLayout.CENTER); // Estágios do pipeline ficam no centro

        setVisible(true);
    }

    private Color generateColor(int index) {
        int r = (index * 50) % 256;  // Vermelho (ajusta o valor)
        int g = (index * 100) % 256; // Verde
        int b = (index * 150) % 256; // Azul
        return new Color(r, g, b);
    }

    public void updatePipeline(ArrayList<Instruction> pipeline, int cycle) {

        int pointer = cycle; // Posição atual no ciclo

        // Verifica se o pipeline está vazio
        if (pipeline.isEmpty()) {
            for (JLabel label : stageLabels) {
                label.setText("PIPELINE VAZIO");
                label.setBackground(Color.GRAY);
            }
            return;
        }

        // Atualiza cada estágio da pipeline
        for (int i = 0; i < stageLabels.size(); i++) {
            int currentIndex = pointer - i;

            if (currentIndex >= 0 && currentIndex < pipeline.size()) {
                Instruction instr = pipeline.get(currentIndex);

                // Define o texto e a cor de acordo com o contexto
                stageLabels.get(i).setText(instr.inst + " (Ctx " + instr.contexto + ")");
                stageLabels.get(i).setBackground(generateColor(instr.contexto + 1));
                // stageLabels.get(i).setBackground(instr.contexto == 0 ? Color.RED : Color.GREEN);
                if (instr.inst.compareTo("BUB") == 0) {
                    stageLabels.get(i).setBackground(Color.GRAY);
                }
            } else {
                // Para ciclos fora do alcance, exibe "NOP"
                stageLabels.get(i).setText("NOP");
                stageLabels.get(i).setBackground(Color.LIGHT_GRAY);
            }
        }

        // Atualiza o título da janela com o ciclo atual
        if (cycle < pipeline.size() + 5) {
            setTitle("Pipeline - Ciclo " + cycle);
        }
    }
}

class Simulador {

    public static void main(String[] args) {
        Escalar escalar = new Escalar();
        escalar.createBMTPipeline();
        escalar.printPipeline(1);

        escalar.createIMTPipeline();
        // escalar.printPipeline(0);

        // escalar.createBMTPipeline(); // Cria o pipeline BMT

        // Inicializando a interface gráfica
        PipelineViewer viewer = new PipelineViewer();

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