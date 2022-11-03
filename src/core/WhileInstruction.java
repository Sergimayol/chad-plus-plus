package core;

import java.util.List;

public class WhileInstruction extends Instruction {
    private Expresion expresion;
    private List<Instruction> lisfOfInstructions;

    public WhileInstruction() {
        this.instructionType = TypeInstr.IW;
    }

    public WhileInstruction(Expresion expresion, List<Instruction> lisfOfInstructions) {
        this.expresion = expresion;
        this.lisfOfInstructions = lisfOfInstructions;
    }

    public Expresion getExpresion() {
        return this.expresion;
    }

    public void setExpresion(Expresion expresion) {
        this.expresion = expresion;
    }

    public List<Instruction> getLisfOfInstructions() {
        return this.lisfOfInstructions;
    }

    public void setLisfOfInstructions(List<Instruction> lisfOfInstructions) {
        this.lisfOfInstructions = lisfOfInstructions;
    }

    @Override
    public String toString() {
        return "{ While: " +
                " expresion='" + getExpresion() + "'" +
                ", lisfOfInstructions='" + getLisfOfInstructions() + "'" +
                "}";
    }

}