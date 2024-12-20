public class Instruction {
    String inst;    // Nome da instrução
    String dest;
    String op1;
    String op2;
    int contexto;
    String codigo; // Alias para compatibilidade com visualizadores
    int ciclo = 1; // Ciclo restante para a execução
    int needsToBeAfterId = -1;
    int id = -1;

    // Construtor padrão para instruções vazias
    Instruction() {
        this.codigo = "VAZIO";
        this.inst = "VAZIO";
        this.dest = "0";
        this.op1 = "0";
        this.op2 = "0";
        this.contexto = -1;
    }

    // Construtor com parâmetros
    Instruction(String inst, String dest, String op1, String op2, int contexto) {
        this.inst = inst;
        this.codigo = inst; // Para compatibilidade com visualizadores
        this.dest = dest;
        this.op1 = op1;
        this.op2 = op2;
        this.contexto = contexto;
    }

    @Override
    public String toString() {
        String color = (inst.equals("BUB")) ? "\u001B[31m" : "\u001B[32m";
        return color + "| " + inst + " |" + "\u001B[0m";
    }
}