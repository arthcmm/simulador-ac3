public class Instruction {
    String codigo;
    String dest;
    String op1;
    String op2;
    int contexto;
    int ciclo = 1;

    Instruction(String inst, String dest, String op1, String op2, int contexto) {
        this.codigo = inst;
        this.dest = dest;
        this.op1 = op1;
        this.op2 = op2;
        this.contexto = contexto;
    }

    Instruction() {
        this.codigo = "VAZIO";
        this.dest = "0";
        this.op1 = "0";
        this.op2 = "0";
        this.contexto = 0;
    }

    Instruction(int contexto) {
        this.codigo = "VAZIO";
        this.dest = "0";
        this.op1 = "0";
        this.op2 = "0";
        this.contexto = contexto;
    }

}