package semanticAnalyzer.symbolTable.valueStore;

import java.util.ArrayList;
import java.util.HashMap;

public class ValueStore {
    private final HashMap<String, VarValue> values;
    private final ValueStore pre;
    private final ArrayList<ValueStore> posts;

    public ValueStore(ValueStore pre) {
        this.values = new HashMap<>();
        this.pre = pre;
        this.posts = new ArrayList<>();
        if (pre != null) {
            pre.addPost(this);
        }
    }

    public void addVarValue(String identName, int dimen, int[] dimens, int[][] value) {
        values.put(identName, new VarValue(dimen, dimens, value));
    }

    public void addPost(ValueStore post) {
        this.posts.add(post);
    }

    public int findVarValue(String identName, ArrayList<Integer> index) {
        if (values.containsKey(identName)) {
            return values.get(identName).getValueByIndex(index);
        }
        assert pre != null;
        return pre.findVarValue(identName, index);

    }
}
