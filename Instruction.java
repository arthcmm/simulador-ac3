public class Instruction {
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