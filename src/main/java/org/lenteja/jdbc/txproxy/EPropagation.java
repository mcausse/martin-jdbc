package org.lenteja.jdbc.txproxy;

public enum EPropagation {

    NEW, // fa begin/commit si o si
    CREATE_OR_REUSE, // crea si cal, sino reutilitza
    NONE// com si no hi hagués @TransactionalMethod

}