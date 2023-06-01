package com.chpp.core;

import com.chpp.intermediate_code.ThreeAddressCode;

public class L_FArgs extends BaseNode {

    private FnArg arg;
    private L_FArgs nextArg;

    public L_FArgs(FnArg arg, L_FArgs nextArg, int line, int column) {
        super(line, column);
        this.arg = arg;
        this.nextArg = nextArg;
    }

    public FnArg getArg() {
        return arg;
    }

    public void setArg(FnArg arg) {
        this.arg = arg;
    }

    public L_FArgs getNextArg() {
        return nextArg;
    }

    public void setNextArg(L_FArgs nextArg) {
        this.nextArg = nextArg;
    }

    @Override
    public String toString() {
        return "L_FArgs [arg=" + arg + ", nextArg=" + nextArg + " line=" + line + " column=" + column + "]";

    }

    @Override
    public void generate3dc(ThreeAddressCode codigoTresDir) {
        this.arg.generate3dc(codigoTresDir);
        if (this.nextArg != null) {
            this.nextArg.generate3dc(codigoTresDir);
        }
    }

}
