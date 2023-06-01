package com.chpp.assembly;

import com.chpp.core.TypeVar;
import com.chpp.intermediate_code.Instruction;
import com.chpp.intermediate_code.Procedimiento;
import com.chpp.intermediate_code.ThreeAddressCode;
import com.chpp.intermediate_code.Variable;
import com.chpp.symbol_table.StructureReturnType;
import com.chpp.symbol_table.Symbol;
import com.chpp.symbol_table.SymbolTable;
import com.chpp.utils.Env;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Assembly
 */
public class Assembly {

    private final ThreeAddressCode threeAddressCode;
    private final SymbolTable symbolTable;
    private final ArrayList<String> assemblyCode;
    private boolean isInputInt = false;
    private boolean isInputBoolean = false;
    private boolean isOutput = false;
    private int tupSize = 1;

    public Assembly(ThreeAddressCode threeAddressCode, SymbolTable symbolTable) {
        this.threeAddressCode = threeAddressCode;
        this.symbolTable = symbolTable;
        this.assemblyCode = new ArrayList<>();
    }

    public void generateAssemblyCode() {
        analyzeThreeAddressCode();
        generateAssemblyCodePreamble();
        // 68k assembly code
        assemblyCode();
        generateAssemblyCodePostamble();
    }

    private void analyzeThreeAddressCode() {
        for (Instruction ins : threeAddressCode.getCodigo3Dir()) {
            if (ins.getOperation().name().equals("IN_INT")) {
                this.isInputInt = true;
            }
            if (ins.getOperation().name().equals("IN_BOL")) {
                this.isInputBoolean = true;
            }
            if (ins.getOperation().name().equals("OUT")) {
                this.isOutput = true;
            }
        }
    }

    private void generateAssemblyCodePreamble() {
        // 68k assembly code preamble
        assemblyCode.add("\t\tORG\t$1000");
        generateProgramConstAndVars();
        generateAssemblySubroutinesCode();
        assemblyCode.add("START");
    }

    private void generateAssemblyCodePostamble() {
        // 68k assembly code postamble
        assemblyCode.add("\t\tSIMHALT");
        assemblyCode.add("\t\tEND\tSTART");
    }

    private void generateProgramConstAndVars() {
        // 68k assembly code for program constants and variables
        assemblyCode.add("; -----------------------------------------------------------------------------");
        assemblyCode.add("; PROGRAM CONSTANTS AND VARIABLES");
        assemblyCode.add("; -----------------------------------------------------------------------------");
        assemblyCode.add("CR\tEQU\t$0D");
        assemblyCode.add("LF\tEQU\t$0A");
        this.tupSize = 1;
        calcMaxTupSize();
        this.tupSize *= 2;
        assemblyCode.add("TPSZ\tEQU\t" + tupSize);
        for (Variable var : this.threeAddressCode.getTv()) {
            if (var.getType() != null && var.getType().equals(TypeVar.TUP)) {
                assemblyCode.add(var.getId() + "\tDS.W\tTPSZ");

            } else {
                assemblyCode.add(var.getId() + "\tDS.W\t1");
            }
        }
        assemblyCode.add("\tDS.W\t0");
        assemblyCode.add("; -----------------------------------------------------------------------------");
    }

    private void calcMaxTupSize() {
        for (Variable var : this.threeAddressCode.getTv()) {
            if (var.getType() != null && var.getType().equals(TypeVar.TUP) && !var.isVolatile()) { // Get max tup size
                String[] splitName = var.getId().split("_");
                int access = Integer.parseInt(splitName[splitName.length - 1]);
                String id = splitName[0];
                if (splitName.length != 2) {
                    for (int i = 1; i < splitName.length - 1; i++) {
                        id += "_" + splitName[i];
                    }
                }
                ArrayList<Symbol> contenido = this.symbolTable.searchSymbolAtAccess(access, id).getContent();
                if (contenido != null) {
                    int newTupSize = contenido.size();
                    if (newTupSize > this.tupSize) {
                        this.tupSize = newTupSize;
                    }
                }
            }
        }
    }

    private void assemblyCode() {
        for (Instruction instruction : threeAddressCode.getCodigo3Dir()) {
            generateAssemblyCodeForInstruction(instruction);
        }
    }

    /**
     * Genera el código emsamblador para las subrutinas usadas en el programa. Las
     * subrutinas son:
     * <ul>
     * <li>PRINT_INT</li>
     * <li>PRINT_STRING</li>
     * <li>READ_INT</li>
     * <li>READ_STRING</li>
     * <li>STRING_TO_BOOLEAN</li>
     * <li>BOOLEAN_TO_STRING</li>
     * </ul>
     */
    private void generateAssemblySubroutinesCode() {
        // 68k assembly code for subroutines
        // PRINT_INT
        if (isOutput) {
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("PRINT_INT");
            assemblyCode.add("; PRINTS AN INTEGER");
            assemblyCode.add("; INPUT: D1 - INTEGER TO PRINT");
            assemblyCode.add("; OUTPUT: NONE");
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("\tMOVE.W\tD0,-(A7)\t; SAVE D0");
            assemblyCode.add("\tCLR.L\tD0\t; CLEAR D0");
            assemblyCode.add("\tAND.L\t#$0000FFFF,D1\t; CLEAR HIGH WORD");
            assemblyCode.add("\tMOVE.W\t#3,D0\t; PRINT_INT");
            assemblyCode.add("\tTRAP\t#15\t; PRINT_INT CALL TO OS");
            assemblyCode.add("\tLEA\t.NW_LN,A1\t; NEW LINE");
            assemblyCode.add("\tJSR\tPRINT_STRING\t; PRINT NEW LINE");
            assemblyCode.add("\tMOVE.W\t(A7)+,D0\t; RESTORE D0");
            assemblyCode.add("\tRTS\t; RETURN FROM SUBROUTINE");
            assemblyCode.add(".NW_LN\tDC.B\t' ',0");
            // PRINT_STRING
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("PRINT_STRING");
            assemblyCode.add("; PRINTS A STRING");
            assemblyCode.add("; INPUT: A1 - STRING TO PRINT");
            assemblyCode.add("; OUTPUT: NONE");
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("\tMOVE.W\tD0,-(A7)\t; SAVE D0");
            assemblyCode.add("\tCLR.L\tD0\t; CLEAR D0");
            assemblyCode.add("\tMOVE.W\t#13,D0\t; PRINT_STRING");
            assemblyCode.add("\tTRAP\t#15\t; PRINT_STRING CALL TO OS");
            assemblyCode.add("\tMOVE.W\t(A7)+,D0\t; RESTORE D0");
            assemblyCode.add("\tRTS\t; RETURN FROM SUBROUTINE");
            // BOOLEAN_TO_STRING
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("BOOLEAN_TO_STRING");
            assemblyCode.add("; CONVERTS A BOOLEAN TO A STRING");
            assemblyCode.add("; INPUT: D1 - BOOLEAN TO CONVERT");
            assemblyCode.add("; OUTPUT: A1 - STRING VALUE");
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("\tMOVE.W\tD0,-(A7)\t; SAVE D0");
            assemblyCode.add("\tCLR.L\tD0\t; CLEAR D0");
            assemblyCode.add("\tMOVE.W\tD0,A1\t: CLEAR A1");
            assemblyCode.add("\tCMP\t#0,D1\t; CHECK IF D1 IS 0");
            assemblyCode.add("\tBEQ\t.ISFALSE\t; IF D1 IS 0, GO TO ISFALSE");
            assemblyCode.add("\tLEA\t.VALTRUE,A1\t; LOAD ADDRESS OF TRUE STRING");
            assemblyCode.add("\tJMP\t.END\t; JUMP TO END");
            assemblyCode.add(".ISFALSE");
            assemblyCode.add("\tLEA\t.VALFALSE,A1\t; LOAD ADDRESS OF FALSE STRING");
            assemblyCode.add(".END");
            assemblyCode.add("\tMOVE.W\t(A7)+,D0\t; RESTORE D0");
            assemblyCode.add("\tRTS\t; RETURN FROM SUBROUTINE");
            assemblyCode.add(".VALFALSE\tDC.B\t'false',0");
            assemblyCode.add(".VALTRUE\tDC.B\t'true',0");
            assemblyCode.add("\tDS.W\t0");
        }
        if (this.isInputInt) {
            // READ_INT
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("READ_INT");
            assemblyCode.add("; READS AN INTEGER");
            assemblyCode.add("; INPUT: NONE");
            assemblyCode.add("; OUTPUT: D1 - INTEGER READ");
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("\tMOVE.W\tD0,-(A7)\t; SAVE D0");
            assemblyCode.add("\tCLR.L\tD0\t; CLEAR D0");
            assemblyCode.add("\tCLR.L\tD1\t; CLEAR D1");
            assemblyCode.add("\tMOVE.W\t#4,D0\t; READ_INT");
            assemblyCode.add("\tTRAP\t#15\t; READ_INT CALL TO OS");
            assemblyCode.add("\tMOVE.W\t(A7)+,D0\t; RESTORE D0");
            assemblyCode.add("\tRTS\t; RETURN FROM SUBROUTINE");
        }
        if (this.isInputBoolean) {
            // READ_STRING
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("READ_STRING");
            assemblyCode.add("; READS A STRING");
            assemblyCode.add("; INPUT: NONE");
            assemblyCode.add("; OUTPUT: A1 - STRING READ");
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("\tMOVE.W\tD0,-(A7)\t; SAVE D0");
            assemblyCode.add("\tCLR.L\tD0\t; CLEAR D0");
            assemblyCode.add("\tMOVE.W\tD0, A1\t; CLEAR A1");
            assemblyCode.add("\tMOVE.W\t#2,D0\t; READ_STRING");
            assemblyCode.add("\tTRAP\t#15\t; READ_STRING CALL TO OS");
            assemblyCode.add("\tMOVE.W\t(A7)+,D0\t; RESTORE D0");
            assemblyCode.add("\tRTS\t; RETURN FROM SUBROUTINE");
            // STRING_TO_BOOLEAN
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("STRING_TO_BOOLEAN");
            assemblyCode.add("; CONVERTS A STRING TO A BOOLEAN");
            assemblyCode.add("; ASSERT: INPUT STRING IS:");
            assemblyCode.add("; TRUE/true OR FALSE/false");
            assemblyCode.add("; INPUT: A1 - STRING TO CONVERT");
            assemblyCode.add("; OUTPUT: D1 - BOOLEAN VALUE");
            assemblyCode.add("; -----------------------------------------------------------------------------");
            assemblyCode.add("\tMOVEM.W\tD0/A1,-(A7)\t; SAVE D0/A1");
            assemblyCode.add("\tCLR.L\tD0\t; CLEAR D0");
            assemblyCode.add("\tMOVE.B\t(A1),D0\t: FIRST CHARACTER");
            assemblyCode.add("\tCMP.W\t#'F',D0\t; IS FALSE?");
            assemblyCode.add("\tBEQ\t.STR_F\t; IS F");
            assemblyCode.add("\tCMP.W\t#'f',D0\t; IS FALSE?");
            assemblyCode.add("\tBEQ\t.STR_F\t; IS f");
            assemblyCode.add("\tMOVE.W\t#1,D1"); // true
            assemblyCode.add("\tJMP\t.STR_END\t; END");
            assemblyCode.add(".STR_F"); // false
            assemblyCode.add("\tMOVE.W\t#0,D1");
            assemblyCode.add(".STR_END");
            assemblyCode.add("\tMOVEM.W\t(A7)+,D0/D1\t; SAVE D0/A1");
            assemblyCode.add("\tRTS\t; RETURN FROM SUBROUTINE");
        }
    }

    /**
     * Genera el código emsamblador para una instrucción. Las instrucciones son:
     * <ul>
     * <li>MULT</li>
     * <li>DIV</li>
     * <li>ADD</li>
     * <li>SUB</li>
     * <li>AND</li>
     * <li>OR</li>
     * <li>GOTO</li>
     * <li>SKIP</li>
     * <li>ASSIGN</li>
     * <li>PRINT_INT</li>
     * <li>PARAM</li>
     * <li>CALL</li>
     * <li>RETURN</li>
     * <li>EQUAL</li>
     * <li>NOT_EQUAL</li>
     * <li>LESS</li>
     * <li>LESS_EQUAL</li>
     * <li>GREATER</li>
     * <li>GREATER_EQUAL</li>
     * </ul>
     *
     * @param instruction Instrucción a traducir.
     * @see Instruction
     */
    private void generateAssemblyCodeForInstruction(Instruction instruction) {
        // 68k assembly code for instruction
        switch (instruction.getOperation()) {
            case MULT:
                mult(instruction);
                break;
            case DIV:
                div(instruction);
                break;
            case ADD:
                add(instruction);
                break;
            case SUB:
                sub(instruction);
                break;
            case AND:
                and(instruction);
                break;
            case OR:
                or(instruction);
                break;
            case GOTO:
                assemblyCode.add("\tJMP\t" + instruction.getDest());
                break;
            case SKIP:
                assemblyCode.add(instruction.getDest());
                break;
            case ASSIGN:
                assign(instruction);
                break;
            case PARAM:
                param(instruction);
                break;
            case CALL:
                call(instruction);
                break;
            case RETURN:
                returnSubroutine(instruction);
                break;
            case PMB:
                pmb(instruction);
                break;
            case EQUAL:
            case LESS:
            case GREATER:
                // case NOT_EQUAL:
                // case LESS_EQUAL:
                // case GREATER_EQUAL:
                compare(instruction);
                break;
            case IF:
                ifInstruction(instruction);
                break;
            case INDEXED_ASSIGN:
                indexedAssign(instruction);
                break;
            case INDEXED_VALUE:
                indexedValue(instruction);
                break;
            case IN_BOL:
                input(instruction);
            case IN_INT:
                input(instruction);
                break;
            case OUT:
                output(instruction);
                break;
            case EXIT:
                // simhalt
                break;
            case SHIFTL:
            case SHIFTR:
                shift(instruction);
                break;
            default:
                break;
        }
    }

    private void shift(Instruction ins) {
        assemblyCode.add("\tMOVE.W\t" + ins.getOp1() + ",D0");
        if (ins.getOperation().name().equals("SHIFTR")) {
            assemblyCode.add("\tLSR.W\t#" + ins.getOp2() + ",D0");
        } else {
            assemblyCode.add("\tLSL.W\t#" + ins.getOp2() + ",D0");
        }
        assemblyCode.add("\tMOVE.W\tD0," + ins.getDest());
    }

    private boolean isTup(String v) {
        for (Variable var : this.threeAddressCode.getTv()) {
            if (v.equals(var.getId()) && var.getType().equals(TypeVar.TUP)) {
                return true;
            }
        }
        return false;
    }

    private void input(Instruction ins) {
        Variable dest = this.threeAddressCode.findVarById(ins.getDest());
        if (dest.getType().equals(TypeVar.INT)) {
            assemblyCode.add("\tJSR\tREAD_INT");
            assemblyCode.add("\tMOVE.W\tD1," + dest.getId());
        } else {
            assemblyCode.add("\tJSR\tREAD_STRING");
            assemblyCode.add("\tMOVEA.L\tA1," + dest.getId());
        }
    }

    private void output(Instruction ins) {
        // Dependiendo del tipo (INT o BOL) se llama a printInt o printBol
        // String A1 | Int D1
        if (isTup(ins.getOp1())) {
            // TODO:
            // - Mirar como hacer para printear los elementos de una tupla bool como str y
            // no como int
            // - Mirar de no printear los huecos vacios de la tupla reservada
            for (int i = 0; i < this.tupSize; i++) {
                assemblyCode.add("\tMOVE.W\t(" + ins.getOp1() + "+" + (i * 2) + "),D1");
                assemblyCode.add("\tJSR\tPRINT_INT");
            }
        } else {
            Variable var = this.threeAddressCode.findVarById(ins.getOp1());
            if (var == null) {
                if (!ins.getOp1().equals("false") && !ins.getOp1().equals("true")) {
                    assemblyCode.add("\tMOVE.W\t#" + ins.getOp1() + ",D1");
                    assemblyCode.add("\tJSR\tPRINT_INT");
                } else {
                    String out = ins.getOp1().equals("true") ? "#1" : "#0";
                    assemblyCode.add("\tMOVE.W\t" + out + ",D1");
                    assemblyCode.add("\tJSR\tBOOLEAN_TO_STRING");
                    assemblyCode.add("\tJSR\tPRINT_STRING");
                }
            } else {
                if (var.getType().equals(TypeVar.INT)) {
                    assemblyCode.add("\tMOVE.W\t" + ins.getOp1() + ",D1");
                    assemblyCode.add("\tJSR\tPRINT_INT");
                } else {
                    assemblyCode.add("\tMOVE.W\t" + ins.getOp1() + ",D1");
                    assemblyCode.add("\tJSR\tBOOLEAN_TO_STRING");
                    assemblyCode.add("\tJSR\tPRINT_STRING");
                }
            }
        }

    }

    private void indexedValue(Instruction ins) {
        // Miramos de parsear el operando 2 a un numero, si lo es podemos directamente
        // ponerlo en la instrucción. en caso contrario tenemos que hacer la gestion
        // para hacer el acceso a la tupla mediante el uso de registros de direccion
        try {
            Integer.parseInt(ins.getOp2());
        } catch (Exception e) {
            assemblyCode.add("\tMOVE.W\t" + ins.getOp2() + ",D0");
            assemblyCode.add("\tLEA\t" + ins.getOp1() + ",A1");
            assemblyCode.add("\tADDA.L\tD0,A1");
            assemblyCode.add("\tMOVE.W\t(A1)," + ins.getDest());
            return;
        }
        assemblyCode.add("\tMOVE.W\t" + "(" + ins.getOp1() + "+" + ins.getOp2() + ")," + ins.getDest());
    }

    private void indexedAssign(Instruction ins) {
        String var = ins.getOp2();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tMOVE.W\t" + var + ",(" + ins.getDest() + "+" + ins.getOp1() + ")");
    }

    private void ifInstruction(Instruction ins) {
        // Mirar que el op1 sea true, si lo es saltar a dest
        if (ins.getOp1().equals("false") || ins.getOp1().equals("true")) {
            String out = ins.getOp1().equals("true") ? "#1" : "#0";
            assemblyCode.add("\tMOVE.W\t" + out + ",D1");
            assemblyCode.add("\tCMP.W\t" + "#1, D1");
        } else {
            assemblyCode.add("\tCMP.W\t" + "#1, " + ins.getOp1());
        }
        assemblyCode.add("\tBEQ\t" + ins.getDest());
    }

    private void add(Instruction ins) {
        String var = ins.getOp1();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tMOVE.W\t" + var + ",D0");
        var = ins.getOp2();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tADD.W\t" + var + ",D0");
        assemblyCode.add("\tMOVE.W\tD0," + ins.getDest());
    }

    private void sub(Instruction ins) {
        String var = ins.getOp1();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tMOVE.W\t" + var + ",D0");
        var = ins.getOp2();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tSUB.W\t" + var + ",D0");
        assemblyCode.add("\tMOVE.W\tD0," + ins.getDest());
    }

    private void mult(Instruction ins) {
        String var = ins.getOp1();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tMOVE.W\t" + var + ",D0");
        assemblyCode.add("\tEXT.L\tD0");
        var = ins.getOp2();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tMOVE.W\t" + var + ",D1");
        assemblyCode.add("\tEXT.L\tD1");
        assemblyCode.add("\tMULS.W\tD1,D0");
        assemblyCode.add("\tMOVE.W\tD0," + ins.getDest());
    }

    private void div(Instruction ins) {
        String var = ins.getOp1();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tMOVE.W\t" + var + ",D0");
        assemblyCode.add("\tEXT.L\tD0");
        var = ins.getOp2();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tMOVE.W\t" + var + ",D1");
        assemblyCode.add("\tEXT.L\tD1");
        assemblyCode.add("\tDIVS.W\tD1,D0");
        assemblyCode.add("\tMOVE.W\tD0," + ins.getDest());
    }

    private void and(Instruction ins) {
        String var = ins.getOp1();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tMOVE.W\t" + var + ",D0");
        var = ins.getOp2();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tAND.W\t" + var + ",D0");
        assemblyCode.add("\tMOVE.W\tD0," + ins.getDest());
    }

    private void or(Instruction ins) {
        String var = ins.getOp1();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tMOVE.W\t" + var + ",D0");
        var = ins.getOp2();
        if (this.threeAddressCode.findVarById(var) == null) {
            var = "#" + var;
        }
        assemblyCode.add("\tOR.W\t" + var + ",D0");
        assemblyCode.add("\tMOVE.W\tD0," + ins.getDest());
    }

    private void assign(Instruction ins) {
        String var1 = ins.getOp1();
        if (var1.equals("false")) {
            var1 = "0";
        } else {
            if (var1.equals("true")) {
                var1 = "1";
            }
        }
        if (this.threeAddressCode.findVarById(ins.getOp1()) == null) {
            var1 = "#" + var1;
        }
        String dest = ins.getDest();
        if (isTup(var1) && isTup(dest)) {
            for (int i = 0; i < this.tupSize; i++) {
                String op1 = "(" + var1 + "+" + (i * 2) + "),";
                String op2 = "(" + dest + "+" + (i * 2) + ")";
                assemblyCode.add("\tMOVE.W\t" + op1 + op2);
            }
        } else {
            assemblyCode.add("\tMOVE.W\t" + var1 + "," + ins.getDest());
        }
    }

    private void param(Instruction ins) {
        // int and bool = .W
        TypeVar tp = null;
        for (Variable var : this.threeAddressCode.getTv()) {
            if (ins.getOp1().equals(var.getId())) {
                tp = var.getType();
            }
        }
        if (tp == null) {
            assemblyCode.add("\tMOVE.W\t#" + ins.getOp1() + ",-(A7)");
        } else {
            if (tp.equals(TypeVar.TUP)) {
                for (int i = 0; i < this.tupSize; i++) {
                    assemblyCode.add("\tMOVE.W\t(" + ins.getOp1() + "+" + (i * 2) + "),-(A7)");
                }
            } else {
                assemblyCode.add("\tMOVE.W\t" + ins.getOp1() + ",-(A7)");
            }
        }
    }

    private void call(Instruction ins) {
        // Si tiene valores de retorno guardar espacio
        Procedimiento prod = this.threeAddressCode.getProcedimiento(ins.getOp1());
        assert prod != null;
        if (prod.getReturnType() != StructureReturnType.VOID) {
            if (prod.getReturnType().equals(StructureReturnType.TUP)) {
                assemblyCode.add("\tSUBA.L\t#TPSZ,A7"); // RTS val
            } else {
                assemblyCode.add("\tSUBA.L\t#2,A7"); // RTS val
            }
        }
        // Llamar subrutina
        assemblyCode.add("\tJSR\trun_" + ins.getOp1());
        // Si tiene valores de retorno recuperarlo y guardarlo donde toca
        if (prod.getReturnType() != StructureReturnType.VOID) {
            if (prod.getReturnType().equals(StructureReturnType.TUP)) {
                for (int i = 0; i < this.tupSize; i++) {
                    assemblyCode.add("\tMOVE.W\t(A7)+,(" + ins.getDest() + "+" + (i * 2) + ")");
                }
            } else {
                assemblyCode.add("\tMOVE.W\t(A7)+," + ins.getDest());
            }
        }
        /*
         * // Vaciar pila dependiendo del núm de params
         * int paramsSize = prod.getParameters().size();
         * for (Variable param : prod.getParameters()) {
         * if (param.getType().equals(TypeVar.TUP)) {
         * paramsSize -= 1;
         * }
         * }
         * if (paramsSize > 0) {
         * assemblyCode.add("\tADDA.L\t#" + paramsSize * 2 + ",A7");
         * }
         */
    }

    private void returnSubroutine(Instruction ins) {
        String dest = ins.getDest();
        if (dest != null) {
            if (isTup(dest)) {
                for (int i = 0; i < this.tupSize; i++) {
                    String op1 = "(" + dest + "+" + (i * 2) + "),";
                    String op2 = ((i * 2) + 4) + "(A7)";
                    assemblyCode.add("\tMOVE.W\t" + op1 + op2);
                }
            } else {
                assemblyCode.add("\tMOVE.W\t" + dest + ",4(A7)");
            }
        }
        assemblyCode.add("\tRTS");
    }

    private void pmb(Instruction ins) {
        String[] splitProdName = ins.getDest().split("_");
        String prodName = splitProdName[1];
        if (splitProdName.length != 2) {
            for (int i = 2; i < splitProdName.length; i++) {
                prodName += "_" + splitProdName[i];
            }
        }
        Procedimiento prod = this.threeAddressCode.getProcedimiento(prodName);
        assert prod != null;
        ArrayList<Variable> params = prod.getParameters();
        // Save rts dir
        assemblyCode.add("\tMOVE.L\t(A7)+,D7");
        if (prod.getReturnType() != StructureReturnType.VOID) {
            if (prod.getReturnType().equals(StructureReturnType.TUP)) {
                assemblyCode.add("\tADDA.L\t#TPSZ,A7");
            } else {
                assemblyCode.add("\tMOVE.W\t(A7)+,D6");
            }
        }
        if (params != null) {
            Collections.reverse(params);
            for (Variable param : params) {
                TypeVar tp = null;
                for (Variable var : this.threeAddressCode.getTv()) {
                    if (param.getId().equals(var.getId())) {
                        tp = var.getType();
                    }
                }
                assert tp != null;
                if (tp.equals(TypeVar.TUP)) {
                    for (int i = 0; i < this.tupSize; i++) {
                        assemblyCode.add("\tMOVE.W\t(A7)+,(" + param.getId() + "+" + (i * 2) + ")");
                    }
                } else {
                    assemblyCode.add("\tMOVE.W\t(A7)+," + param.getId());
                }
            }
            Collections.reverse(params);
            if (prod.getReturnType() != StructureReturnType.VOID) {
                if (prod.getReturnType().equals(StructureReturnType.TUP)) {
                    assemblyCode.add("\tSUBA.L\t#TPSZ,A7");
                } else {
                    assemblyCode.add("\tMOVE.W\tD6,-(A7)");
                }
            }
            assemblyCode.add("\tMOVE.L\tD7,-(A7)");
        }
    }

    private void compare(Instruction ins) {
        String var1 = ins.getOp1();
        if (this.threeAddressCode.findVarById(var1) != null) {
            var1 = "(" + var1 + ")";
        } else {
            var1 = "#" + var1;
        }
        String var2 = ins.getOp2();
        if (this.threeAddressCode.findVarById(var2) != null) {
            var2 = "(" + var2 + ")";
        } else {
            var2 = "#" + var2;
        }

        assemblyCode.add("\tMOVE.W\t" + var1 + ",D0");
        assemblyCode.add("\tMOVE.W\t" + var2 + ",D1");

        assemblyCode.add("\tCMP.W\tD1,D0");
        switch (ins.getOperation()) {
            case EQUAL:
                assemblyCode.add("\tSEQ\tD0");
                assemblyCode.add("\tAND.W\t #1, D0");
                assemblyCode.add("\tMOVE.W D0, " + ins.getDest());
                break;
            case LESS:
                assemblyCode.add("\tSLT\tD0");
                assemblyCode.add("\tAND.W\t #1, D0");
                assemblyCode.add("\tMOVE.W D0, " + ins.getDest());
                break;
            case GREATER:
                assemblyCode.add("\tSGT\tD0");
                assemblyCode.add("\tAND.W\t #1, D0");
                assemblyCode.add("\tMOVE.W D0, " + ins.getDest());
                break;
            case IF:
                assemblyCode.add("\tBEQ\t" + ins.getDest());
                break;
            default: // Nunca va llegar, pero es para evitar warnings
                break;
        }
    }

    public void saveAssemblyCode(String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Env.GENERATED_FILES + Env.SLASH + fileName));
            for (String line : assemblyCode) {
                writer.write(line + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
