import java.util.ArrayList;

public class Contexto {

    int id;
    ArrayList<Instruction> instructions = new ArrayList<>();
    int qtdInstrucoes;

    Contexto(int id, ArrayList<Instruction> instructions) {
        this.id = id;
        this.instructions = new ArrayList<>(instructions);
        this.qtdInstrucoes = instructions.size();
    }

}