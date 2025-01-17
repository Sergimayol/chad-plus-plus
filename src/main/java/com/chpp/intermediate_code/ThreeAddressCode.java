package com.chpp.intermediate_code;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.chpp.core.TypeVar;
import com.chpp.symbol_table.StructureReturnType;
import com.chpp.utils.Env;

/**
 * CodigoTresDrirecciones
 */
public class ThreeAddressCode {

  /**
   * Lista de instrucciones de código de 3 direcciones
   *
   * @see Instruction
   */
  private ArrayList<Instruction> codigo3Dir;

  /**
   * Tabla de variables
   *
   * @see Variable
   */
  private ArrayList<Variable> tv;

  /**
   * Subconjunto de la tabla de variables. Contiene todas las declaraciones
   * globales
   *
   * @see Variable
   */
  private ArrayList<Variable> tvg;

  /**
   * Tabla de funciones
   *
   * @see Funcion
   */
  private ArrayList<Procedimiento> tp;

  /**
   * Contador de etiquetas
   */
  private int labelCounter = 0;

  /**
   * Contador de las variables volatiles
   */
  private int volatileCounter = 0;

  /**
   * Permite guardar el tipo de datos en una declaración para la formacion de
   * variables
   */
  private TypeVar declType = null;

  /**
   * Permite identificar si se están añadiendo parametros o declaraciones
   */
  private boolean declaringParams = false;

  /**
   * Se usa para conocer la lista de ids en una asignación
   */
  private ArrayList<Variable> lids;

  /**
   * Permite identificar la lista de argumentos de una asignación o llamada
   */
  private ArrayList<Variable> largs;

  /**
   * Permite identificar si largs es apra una llamada a una función o para una
   * asignación
   */
  private boolean callingFunction = false;

  /**
   * Constructor de la clase, inicializa las variables de la clase.
   *
   * @param tree Árbol sintáctico del código fuente
   */
  public ThreeAddressCode() {
    this.codigo3Dir = new ArrayList<>();
    this.tv = new ArrayList<>();
    this.tvg = new ArrayList<>();
    this.tp = new ArrayList<>();
    this.lids = new ArrayList<>();
    this.largs = new ArrayList<>();

  }

  /**
   * Crea una nueva etiqueta y la devuelve.
   *
   * @return String nueva
   */
  public String newLabel() {
    return "e" + labelCounter++;
  }

  /**
   * Dados los parametros, o crea la variable y te la devuelve o si existe te pasa
   * una referencia de esa variable
   *
   * @param id
   * @param variableType
   * @return Variable
   */
  public Variable putVar(String id, TypeVar variableType) {
    // Mirar si id es null -> variable volatil
    Variable var = null;
    if (id == null) {
      var = new Variable(variableType, "t" + volatileCounter++, true);
    } else {

      // Si tp esta vacio, implica que no hemos entado a ninguna funcion y por lo
      // tanto estamos creando una declaracion global
      if (tp.isEmpty()) {
        var = new Variable(variableType, id + "_" + tp.size(), false);
        tvg.add(var);
      } else {
        // Cogemos el último procedimiento activo
        Procedimiento procedure = tp.get(tp.size() - 1);

        // Mergeamos los arrays de parametros y declaraciones del procedimiento activo
        ArrayList<Variable> aux = new ArrayList<>();
        aux.addAll(procedure.getParameters());
        aux.addAll(procedure.getDeclarations());

        // Recorremos el array mergeado para buscar el id pasado por parametro
        for (Variable variable : aux) {
          if (variable.getId().equals(id + "_" + tp.size())) {
            // Si se encuentra, devolvemos una referencia de esta variable
            return variable;
          }
        }

        // Si no se ha encontrado es que no se ha declarado, por lo que se crea
        if (var == null) {
          var = new Variable(variableType, id + "_" + tp.size(), false);
          if (declaringParams) {
            procedure.addParameter(var);
          } else {
            procedure.addDeclaration(var);

          }
        }
      }
    }

    // Añadimos la variable a la tabla
    tv.add(var);
    return var;
  }

  /**
   * Dados los parametro crea una nueva funcion y te la devuelve.
   *
   * @param id
   * @param fnReturnType
   * @return Funcion
   */
  public Procedimiento newFn(String id, StructureReturnType fnReturnType) {
    Procedimiento fn = new Procedimiento(id, fnReturnType);
    tp.add(fn);
    return fn;
  }

  /**
   * Busca por toda la tabla de variables la primera instancia del nombre de la
   * variable pasada por parametro. Devuelve dicha variable si se encuentra y null
   * en caso contrario
   *
   * @param id
   * @return Variable | null
   */
  public Variable findVarById(String id) {
    for (Variable var : this.tv) {
      if (var.getId().equals(id)) {
        return var;
      }
    }
    return null;
  }

  /**
   * Devuelve la última variable de la tabla de variables
   *
   * @return Variable
   */
  public Variable getLastVariable() {
    return tv.get(tv.size() - 1);
  }

  /**
   * Busca en la tabla de procedimientos por el procedimiento con id pasado por
   * parametro. Devuelve el procedimiento si se ha encontrado y null en caso
   * contrario
   *
   * @param id String
   * @return Procedmiento | null
   */
  public Procedimiento getProcedimiento(String id) {
    for (int i = 0; i < tp.size(); i++) {
      if (tp.get(i).getId().equals(id)) {
        return tp.get(i);
      }
    }
    return null;
  }

  /**
   * Añade una instrucción al codigo de 3 direcciones
   *
   * @param instr
   */
  public void addInstr(Instruction instr) {
    codigo3Dir.add(instr);
  }

  /**
   * Inicializa el tipo de declaracion al pasado por parametro
   *
   * @param declType
   */
  public void startDeclaration(TypeVar declType) {
    this.declType = declType;
  }

  /**
   */
  public void toggleParams() {
    declaringParams = !declaringParams;
  }

  public boolean getDeclaringParams() {
    return this.declaringParams;
  }

  /**
   * Resetea el tipo de declaracion
   */
  public void endDeclaration() {
    this.declType = null;
  }

  public TypeVar getDeclType() {
    return this.declType;
  }

  public ArrayList<Variable> getTv() {
    return this.tv;
  }

  public ArrayList<Procedimiento> getTp() {
    return this.tp;
  }

  public ArrayList<Instruction> getCodigo3Dir() {
    return codigo3Dir;
  }

  public void setCodigo3Dir(ArrayList<Instruction> codigo3Dir) {
    this.codigo3Dir = codigo3Dir;
  }

  public void setTv(ArrayList<Variable> tv) {
    this.tv = tv;
  }

  public ArrayList<Variable> getTvg() {
    return tvg;
  }

  public void setTvg(ArrayList<Variable> tvg) {
    this.tvg = tvg;
  }

  public void setTp(ArrayList<Procedimiento> tp) {
    this.tp = tp;
  }

  public int getLabelCounter() {
    return labelCounter;
  }

  public void setLabelCounter(int labelCounter) {
    this.labelCounter = labelCounter;
  }

  public int getVolatileCounter() {
    return volatileCounter;
  }

  public void setVolatileCounter(int volatileCounter) {
    this.volatileCounter = volatileCounter;
  }

  public void setDeclType(TypeVar declType) {
    this.declType = declType;
  }

  public void setDeclaringParams(boolean declaringParams) {
    this.declaringParams = declaringParams;
  }

  public ArrayList<Variable> getLids() {
    return lids;
  }

  public void setLids(ArrayList<Variable> lids) {
    this.lids = lids;
  }

  public ArrayList<Variable> getLargs() {
    return largs;
  }

  public void setLargs(ArrayList<Variable> largs) {
    this.largs = largs;
  }

  public void purgeArgs() {
    largs.clear();
  }

  public void addArg(Variable arg) {
    this.largs.add(arg);
  }

  public void purgeIds() {
    lids.clear();
  }

  public void addId(Variable id) {
    lids.add(id);
  }

  public void toggleCallFn() {
    this.callingFunction = !callingFunction;
  }

  public boolean isCallingFn() {
    return this.callingFunction;
  }

  public String getTvString() {
    String out = "";
    for (Variable variable : tv) {
      out += variable.toString() + "\n";
    }
    return out;
  }

  public String getTpString() {
    String out = "";
    for (Procedimiento procedure : tp) {
      out += procedure.toString() + "\n";
    }
    return out;
  }

  /**
   * Guarda el el código de 3 direcciones en un fichero de texto
   */
  public void saveThreeAddressCode(String fileName) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(Env.GENERATED_FILES + "/" + fileName));
      for (int i = 0; i < codigo3Dir.size(); i++) {
        writer.write(codigo3Dir.get(i).toString());
        writer.write("\n");
      }
      writer.close();
    } catch (IOException err) {
      System.out.println(err);
    }
  }

  @Override
  public String toString() {
    String out = "";
    for (Instruction instruction : codigo3Dir) {
      out += instruction.toString() + "\n";

    }
    return out;
  }

}
