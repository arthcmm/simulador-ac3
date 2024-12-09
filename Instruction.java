public class Instruction {
    String inst;    // no código original era 'inst', aqui utilizaremos 'inst' e 'codigo' como sinônimos
    String dest;
    String op1;
    String op2;
    int contexto;
    String codigo; // adicione esse alias se necessário, ou use inst = codigo
    int ciclo = 1;

    Instruction() {
        this.codigo = "VAZIO";
        this.inst = "VAZIO";
        this.contexto = -1;
    }

    Instruction(String inst, String dest, String op1, String op2, int contexto) {
        this.inst = inst;
        this.codigo = inst; // para compatibilidade com SimplePipelineVisualizer
        this.dest = dest;
        this.op1 = op1;
        this.op2 = op2;
        this.contexto = contexto;
    }
}