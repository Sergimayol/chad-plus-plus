package com.chpp.core;

import com.chpp.intermediate_code.Instruction;
import com.chpp.intermediate_code.Operator;
import com.chpp.intermediate_code.ThreeAddressCode;
import com.chpp.intermediate_code.Variable;

public class Output extends BaseNode {
    private Expresion expresion;

    public Output(Expresion expresion, int line, int column) {
        super(line, column);
        this.expresion = expresion;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    public void setExpresion(Expresion expresion) {
        this.expresion = expresion;
    }

    @Override
    public String toString() {
        return "Output [expresion=" + expresion + " line=" + line + " column=" + column + "]";

    }

    @Override
    public void generate3dc(ThreeAddressCode codigoTresDir) {
        this.expresion.generate3dc(codigoTresDir);
        if (!codigoTresDir.getLargs().isEmpty()) {
            for (Variable var : codigoTresDir.getLargs()) {
                codigoTresDir.addInstr(new Instruction(null, var.getId(), Operator.OUT, null));
            }
        } else {
            codigoTresDir.addInstr(new Instruction(null, codigoTresDir.getLastVariable().getId(), Operator.OUT, null));
        }
        codigoTresDir.purgeArgs();

    }

}
