package com.chpp.core;

import com.chpp.intermediate_code.Instruction;
import com.chpp.intermediate_code.Operator;
import com.chpp.intermediate_code.ThreeAddressCode;
import com.chpp.intermediate_code.Variable;

public class L_Args extends BaseNode {
    private Expresion arg;
    private L_Args nextArg;

    public L_Args(Expresion arg, L_Args nextArg, int line, int column) {
        super(line, column);
        this.arg = arg;
        this.nextArg = nextArg;
    }

    public L_Args(Expresion arg, int line, int column) {
        super(line, column);
        this.arg = arg;
    }

    public Expresion getArg() {
        return arg;
    }

    public void setArg(Expresion arg) {
        this.arg = arg;
    }

    public L_Args getNextArg() {
        return nextArg;
    }

    public void setNextArg(L_Args nextArg) {
        this.nextArg = nextArg;
    }

    @Override
    public String toString() {
        return "L_Args [arg=" + arg + ", nextArg=" + nextArg + " line=" + line + " column=" + column + "]";

    }

    @Override
    public void generate3dc(ThreeAddressCode codigoTresDir) {
        // En caso que se este llamando a una funcion tenemos que añadir los argumentos
        // como params
        this.arg.generate3dc(codigoTresDir);
        Variable expresionVar = codigoTresDir.getLastVariable();
        if (codigoTresDir.isCallingFn()) {
            codigoTresDir.addInstr(new Instruction(null, expresionVar.getId(), Operator.PARAM, null));
        } else {
            codigoTresDir.addArg(expresionVar);
        }

        if (this.nextArg != null) {
            this.nextArg.generate3dc(codigoTresDir);
        }

    }

}
