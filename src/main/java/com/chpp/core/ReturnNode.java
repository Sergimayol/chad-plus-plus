package com.chpp.core;

import com.chpp.intermediate_code.Instruction;
import com.chpp.intermediate_code.Operator;
import com.chpp.intermediate_code.ThreeAddressCode;

public class ReturnNode extends BaseNode {
    private Expresion expresion;

    public ReturnNode(Expresion expresion, int line, int column) {
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
        return "ReturnNode [expresion=" + expresion + " line=" + line + " column=" + column + "]";

    }

    @Override
    public void generate3dc(ThreeAddressCode codigoTresDir) {
        expresion.generate3dc(codigoTresDir);
        String var = codigoTresDir.getLastVariable().getId();

        codigoTresDir.addInstr(new Instruction(var, null, Operator.RETURN, null));

    }

}
