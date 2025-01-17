package com.chpp.core;

import com.chpp.intermediate_code.ThreeAddressCode;

public class Value {
    private String currentInstance;
    private Expresion expresion;
    private Number number;
    private Bol bol;
    private Tuple tuple;
    private Id id;
    private CallFn callFn;
    private A_Tuple aTuple;
    private Input input;

    public Value(String instance, String value, int line, int column) {
        switch (instance) {
            case "Number":
                this.currentInstance = instance;
                this.number = new Number(value, line, column);
                break;
            case "Bol":
                this.currentInstance = instance;
                this.bol = new Bol(value, line, column);
                break;
            case "Id":
                this.currentInstance = instance;
                this.id = new Id(value, line, column);
                break;
            default:
                this.currentInstance = "error";

                // return "not valid";
        }
    }

    public Value(A_Tuple aTuple) {
        this.currentInstance = "A_Tuple";
        this.aTuple = aTuple;
    }

    public Value(Input input) {
        this.currentInstance = "Input";
        this.input = input;
    }

    public Value(Expresion expresion) {
        this.expresion = expresion;
        this.currentInstance = "Expresion";
    }

    public Value(Number number) {
        this.number = number;
        this.currentInstance = "Number";
    }

    public Value(Tuple tuple) {
        this.tuple = tuple;
        this.currentInstance = "Tuple";
    }

    public Value(Id id) {
        this.id = id;
        this.currentInstance = "Id";
    }

    public Value(CallFn callFn) {
        this.callFn = callFn;
        this.currentInstance = "CallFn";
    }

    public String getCurrentInstance() {
        return currentInstance;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    public Number getNumber() {
        return number;
    }

    public Bol getBol() {
        return bol;
    }

    public Tuple getTuple() {
        return tuple;
    }

    public Id getId() {
        return id;
    }

    public CallFn getCallFn() {
        return callFn;
    }

    public A_Tuple getaTuple() {
        return aTuple;
    }

    public Input getInput() {
        return input;
    }

    public void generate3dc(ThreeAddressCode codigoTresDir) {
        switch (currentInstance) {
            case "Expresion":
                this.expresion.generate3dc(codigoTresDir);
                break;
            case "Number":
                this.number.generate3dc(codigoTresDir);
                break;
            case "Tuple":
                this.tuple.generate3dc(codigoTresDir);
                break;
            case "Bol":
                this.bol.generate3dc(codigoTresDir);
                break;
            case "Id":
                this.id.generate3dc(codigoTresDir);
                break;
            case "CallFn":
                this.callFn.generate3dc(codigoTresDir);
                break;
            case "A_Tuple":
                this.aTuple.generate3dc(codigoTresDir);
                break;
            case "Input":
                this.input.generate3dc(codigoTresDir);
                break;
        }

    }

    @Override
    public String toString() {
        switch (currentInstance) {
            case "Expresion":
                return "Value [expresion=" + expresion + "]";
            case "Number":
                return "Value [number=" + number + "]";
            case "Tuple":
                return "Value [tuple=" + tuple + "]";
            case "Bol":
                return "Value [bol=" + bol + "]";
            case "Id":
                return "Value [id=" + id + "]";
            case "CallFn":
                return "Value [callFn=" + callFn + "]";
            case "A_Tuple":
                return "Value [aTuple=" + aTuple + "]";
            case "Input":
                return "Value [input=" + input + "]";
            default:
                return "not valid";
        }

    }

}
