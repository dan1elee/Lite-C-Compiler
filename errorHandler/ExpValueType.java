package errorHandler;

public enum ExpValueType {
    NOT_FOUND, NULL,
    INT_FUNC, VOID_FUNC, VOID,
    VAR_INT, VAR_INT_ARR_1, VAR_INT_ARR_2,
    VAR_CONST_INT, VAR_CONST_INT_ARR_1, VAR_CONST_INT_ARR_2;

    public ExpValueType calculate(ExpValueType lType) {
        if (this == NULL || lType == NULL) {
            return NULL;
        } else if (this == VOID || lType == VOID) {
            return VOID;
        } else if (this == VAR_INT_ARR_1 || this == VAR_CONST_INT_ARR_1
                || this == VAR_INT_ARR_2 || this == VAR_CONST_INT_ARR_2) {
            return this;
        } else if (lType == VAR_INT_ARR_1 || lType == VAR_CONST_INT_ARR_1
                || lType == VAR_INT_ARR_2 || lType == VAR_CONST_INT_ARR_2) {
            return lType;
        } else {
            return VAR_INT;
        }
    }
}
