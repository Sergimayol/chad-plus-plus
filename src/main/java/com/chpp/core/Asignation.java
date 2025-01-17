package com.chpp.core;

import com.chpp.intermediate_code.Instruction;
import com.chpp.intermediate_code.Operator;
import com.chpp.intermediate_code.ThreeAddressCode;
import com.chpp.intermediate_code.Variable;

public class Asignation extends BaseNode {

    private L_Ids lid;
    private Expresion expresion;

    public Asignation(L_Ids id, Expresion expresion, int line, int column) {
        super(line, column);
        this.lid = id;
        this.expresion = expresion;
    }

    public L_Ids getL_Ids() {
        return lid;
    }

    public void setL_Ids(L_Ids lid) {
        this.lid = lid;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    public void setExpresion(Expresion expresion) {
        this.expresion = expresion;
    }

    @Override
    public String toString() {
        return "Asignation [L_Ids=" + lid + ", expresion=" + expresion + " line=" + line + " column=" + column + "]";

    }

    @Override
    public void generate3dc(ThreeAddressCode codigoTresDir) {
        lid.generate3dc(codigoTresDir);
        this.expresion.generate3dc(codigoTresDir);
        // Si estamos asignado una tupla, no tenemos que hacer esto
        if (!expresion.getValue().getCurrentInstance().equals("Tuple")) {
            String expLoc = codigoTresDir.getLastVariable().getId();
            for (Variable var : codigoTresDir.getLids()) {
                codigoTresDir.addInstr(new Instruction(var.getId(), expLoc, Operator.ASSIGN, null));
            }
        } else {
            for (Variable id : codigoTresDir.getLids()) {
                for (int i = 0; i < codigoTresDir.getLargs().size(); i++) {
                    codigoTresDir.addInstr(new Instruction(id.getId(), Integer.toString(i * 2), Operator.INDEXED_ASSIGN,
                            codigoTresDir.getLargs().get(i).getId()));
                }
            }
            codigoTresDir.purgeArgs();
        }

        codigoTresDir.purgeIds();
    }

}
