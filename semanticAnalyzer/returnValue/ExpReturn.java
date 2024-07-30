package semanticAnalyzer.returnValue;

import errorHandler.ExpValueType;

public class ExpReturn {
    private final boolean isValue;
    private final String content;
    private final ExpValueType expValueType;

    public ExpReturn(boolean isValue, String content) {
        this.isValue = isValue;
        this.content = content;
        this.expValueType = ExpValueType.NULL;
    }

    public ExpReturn(int value) {
        this.isValue = true;
        this.content = String.valueOf(value);
        this.expValueType = ExpValueType.VAR_INT;
    }

    public ExpReturn(boolean isValue, String content, ExpValueType expValueType) {
        this.isValue = isValue;
        this.content = content;
        this.expValueType = expValueType;
    }

    public ExpReturn(int value, ExpValueType expValueType) {
        this.isValue = true;
        this.content = String.valueOf(value);
        this.expValueType = expValueType;
    }

    public boolean isValue() {
        return isValue;
    }

    public String getContent() {
        return content;
    }

    public int getValue() {
        assert isValue;
        return Integer.parseInt(content);
    }

    public ExpValueType getExpValueType() {
        return expValueType;
    }
}
