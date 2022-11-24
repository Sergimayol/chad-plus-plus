package errors;

/**
 * ErrorCodes - Clase que contiene los códigos de error de la aplicación.
 * Se utiliza para identificar el tipo de error que se ha producido en la
 * compilación y evitar los 'magic numbers'.
 *
 * Assignatura 21742 - Compiladors
 * Estudis: Grau en Informàtica
 * Itinerari: Computació
 * Curs: 2022 - 2023
 *
 */
public enum ErrorCode {

    CUSTOM(""),
    FILE_NOT_FOUND("Couldn't find file."),
    INVALID_FILE_EXTENSION("Invalid file extension."),
    INVALID_FILE("Invalid file."),
    INVALID_TOKEN("Invalid Token."),
    PARSER_ERROR("Parser error.");

    public String message;

    ErrorCode(String message) {
        this.message = message;
    }

}