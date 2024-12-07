import java.util.ArrayList;

public class Contexto {

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
